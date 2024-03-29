package ch.ethz.sae;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import apron.Interval;
import soot.jimple.IntConstant;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
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
//				System.out.println("method toString:\n" + method.toString());
//				System.out.println(method.getActiveBody().toString());
				//TEST OUTPUT END
				
				
				Analysis analysis = new Analysis(new BriefUnitGraph(
						method.retrieveActiveBody()), c);
				//TEST OUTPUT START
//				for (Local var : method.getActiveBody().getLocals()){
//					if (var.getType().toString().equals("MissileBattery")){
//						DoublePointsToSet allocSite = (DoublePointsToSet) pointsToAnalysis.reachingObjects(var);
//						System.out.println("Var :" + var.getName() + " reaches " + allocSite);
//					}
//					
//				}
				//TEST OUTPUT END
				
				analysis.run();
				
				//TEST OUTPUT START
//				for(Unit u: method.getActiveBody().getUnits()){
//					AWrapper a = analysis.getFlowBefore(u);
//					System.out.println("Wrapper " + a.toString() + " | Label: " + u);
//				}
				//TEST OUTPUT END
				
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
		
		Map<Local,boolean[]> allocSites = analysis.newMBattAlloc; //map of allocated MBatts
		AWrapper state; //wrapper for the state at a specific label
		
		//we iterate over all labels and look at all fire commands:
		for(Unit label : method.getActiveBody().getUnits()){
			if((label instanceof JInvokeStmt) && (((JInvokeStmt)label).getInvokeExpr() instanceof JVirtualInvokeExpr)){
				state = analysis.getFlowBefore(label);
				
				//we get the missileObj the fire command is executed on and the argument of the call
				JVirtualInvokeExpr expr = (JVirtualInvokeExpr) ((JInvokeStmt) label).getInvokeExpr();
				Value missileObject = expr.getBase();
				Value argument = expr.getArg(0); //might be JimpleLocal or IntConstant
				
				//now we get information about the allocation node of the missileObj
				PointsToSet p2s = graph.reachingObjects((Local) missileObject);
				
				boolean[] alreadyFired = {};
				Interval argInterval;
				Interval missileInterval;
				
				//TEST OUTPUT START
//				System.out.println(missileObject + " has been allocated at: " + p2s);
				//TEST OUPUT END
				
				/* absolute shit-style programming coming up:
				 * iterate through all allocated MBatt objects
				 * and check if AllocNode of the object we use fire command on
				 * and object we created intersect, if yes assign size of that 
				 * created object
				 */
				
				for(Entry<Local,boolean[]> entry : allocSites.entrySet()){
					if(graph.reachingObjects(entry.getKey()).hasNonEmptyIntersection(p2s)){
						alreadyFired = entry.getValue();
						break;
					}
				}
				
				/*t his try catch block should probably be moved to an Analysis class method b/c
				 * it uses a lot of apron classes (we should keep the apron stuff in Analysis.java)
				 * --> create a method `toInterval` or similar.
				 */
				
				if(argument instanceof IntConstant) {
					int value = ((IntConstant) argument).value;
					argInterval = new Interval(value,value);
				}
				else if(argument instanceof JimpleLocal){
					argInterval = analysis.toInterval(state, argument.toString());
				}
				else{
					argInterval = new Interval(-1,-1); //sure to be out of bound
				}
				
				//now we do the checks for out of bound and already fired missiles a.s.o.
				if(state.isBottom()){
					/* if the state is bottom ANY call to fire() is apparently safe? 
					 * we do nothing (which mean keep 'isSafe' at true)
					 */
				}
				else if (argInterval.isTop()){//we check for top because we can't assign inf to integers =)
					isSafe = false;
					break;
				}
				else{
					//shitty way to convert interval borders to ints
					double[] convert = new double[1];
					argInterval.inf.toDouble(convert,0);
					int lower = (int) convert[0];
					argInterval.sup.toDouble(convert, 0);
					int upper = (int) convert[0];
					
					//check the if the range is in the alowed interval
					//missiles are fired in the range of [0,alreadyFired.lenth -1]
					if(lower < 0 || upper >= alreadyFired.length){
						isSafe = false;
						break;
					}
					
					//now mark fired missiles in the bitmap and report if we already fired a missile
					//if we fire a single missile instead of range then: lower == upper
					int i = lower;
					if(lower == upper){
						if(!alreadyFired[i]) alreadyFired[i] = true;
						else{
							isSafe = false;
							break;
						}
					}else{
						while(i < upper){
							if(!alreadyFired[i]) alreadyFired[i] = true;
							else{
								isSafe = false;
								break;
							}
							i++;
						}
					}
					/*we need this conditional b/c when breaking out of above
					 * while loop we didn't break out of the for loop (which we need to)
					 */
					if(!isSafe) break;
				}
			}//close if condition to check if this is a firecall
		}//close iteration over method labels
		return isSafe;
	}
}
