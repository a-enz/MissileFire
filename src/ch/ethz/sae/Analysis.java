package ch.ethz.sae;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.sun.org.apache.bcel.internal.generic.LCONST;

import apron.Abstract1;
import apron.ApronException;
import apron.Environment;
import apron.Lincons1;
import apron.Linexpr1;
import apron.Linterm1;
import apron.Manager;
import apron.MpqScalar;
import apron.Polka;
import apron.Tcons1;
import apron.Texpr0Node;
import apron.Texpr1BinNode;
import apron.Texpr1CstNode;
import apron.Texpr1Intern;
import apron.Texpr1Node;
import apron.Texpr1VarNode;
import soot.IntegerType;
import soot.Local;
import soot.SootClass;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
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
	//helper int
	private int iterCount = 0;
	private int JNewStmtCount = 0;
	
	/* The following HashMap we will be using to keep track of loops:
	 * Key value is the BackJumpStmt of the loop and the Integer
	 * Value indicates how many times the Stmt has been executed
	 */
	private Map<Stmt,Integer> loopHeadCounts = new HashMap<Stmt,Integer>();
	public Map<Local,Integer> newMBattAlloc = new HashMap<Local,Integer>();

	
	/* === Constructor === */
	public Analysis(UnitGraph g, SootClass jc) {
		super(g);

		this.g = g;
		this.jclass = jc;

		buildEnvironment();
		instantiateDomain();
		getLoopData();
		
		
		//TEST OUTPUT START
		System.out.println("*******************************\nInitializing Grid for the following Integer Variables:");
		for(String name : local_ints){
			System.out.print(name + "   ");
		}
		System.out.println();
		//TEST OUTPUT END
	}
	

	private void recordIntLocalVars() {

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
		
		//sets variable true if right operand on binary comparison is variable, false if const., error otherwise.
		boolean rightIsVariable = false;
		if(right instanceof JimpleLocal){
			rightIsVariable = true;
		}else if(right instanceof IntConstant){
		}else{
			unhandled("right operand of binaryexpression in if statement");
		}
		
		boolean leftIsVariable = false;
		if(left instanceof JimpleLocal){
			leftIsVariable = true;
		}else if(left instanceof IntConstant){
		}else{
			unhandled("right operand of binaryexpression in if statement");
		}
		
		//handle an expression like: x 'comparison operator' (y/const.)

		Texpr1Node leftNode;
		if(leftIsVariable){
			leftNode = new Texpr1VarNode(left.toString());
		}else{
			leftNode = new Texpr1CstNode(new MpqScalar(Integer.parseInt(left.toString())));
		}
		
		Texpr1Node rightNode;
		if(rightIsVariable){
			rightNode = new Texpr1VarNode(right.toString());
		}else{
			rightNode = new Texpr1CstNode(new MpqScalar(Integer.parseInt(right.toString())));
		}
		
		if (expr instanceof JEqExpr){
			
			Tcons1 consIf = new Tcons1(Tcons1.EQ, new Texpr1Intern(env, new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));
			
			Tcons1 consElse = new Tcons1(Tcons1.DISEQ,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));

			ow.set(new Abstract1(man, new Tcons1[] {consIf}));
			ow_branchout.set(new Abstract1(man, new Tcons1[] {consElse}));	

		}
		else if (expr instanceof JNeExpr){
			
			Tcons1 consIf = new Tcons1(Tcons1.DISEQ, new Texpr1Intern(env, new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));
			
			Tcons1 consElse = new Tcons1(Tcons1.EQ,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));

			ow.set(new Abstract1(man, new Tcons1[] {consElse}));
			ow_branchout.set(new Abstract1(man, new Tcons1[] {consIf}));
		}
		else if (expr instanceof JGeExpr){

			Tcons1 consIf  = new Tcons1(Tcons1.SUPEQ,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));
			Tcons1 consElse  = new Tcons1(Tcons1.SUP,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, rightNode, leftNode)));
					

			ow.set(new Abstract1(man, new Tcons1[] {consElse}));
			ow_branchout.set(new Abstract1(man, new Tcons1[] {consIf}));
		}
		else if (expr instanceof JGtExpr){

			Tcons1 consIf  = new Tcons1(Tcons1.SUP,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));
			Tcons1 consElse  = new Tcons1(Tcons1.SUPEQ,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, rightNode, leftNode)));
					

			ow.set(new Abstract1(man, new Tcons1[] {consElse}));
			ow_branchout.set(new Abstract1(man, new Tcons1[] {consIf}));
		}
		else if (expr instanceof JLeExpr){

			Tcons1 consIf  = new Tcons1(Tcons1.SUPEQ,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, rightNode, leftNode)));
			Tcons1 consElse  = new Tcons1(Tcons1.SUP,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));
					

			ow.set(new Abstract1(man, new Tcons1[] {consElse}));
			ow_branchout.set(new Abstract1(man, new Tcons1[] {consIf}));
		}
		else if (expr instanceof JLtExpr){

			Tcons1 consIf  = new Tcons1(Tcons1.SUP,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, rightNode, leftNode)));
			Tcons1 consElse  = new Tcons1(Tcons1.SUPEQ,new Texpr1Intern(env,new Texpr1BinNode(Texpr1BinNode.OP_SUB, leftNode, rightNode)));

			ow.set(new Abstract1(man, new Tcons1[] {consElse}));
			ow_branchout.set(new Abstract1(man, new Tcons1[] {consIf}));
		}
		else{
			unhandled("if statement with unhandled operator");
		}
	}

	// handle assignments
	private void handleDef(Abstract1 o, Value left, Value right)
			throws ApronException {

		Texpr1Node rAr = null;
		Texpr1Intern xp = null;
		
		if (left instanceof JimpleLocal) {
			//handle local variable assignments
			String varName = ((JimpleLocal) left).getName();
			
			if (right instanceof IntConstant) {
				//assign to Integer Values
				IntConstant c = ((IntConstant) right);
				
				rAr = new Texpr1CstNode(new MpqScalar(c.value));
				xp = new Texpr1Intern(env, rAr);

				o.assign(man, varName, xp, null);

			} else if (right instanceof JimpleLocal) {
				/* Example of handling assignment to a local variable 
				 * only handle it if it is an Integer variable and
				 * thus stored in our Environment `env`
				 */
				if (env.hasVar(((JimpleLocal) right).getName())) {
					rAr = new Texpr1VarNode(((JimpleLocal) right).getName());
					xp = new Texpr1Intern(env, rAr);
					
					o.assign(man, varName, xp, null);
				}
			} else if (right instanceof BinopExpr) {

				rAr = jimpleToApronTree((BinopExpr) right);
				xp = new Texpr1Intern(env, rAr);
				
				o.assign(man, varName, xp, null);
			} else {
				//nothing happens to other cases
			}
		} else{
			//we don't need to handle anything else but JimpleLocal objects
			unhandled("left side of assignment: '" + right.toString() + "'"); //we just print the unhandled statement and exit
		}
	}
	

	@Override
	protected void flowThrough(AWrapper current, Unit op,
			List<AWrapper> fallOut, List<AWrapper> branchOuts) {
		
		Stmt s = (Stmt) op;
		Abstract1 in = ((AWrapper) current).get();

		
		try {
			//TEST OUTPUT START
			
			printLabel(s,current);
			
			System.out.print("BEFORE TRANSFORMER:\nfallOut:");
			for(AWrapper fo : fallOut) {
				System.out.print(fo.toString() + " associated statement: " + fo.getStatement() + " | ");
			}
			System.out.print("\nbranchOuts:");
			for(AWrapper bo : branchOuts){
				System.out.print(bo.toString()  + " associated statement: " + bo.getStatement() + " | ");
			}
			System.out.println();
			
			//TEST OUTPUT END
			

			Abstract1 o = new Abstract1(man, in);
			Abstract1 o_branchout = new Abstract1(man, in);
			

			if (s instanceof DefinitionStmt) {
				Value l = ((DefinitionStmt) s).getLeftOp();
				Value r = ((DefinitionStmt) s).getRightOp();
				handleDef(o, l, r);
				
				for (AWrapper ft : fallOut){
					ft.set(o);
				}
			} else if (s instanceof JIfStmt) {
				Value cond = ((JIfStmt) s).getCondition();
				AWrapper ft = fallOut.get(0);
				//we know there will be a branchout because
				//we're handling a conditional statement
				AWrapper bt = branchOuts.get(0);
				
				handleIf((AbstractBinopExpr) cond, in, ft, bt);
				
				o.meet(man, ft.get());
				ft.set(o);
					
				o_branchout.meet(man, bt.get());
				bt.set(o_branchout);

			} else if (s instanceof JInvokeStmt){
				Value expr = s.getInvokeExpr();
				if(expr instanceof JVirtualInvokeExpr){
					//fire commands, maybe do something with that?
				} else if (expr instanceof JSpecialInvokeExpr){
					Value capacity = ((JSpecialInvokeExpr) expr).getArg(0);
					Local missileObj = (Local) ((JSpecialInvokeExpr) expr).getBase();
					if(capacity instanceof IntConstant){
						JNewStmtCount++;
						//safe newly instantiated MBatt object and argument int
						newMBattAlloc.put(missileObj, ((IntConstant) capacity).value);
					} else{
						unhandled("MissileBattery not instantiated with IntConstant");
					}
				}
				
				//just propagate the fallthrough case
				for (AWrapper ft : fallOut){
					ft.set(o);
				}
				
			} else if (s instanceof JGotoStmt) {
				
				
				//just propagate the branch case
				for (AWrapper ft : branchOuts){
					ft.set(o);
				}
			} else {
				
				
				//just propagate the fallthrough case
				for (AWrapper ft : fallOut){
					ft.set(o);
				}
			}
			

			//TEST OUTPUT START
			System.out.print("\nAFTER TRANSORMER:\nfallOut:");
			for(AWrapper fo : fallOut) {
				System.out.print(fo.toString());
			}
			System.out.print("\nbranchOuts:");
			for(AWrapper bo : branchOuts){
				System.out.print(bo.toString());
			}
			System.out.println("\n");
			//TEST OUTPUT END
			
			
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
	/* Implement merge dummy method because it is an abstract method we need to
	 * implement, but it uses the second merge method anyway so we should be fine
	 *
	 */
	@Override 
	protected void merge(AWrapper src1, AWrapper src2, AWrapper trg){
		//should not be called
	}
	
	// Real implementation of join and widening
	@Override
	protected void merge(Unit node, AWrapper src1, AWrapper src2, AWrapper trg) {
		Abstract1 in1, in2;
		in1 = src1.get();
		in2 = src2.get();
		Stmt s = (Stmt) node;
		try {
			if (loopHeadCounts.containsKey(s)) { //join/widening of loops
				int count = loopHeadCounts.get(s);
				if (count > 5) { // we use GT because merge happens before the loop has been executed
					trg.set(in1.widening(man, in2));
					System.out.println("====>widen to: " + trg.toString());
				} else { //merge and update count
					trg.set(in1.joinCopy(man, in2));
					loopHeadCounts.put(s, count + 1); //update count
				}
			} else {
				trg.set(in1.joinCopy(man, in2)); //normal join
			}
			
		} catch (ApronException e){
			e.printStackTrace();
		}
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
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Function to create a Expr Tree in Apron out of a Jimple Parse Tree.
	 * It is a bit overkill because a complicated assignment expression
	 * will be already broken down by Jimple into simple binary expressions
	 * when constructing the program Labels (Units)
	 * 
	 * @param val 
	 * @return
	 */
	private Texpr1Node jimpleToApronTree (Value val){

		if (val instanceof IntConstant){
			IntConstant c = ((IntConstant) val);
			return new Texpr1CstNode(new MpqScalar(c.value));
		}
		else if (val instanceof JimpleLocal){
			if (env.hasVar(((JimpleLocal) val).getName())) {
				return new Texpr1VarNode(((JimpleLocal) val).getName());
			} else unhandled("non-integer variable in tree: '" + val.toString() + "'"); //we just print the unhandled statement and exit
		}
		else if (val instanceof BinopExpr){//val instanceof JAddExpr || val instanceof JSubExpr || val instanceof JMulExpr || val instanceof JDivExpr){
			Value left = ((BinopExpr) val).getOp1();
			Value right = ((BinopExpr) val).getOp2();
			Texpr1Node lAr = jimpleToApronTree(left);
			Texpr1Node rAr = jimpleToApronTree(right);
			
			int opCode = 0;
			if (val instanceof JAddExpr) opCode = Texpr1BinNode.OP_ADD; //intvalue : 0
			else if (val instanceof JSubExpr) opCode = Texpr1BinNode.OP_SUB; //intvalue: 1
			else if (val instanceof JMulExpr) opCode = Texpr1BinNode.OP_MUL; //intvalue: 2
			else if (val instanceof JDivExpr) opCode = Texpr1BinNode.OP_DIV; //intvalue: 3
			else unhandled("binary operator in tree: '" + val.toString() + "'"); //we just print the unhandled statement and exit
			
			return new Texpr1BinNode(opCode, lAr, rAr);
			
		} else unhandled("expression in tree: '" + val.toString() + "'"); //we just print the unhandled statement and exit
		return null; //should never happen, call to unhandled exits the system
	}
	
	
	private void getLoopData (){
		LoopNestTree loops= new LoopNestTree(g.getBody());
		
		//initialize loopHeadCount with integer value 0 
		//which indicates this loop has never been taken
		for(Loop l : loops){
			loopHeadCounts.put(l.getHead(), 0);
		}
		
	}
	
	//TEST OUTPUT START
	private void printLabel(Stmt s, AWrapper current){
		iterCount++;
		System.out.println("----------------------------------------------------------------------");
		System.out.println("Iteration " + iterCount + ": " + s.toString() + s.getClass() + " \nWrapper: " + current.toString());
		System.out.println("======================================================================");
		
	}
	//TEST OUTPUT END
}
