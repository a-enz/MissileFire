package ch.ethz.sae;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import apron.ApronException;
import apron.Interval;
import soot.jimple.IntConstant;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.Local;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.BriefUnitGraph;

public class Verifier {

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println("Incorrect usage");
			System.exit(-1);
		}
		
		//args[0] is the name of the file we want to analyze:
		String analyzedClass = args[0];
		//create a soot object for this file:
		SootClass c = loadClass(analyzedClass);
		PAG pointsToAnalysis = doPointsToAnalysis(c);
		
		boolean programCorrectFlag = true;
		
		//do for all methods in the file we want to analyze:
		for (SootMethod method : c.getMethods()) {
			
			if(!method.getName().equals("<init>")){//cheap way to ignore the first function which should always be the same
				
				//TEST OUTPUT START
				System.out.println("method toString:\n" + method.toString());
				System.out.println(method.getActiveBody().toString());
				//TEST OUTPUT END
				
				
				Analysis analysis = new Analysis(new BriefUnitGraph(
						method.retrieveActiveBody()), c);
				
				for (Local var : method.getActiveBody().getLocals()){
					if (var.getType().toString().equals("MissileBattery")){
						DoublePointsToSet allocSite = (DoublePointsToSet) pointsToAnalysis.reachingObjects(var);
						System.out.println("Var :" + var.getName() + " reaches " + allocSite);
					}
					
				}
				
				analysis.run();
				
				//now check if the method is safe
				if(!isMethodSafe(analysis, pointsToAnalysis, method)){
					programCorrectFlag = false;
					break;
				}
			}//close if(!method.getName().equals("<init>")
		}
		
		if (programCorrectFlag) {
			System.out.println("Program " + analyzedClass + " is SAFE");
		} else {
			System.out.println("Program " + analyzedClass + " is UNSAFE");
		}
		
		
	}//close main

	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
	}

	private static PAG doPointsToAnalysis(SootClass c) {
		Scene.v().setEntryPoints(c.getMethods());

		HashMap<String, String> options = new HashMap<String, String>();
		options.put("enabled", "true");
		options.put("verbose", "false");
		options.put("propagator", "worklist");
		options.put("simple-edges-bidirectional", "false");
		options.put("on-fly-cg", "true");
		options.put("set-impl", "double");
		options.put("double-set-old", "hybrid");
		options.put("double-set-new", "hybrid");

		SparkTransformer.v().transform("", options);
		PAG pag = (PAG) Scene.v().getPointsToAnalysis();

		return pag;
	}
	
	private static boolean isMethodSafe(Analysis analysis, PAG graph, SootMethod method){
		
		boolean isSafe = true;
		Map<Local,Integer> allocSites = analysis.newMBattAlloc;
		AWrapper state;
				
		for(Unit label : method.getActiveBody().getUnits()){
			
			if((label instanceof JInvokeStmt) && (((JInvokeStmt)label).getInvokeExpr() instanceof JVirtualInvokeExpr)){
				state = analysis.getFlowBefore(label);
				
				JVirtualInvokeExpr expr = (JVirtualInvokeExpr) ((JInvokeStmt) label).getInvokeExpr();
				Value missileObject = expr.getBase();
				Value argument = expr.getArg(0); //might be JimpleLocal or IntConstant
				
				PointsToSet p2s = graph.reachingObjects((Local) missileObject);
				
				int size = 0;
				Interval argInterval;
				Interval missileInterval;
				
				System.out.println(missileObject + " has been allocated at: " + p2s);
				
				
				/* absolute shit-style programming coming up:
				 * iterate through all allocated MBatt objects
				 * and check if AllocNode of the object we use fire command on
				 * and object we created intersect, if yes assign size of that 
				 * created object
				 */
				
				for(Entry<Local,Integer> entry : allocSites.entrySet()){
					if(graph.reachingObjects(entry.getKey()).hasNonEmptyIntersection(p2s)){
						size = entry.getValue();
						break;
					}
				}
				
				try{
					missileInterval = new Interval(0,size);
					if(argument instanceof IntConstant) {
						int value = ((IntConstant) argument).value;
						argInterval = new Interval(value, value);
					}
					else if(argument instanceof JimpleLocal){
						argInterval = state.get().getBound(analysis.man, argument.toString());
					}
					else {
						argInterval = new Interval(-2,-1);
					}
					
					int intersectCount = missileInterval.cmp(argInterval);
					if(intersectCount <= 0) {
						isSafe = false; break;
					}
				} catch (ApronException e){
					
				}
				/* TODO Next steps to implement:
				 * 1. get allocation site DONE
				 * 2. retrieve size interval of specific MBatt with allocation info DONE
				 * 3. check if fire command violates rules:
				 * 	-->can't fire missile twice
				 *  -->can't fire missile out of size interval
				 */
				
			}
		}
		return isSafe;
	}
}
