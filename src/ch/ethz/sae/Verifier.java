package ch.ethz.sae;

import java.util.HashMap;
import java.util.Iterator;

import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.Local;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
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
						PointsToSet reachedVars = pointsToAnalysis.reachingObjects(var);

						System.out.println("Var :" + var.getName() + " reaches " + reachedVars);
					}
					
				}
				
				analysis.run();

				
				
				/* 
				 * 'g' the unit graph consists of:
				 * some data types to access the 'Unit' Interface which looks
				 * like it represents the program labels we discussed in class.
				 * Each unit represents a label in the program body
				 */
				
				/*TODO: use analysis results to check safety.
				 * this probably happens by checking if the 'size' field of a
				 * particular MissileBattery is in the Polyhedra domain after 
				 * the analysis
				 */
				if(!programCorrectFlag) break; //change that to be a condition on analysis result. then set flag to false
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
}
