package ch.ethz.sae;

import java.util.HashMap;

import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;

public class Verifier {

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println("Incorrect usage");
			System.exit(-1);
		}
		
		String analyzedClass = args[0];
		SootClass c = loadClass(analyzedClass);
		PAG pointsToAnalysis = doPointsToAnalysis(c);

		int programCorrectFlag = 1;

		for (SootMethod method : c.getMethods()) {

			Analysis analysis = new Analysis(new BriefUnitGraph(
					method.retrieveActiveBody()), c);
			
			analysis.run();
			
			//TODO: use analysis results to check safety

		}
		
		if (programCorrectFlag == 1) {
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
