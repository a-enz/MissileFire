package ch.ethz.sae;

import java.util.Iterator;
import java.util.List;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Interval;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import soot.IntegerType;
import soot.Local;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;

// Implement your numerical analysis here.
public class Analysis extends ForwardBranchedFlowAnalysis<AWrapper> {
	

	public static Manager man;
	private Environment env;
	public UnitGraph g;
	public String local_ints[]; // integer local variables of the method
	public static String reals[] = { "foo" };
	public SootClass jclass;
	
	
	/* === Constructor === */
	public Analysis(UnitGraph g, SootClass jc) {
		super(g);

		this.g = g;
		this.jclass = jc;

		buildEnvironment();
		instantiateDomain();
		
		System.out.println("*******************************\nInitializing Grid for the following Integer Variables:");
		for(String name : local_ints){
			System.out.print(name + "   ");
		}
		System.out.println();
	}
	

	private void recordIntLocalVars() {

		/*
		 * 'locals' is a list of all variables in a method. BUT it is important
		 * to note that it is not an exact representation of the program code variables
		 * i think a new variable is created for every assignment statement. those variables in
		 * 'locals' that represent the same real variable have similar names though.
		 */
		Chain<Local> locals = g.getBody().getLocals();
		
		int count = 0;
		Iterator<Local> it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			// String name = next.getName();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		local_ints = new String[count];

		int i = 0;
		it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				local_ints[i++] = name;
		}
	}

	/* Build an environment with integer variables. */
	public void buildEnvironment() {

		recordIntLocalVars();

		String ints[] = new String[local_ints.length];

		/* add local ints */
		for (int i = 0; i < local_ints.length; i++) {
			ints[i] = local_ints[i];
		}

		env = new Environment(ints, reals);
	}

	/* Instantiate a domain. */
	private void instantiateDomain() {
		/*
		 * if Polka is initialized with 'true' then the domain can 
		 * express strict inequalities, i.e., non-closed convex polyhedra
		 * else the domain expresses closed convex polyhedra
		 */
		man = new Polka(true);
	}

	void run() {
		doAnalysis();
	}

	// call this if you encounter statement/expression which you should not be handling
	static void unhandled(String what) {
		System.err.println("Can't handle " + what);
		System.exit(1);
	}

	// handle conditionals
	private void handleIf(AbstractBinopExpr expr, Abstract1 in, AWrapper ow,
			AWrapper ow_branchout) throws ApronException {

		Value left = expr.getOp1();
		Value right = expr.getOp2();

		// TODO handle JEqExpr, JNeExpr and the rest...
	}

	// handle assignments
	private void handleDef(Abstract1 o, Value left, Value right)
			throws ApronException {

		Texpr1Node lAr = null;
		Texpr1Node rAr = null;
		Texpr1Intern xp = null;

		if (left instanceof JimpleLocal) {
			String varName = ((JimpleLocal) left).getName();

			if (right instanceof IntConstant) {
				/* Example of handling assignment to an integer constant */
				IntConstant c = ((IntConstant) right);

				rAr = new Texpr1CstNode(new MpqScalar(c.value));
				xp = new Texpr1Intern(env, rAr);

				o.assign(man, varName, xp, null);

			} else if (right instanceof JimpleLocal) {
				/* Example of handling assignment to a local variable */
				if (env.hasVar(((JimpleLocal) right).getName())) {
					rAr = new Texpr1VarNode(((JimpleLocal) right).getName());
					xp = new Texpr1Intern(env, rAr);
					o.assign(man, varName, xp, null);
				}
			} else if (right instanceof BinopExpr) {
				//TODO right side binary expression

			} else {
				//unhandled("right side of assignment: '" + right.toString() + "'"); //we just print the unhandled statement and exit
			}
			
			/* TODO we also need to handle:
			 * -JMulExpr
			 * -JSubExpr
			 * -JAddExpr
			 * -JDivExpr
			 * 
			 * -JEqExpr (==)
			 * -JGeExpr (>=)
			 * -JGtExpr (>)
			 * -JLeExpr (>=)
			 * -JLtExpr (<)
			 * -JNeExpr (!=)
			 * 
			 * additionally we need to handle:
			 * -RefType (access to procedure calls like constructors and fire())
			 */
		} else{
			//we don't need to handle anything else but JimpleLocal objects
			//unhandled("left side of assignment: '" + right.toString() + "'"); //we just print the unhandled statement and exit
		}
	}
	
	/**
	 * TODO This is the method where we have to handle statements (labels)
	 * 
	 * @param current: 		
	 * @param op:			
	 * @param fallOut					
	 * @param branchOuts	
	 */

	@Override
	protected void flowThrough(AWrapper current, Unit op,
			List<AWrapper> fallOut, List<AWrapper> branchOuts) {
		
		/* we still need to initialize the `statement` field in AWrapper
		 * and this is the first opportunity we get. Unfortunately this
		 * method will be called several times on an particular AWrapper instance 
		 * which makes it also a weird place to initialize it. That's
		 * why we check for the null value
		 */
		if(current.getStatement() == null) current.setStatement(op); 
		
		
		Stmt s = (Stmt) op;
		Abstract1 in = ((AWrapper) current).get();

		Abstract1 o;
		try {
			//TEST OUTPUT START
			System.out.println("----------------------------------------------------------------------");
			System.out.println("Label: " + s.toString() + " | Wrapper: " + current.toString());
			System.out.println("======================================================================");
			System.out.print("fallOut Wrapper:");
			for(AWrapper fo : fallOut) {
				System.out.print(fo.toString());
			}
			System.out.print("\nbranchOuts Wrapper:");
			for(AWrapper bo : branchOuts){
				System.out.print(bo.toString());
			}
			
			//the following output should be the same as in the 2 lists above:
			System.out.println("\nfalling through to: " + this.getFallFlowAfter(op).toString());
			System.out.println("branching to: " + this.getBranchFlowAfter(op).toString());
			//TEST OUTPUT END
			
			
			o = new Abstract1(man, in);
			Abstract1 o_branchout = new Abstract1(man, in);

			if (s instanceof DefinitionStmt) {
				handleDef(in, ((DefinitionStmt) s).getLeftOp(), ((DefinitionStmt) s).getRightOp());
				//set changes to `current` and propagate to fallOut

			} else if (s instanceof JIfStmt) {
				// call handleIf
			} else {
				//unhandled("statement: '" + s.toString() + "'"); //we just print the unhandled statement and exit
			}
			
			//simple forwarding for now: TODO improve if necessary
			current.set(in);
			for(AWrapper ft : fallOut){
				ft.copy(current);
			}
			for(AWrapper bt : branchOuts){
				bt.copy(current);
			}

			//TODO somewhere in here we also have to handle loops
		} catch (ApronException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Initialize starting label (top)
	// in Apron terms: "top" means "universal" (always true)
	@Override
	protected AWrapper entryInitialFlow(){
		try{
			Abstract1 a1 = new Abstract1(man,env,false); //this initializes to top
			return new AWrapper(a1,man);
		} catch (ApronException e){
			e.printStackTrace();
			return null;
		}
	}

	// Implement Join
	@Override
	protected void merge(AWrapper src1, AWrapper src2, AWrapper trg) {

	}

	// Initialize all labels (bottom)
	// in Apron terms: "bottom" means "empty" (always false)
	@Override
	protected AWrapper newInitialFlow() {
		try{
			Abstract1 a1 = new Abstract1(man,env,true); //this initializes to bottom
			return new AWrapper(a1,man);
		} catch (ApronException e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void copy(AWrapper source, AWrapper dest) {
		try{
			dest.copy(source);
		} catch (Exception e){
			//TEST OUTPUT START
			System.err.println("Error occuring in copy() cought"); 
			System.exit(1);
			//TEST OUTPUT END
		}
		
	}

	/* It may be useful for widening */
	/*
	 * private HashSet<Stmt> backJumps = new HashSet<Stmt>(); private
	 * HashSet<Integer> backJumpIntervals = new HashSet<Integer>();
	 */
}
