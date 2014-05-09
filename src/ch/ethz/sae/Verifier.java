package ch.ethz.sae;

import java.util.HashMap;
import java.util.Iterator;

import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.Local;
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
			
			//TEST OUTPUT START
			System.out.println("method toString:\n" + method.toString());
			System.out.println(method.getActiveBody().toString());
			//TEST OUTPUT END
			
			Analysis analysis = new Analysis(new BriefUnitGraph(
					method.retrieveActiveBody()), c);
			
			//analysis.run();
			
			
			//TEST OUTPUT START
			Iterator<Unit> uit = analysis.g.iterator();

			for(Local loco : analysis.g.getBody().getLocals()){
				System.out.println(loco.toString());
			}
			
			while(uit.hasNext()){
				Unit u = uit.next();
				System.out.println(u.toString() + " || " + u.getUseAndDefBoxes().toString());
			}
			//TEST OUTPUT END
			
			/* 
			 * 'g' the unit graph consists of:
			 * some data types to access the 'Unit' Interface which looks
			 * like it represents the program labels we discussed in class.
			 * Each unit represents a label in the program body
			 */
			
			//TODO: use analysis results to check safety
			if(!programCorrectFlag) break; //change that to be a condition on analysis result. then set flag to false
		}
		
		if (programCorrectFlag) {
			System.out.println("Program " + analyzedClass + " is SAFE");
		} else {
			System.out.println("Program " + analyzedClass + " is UNSAFE");
		}
	}

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
