package z3;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import z3.JavaExample.TestFailedException;

import com.microsoft.z3.ApplyResult;
import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.ArraySort;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Goal;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntSort;
import com.microsoft.z3.Log;
import com.microsoft.z3.Model;
import com.microsoft.z3.Params;
import com.microsoft.z3.Pattern;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;
import com.microsoft.z3.Symbol;
import com.microsoft.z3.Tactic;
import com.microsoft.z3.Version;
import com.microsoft.z3.Z3Exception;

public class Z3Test {

	@SuppressWarnings("serial")
	class TestFailedException extends Exception {
		public TestFailedException() {
			super("Check FAILED");
		}
	};

	Context ctx;

	@Before
	public void setup() throws Z3Exception {
		com.microsoft.z3.Global.ToggleWarningMessages(true);
		Log.open("test.log");

		System.out.print("Z3 Major Version: ");
		System.out.println(Version.getMajor());
		System.out.print("Z3 Full Version: ");
		System.out.println(Version.getString());
		HashMap<String, String> cfg = new HashMap<String, String>();
		// cfg.put("model", "true");
		cfg.put("proof", "true");
		ctx = new Context(cfg);
	}
	
	@Test
	 public void bitvectorExample1() throws TestFailedException, Z3Exception
	    {
	        System.out.println("BitvectorExample1");
	        Log.append("BitvectorExample1");

	        Sort bv_type = ctx.mkBitVecSort(32);
	        BitVecExpr x = (BitVecExpr) ctx.mkConst("x", bv_type);
	        BitVecNum zero = (BitVecNum) ctx.mkNumeral("0", bv_type);
	        BitVecNum ten = ctx.mkBV(10, 32);
	        BitVecExpr x_minus_ten = ctx.mkBVSub(x, ten);
	        /* bvsle is signed less than or equal to */
	        BoolExpr c1 = ctx.mkBVSLE(x, ten);
	        BoolExpr c2 = ctx.mkBVSLE(x_minus_ten, zero);
	        BoolExpr thm = ctx.mkIff(c1, c2);
	        System.out
	                .println("disprove: x - 10 <= 0 IFF x <= 10 for (32-bit) machine integers");
	        disprove(ctx, thm, false);
	    }
	
	
	   // / A simple array example.
	@Test
    public void arrayExample1() throws TestFailedException, Z3Exception
    {
        System.out.println("ArrayExample1");
        Log.append("ArrayExample1");

        Goal g = ctx.mkGoal(true, false, false);
        ArraySort asort = ctx.mkArraySort(ctx.getIntSort(),
                ctx.mkBitVecSort(32));
        ArrayExpr aex = (ArrayExpr) ctx.mkConst(ctx.mkSymbol("MyArray"), asort);
        Expr sel = ctx.mkSelect(aex, ctx.mkInt(0));
        g.add(ctx.mkEq(sel, ctx.mkBV(42, 32)));
        Symbol xs = ctx.mkSymbol("x");
        IntExpr xc = (IntExpr) ctx.mkConst(xs, ctx.getIntSort());

        Symbol fname = ctx.mkSymbol("f");
        Sort[] domain = { ctx.getIntSort() };
        FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
        Expr[] fargs = { ctx.mkConst(xs, ctx.getIntSort()) };
        IntExpr fapp = (IntExpr) ctx.mkApp(fd, fargs);

        g.add(ctx.mkEq(ctx.mkAdd(xc, fapp), ctx.mkInt(123)));

        Solver s = ctx.mkSolver();
        for (BoolExpr a : g.getFormulas())
            s.add(a);
        System.out.println("Solver: " + s);

        Status q = s.check();
        System.out.println("Status: " + q);

        if (q != Status.SATISFIABLE)
            throw new TestFailedException();

        System.out.println("Model = " + s.getModel());

        System.out.println("Interpretation of MyArray:\n"
                + s.getModel().getFuncInterp(aex.getFuncDecl()));
        System.out.println("Interpretation of x:\n"
                + s.getModel().getConstInterp(xc));
        System.out.println("Interpretation of f:\n"
                + s.getModel().getFuncInterp(fd));
        System.out.println("Interpretation of MyArray as Term:\n"
                + s.getModel().getFuncInterp(aex.getFuncDecl()));
    }

    // / Prove <tt>store(a1, i1, v1) = store(a2, i2, v2) implies (i1 = i3 or i2
    // = i3 or select(a1, i3) = select(a2, i3))</tt>.

    // / <remarks>This example demonstrates how to use the array
    // theory.</remarks>
    @Test
    public void arrayExample2() throws TestFailedException, Z3Exception
    {
        System.out.println("ArrayExample2");
        Log.append("ArrayExample2");

        Sort int_type = ctx.getIntSort();
        Sort array_type = ctx.mkArraySort(int_type, int_type);

        ArrayExpr a1 = (ArrayExpr) ctx.mkConst("a1", array_type);
        ArrayExpr a2 = ctx.mkArrayConst("a2", int_type, int_type);
        Expr i1 = ctx.mkConst("i1", int_type);
        Expr i2 = ctx.mkConst("i2", int_type);
        Expr i3 = ctx.mkConst("i3", int_type);
        Expr v1 = ctx.mkConst("v1", int_type);
        Expr v2 = ctx.mkConst("v2", int_type);

        Expr st1 = ctx.mkStore(a1, i1, v1);
        Expr st2 = ctx.mkStore(a2, i2, v2);

        Expr sel1 = ctx.mkSelect(a1, i3);
        Expr sel2 = ctx.mkSelect(a2, i3);

        /* create antecedent */
        BoolExpr antecedent = ctx.mkEq(st1, st2);

        /*
         * create consequent: i1 = i3 or i2 = i3 or select(a1, i3) = select(a2,
         * i3)
         */
        BoolExpr consequent = ctx.mkOr(ctx.mkEq(i1, i3), ctx.mkEq(i2, i3),
                ctx.mkEq(sel1, sel2));

        /*
         * prove store(a1, i1, v1) = store(a2, i2, v2) implies (i1 = i3 or i2 =
         * i3 or select(a1, i3) = select(a2, i3))
         */
        BoolExpr thm = ctx.mkImplies(antecedent, consequent);
        System.out
                .println("prove: store(a1, i1, v1) = store(a2, i2, v2) implies (i1 = i3 or i2 = i3 or select(a1, i3) = select(a2, i3))");
        System.out.println(thm);
        prove(ctx, thm, false);
    }

    // / Show that <code>distinct(a_0, ... , a_n)</code> is
    // / unsatisfiable when <code>a_i</code>'s are arrays from boolean to
    // / boolean and n > 4.

    // / <remarks>This example also shows how to use the <code>distinct</code>
    // construct.</remarks>
    @Test
    public void arrayExample3() throws TestFailedException, Z3Exception
    {
        System.out.println("ArrayExample3");
        Log.append("ArrayExample2");

        for (int n = 2; n <= 5; n++)
        {
            System.out.println("n = " + Integer.toString(n));

            Sort bool_type = ctx.mkBoolSort();
            Sort array_type = ctx.mkArraySort(bool_type, bool_type);
            Expr[] a = new Expr[n];

            /* create arrays */
            for (int i = 0; i < n; i++)
            {
                a[i] = ctx.mkConst("array_" + Integer.toString(i), array_type);
            }

            /* assert distinct(a[0], ..., a[n]) */
            BoolExpr d = ctx.mkDistinct(a);
            System.out.println(d);

            /* context is satisfiable if n < 5 */
            Model model = check(ctx, d, n < 5 ? Status.SATISFIABLE
                    : Status.UNSATISFIABLE);
            if (n < 5)
            {
                for (int i = 0; i < n; i++)
                {
                    System.out.println(a[i].toString() + " = "
                            + model.evaluate(a[i], false));
                }
            }
        }
    }
	
	@Test
	 public void bitvectorExample2() throws TestFailedException, Z3Exception
	    {
	        System.out.println("BitvectorExample1");
	        Log.append("BitvectorExample1");

	        Sort bv_type = ctx.mkBitVecSort(32);
	     
	        BitVecExpr x = (BitVecExpr) ctx.mkConst("x", bv_type);
	        BitVecNum zero = (BitVecNum) ctx.mkNumeral("0", bv_type);
	        BitVecNum ten =  (BitVecNum) ctx.mkNumeral("-1", bv_type);
	        BitVecExpr minus_x = ctx.mkBVSub(zero, x);
	        BitVecExpr sum = ctx.mkBVAdd(minus_x, minus_x);
	        /* bvsle is signed less than or equal to */
	        //BoolExpr c1 = ctx.mkBVSLE(x, ten);
	        BoolExpr c2 = ctx.mkEq(minus_x, ten);
	        //BoolExpr thm = ctx.mkIff(c1, c2);
	        
	        disprove(ctx, c2, true);
	    }
	
	
	
	@Test
	public void incrementalTest() throws Z3Exception, TestFailedException{
		  System.out.println("PushPopExample1");
	        Log.append("PushPopExample1");

	        /* create a big number */
	        IntSort int_type = ctx.getIntSort();
	        IntExpr big_number = ctx
	                .mkInt("100000000");

	        /* create number 3 */
	        IntExpr three = (IntExpr) ctx.mkNumeral("3", int_type);

	        /* create x */
	        IntExpr x = ctx.mkIntConst("x");

	        Solver solver = ctx.mkSolver();

	        /* assert x >= "big number" */
	        BoolExpr c1 = ctx.mkGe(x, big_number);
	        solver.add(c1);
	        System.out.println(solver.toString());
	        
	        /* create a backtracking point */
	        System.out.println("push");
	        solver.push();

	        /* assert x <= 3 */
	        BoolExpr c2 = ctx.mkLe(x, three);
	        solver.add(c2);
	        System.out.println(solver.toString());
	        /* context is inconsistent at this point */
	        if (solver.check() != Status.UNSATISFIABLE){
	        	 throw new TestFailedException();
	        }

	        /*
	         * backtrack: the constraint x <= 3 will be removed, since it was
	         * asserted after the last ctx.Push.
	         */
	        System.out.println("pop");
	        solver.pop(1);
	        System.out.println(solver.toString());
	        /* the context is consistent again. */
	        if (solver.check() != Status.SATISFIABLE)
	            throw new TestFailedException();

	        /* new constraints can be asserted... */

	        /* create y */
	        IntExpr y = ctx.mkIntConst("y");

	        /* assert y > x */
	        System.out.println("push");
	        BoolExpr c3 = ctx.mkGt(y, x);
	        solver.add(c3);
	        System.out.println(solver.toString());
	        /* the context is still consistent. */
	        if (solver.check() != Status.SATISFIABLE)
	            throw new TestFailedException();
	}
	
	@Test
	public void unsatCoreAndProofExample() throws Z3Exception {
		System.out.println("UnsatCoreAndProofExample");
		Log.append("UnsatCoreAndProofExample");

		Solver solver = ctx.mkSolver();

		BoolExpr pa = ctx.mkBoolConst("PredA");
		BoolExpr pb = ctx.mkBoolConst("PredB");
		BoolExpr pc = ctx.mkBoolConst("PredC");
		BoolExpr pd = ctx.mkBoolConst("PredD");
		BoolExpr p1 = ctx.mkBoolConst("P1");
		BoolExpr p2 = ctx.mkBoolConst("P2");
		BoolExpr p3 = ctx.mkBoolConst("P3");
		BoolExpr p4 = ctx.mkBoolConst("P4");
		BoolExpr[] assumptions = new BoolExpr[] { ctx.mkNot(p1), ctx.mkNot(p2),
				ctx.mkNot(p3), ctx.mkNot(p4) };
		BoolExpr f1 = ctx.mkAnd(pa, pb, pc);
		BoolExpr f2 = ctx.mkAnd(pa, ctx.mkNot(pb), pc);
		BoolExpr f3 = ctx.mkOr(ctx.mkNot(pa), ctx.mkNot(pc));
		BoolExpr f4 = ctx.mkNot(pb);

		solver.add(ctx.mkOr(f1, p1));
		solver.add(ctx.mkOr(f2, p2));
		solver.add(ctx.mkOr(f3, p3));
		solver.add(ctx.mkOr(f4, p4));
		Status result = solver.check(assumptions);

		if (result == Status.UNSATISFIABLE) {
			System.out.println("unsat");
			System.out.println("proof: " + solver.getProof());
			System.out.println("core: ");
			for (Expr c : solver.getUnsatCore()) {
				System.out.println(c);
			}
		}
	}

	@Test
	public void unsatCoreAndProofExample2() throws Z3Exception {
		System.out.println("UnsatCoreAndProofExample2");
		Log.append("UnsatCoreAndProofExample2");

		Solver solver = ctx.mkSolver();

		BoolExpr pa = ctx.mkBoolConst("PredA");
		BoolExpr pb = ctx.mkBoolConst("PredB");
		BoolExpr pc = ctx.mkBoolConst("PredC");
		BoolExpr pd = ctx.mkBoolConst("PredD");

		BoolExpr f1 = ctx.mkAnd(new BoolExpr[] { pa, pb, pc });
		BoolExpr f2 = ctx.mkAnd(new BoolExpr[] { pa, ctx.mkNot(pb), pc });
		BoolExpr f3 = ctx.mkOr(ctx.mkNot(pa), ctx.mkNot(pc));
		BoolExpr f4 = pd;

		BoolExpr p1 = ctx.mkBoolConst("P1");
		BoolExpr p2 = ctx.mkBoolConst("P2");
		BoolExpr p3 = ctx.mkBoolConst("P3");
		BoolExpr p4 = ctx.mkBoolConst("P4");

		solver.assertAndTrack(pa, p1);
		solver.assertAndTrack(ctx.mkNot(pa), p2);
		// solver.assertAndTrack(f3, p3);
		// solver.assertAndTrack(f4, p4);
		Status result = solver.check();

		if (result == Status.UNSATISFIABLE) {
			System.out.println("unsat");
			System.out.println("core: ");
			for (Expr c : solver.getUnsatCore()) {
				System.out.println(c);
			}
		}
	}

	@Test
	public void quantifierExample1() throws Z3Exception {
		System.out.println("QuantifierExample");
		Log.append("QuantifierExample");

		Sort[] types = new Sort[3];
		IntExpr[] xs = new IntExpr[3];
		Symbol[] names = new Symbol[3];
		IntExpr[] vars = new IntExpr[3];

		for (int j = 0; j < 3; j++) {
			types[j] = ctx.getIntSort();
			names[j] = ctx.mkSymbol("x_" + Integer.toString(j));
			xs[j] = (IntExpr) ctx.mkConst(names[j], types[j]);
			vars[j] = (IntExpr) ctx.mkBound(2 - j, types[j]); // <-- vars
																// reversed!
		}

		Expr body_vars = ctx.mkAnd(
				ctx.mkEq(ctx.mkAdd(vars[0], ctx.mkInt(1)), ctx.mkInt(2)),
				ctx.mkEq(ctx.mkAdd(vars[1], ctx.mkInt(2)),
						ctx.mkAdd(vars[2], ctx.mkInt(3))));

		Expr body_const = ctx.mkAnd(
				ctx.mkEq(ctx.mkAdd(xs[0], ctx.mkInt(1)), ctx.mkInt(2)),
				ctx.mkEq(ctx.mkAdd(xs[1], ctx.mkInt(2)),
						ctx.mkAdd(xs[2], ctx.mkInt(3))));

		Expr x = ctx.mkForall(types, names, body_vars, 1, null, null,
				ctx.mkSymbol("Q1"), ctx.mkSymbol("skid1"));
		System.out.println("Quantifier X: " + x.toString());

		Expr y = ctx.mkForall(xs, body_const, 1, null, null,
				ctx.mkSymbol("Q2"), ctx.mkSymbol("skid2"));
		System.out.println("Quantifier Y: " + y.toString());
	}

	void quantifierExample2(Context ctx) throws Z3Exception {

		System.out.println("QuantifierExample2");
		Log.append("QuantifierExample2");

		Expr q1, q2;
		FuncDecl f = ctx.mkFuncDecl("f", ctx.getIntSort(), ctx.getIntSort());
		FuncDecl g = ctx.mkFuncDecl("g", ctx.getIntSort(), ctx.getIntSort());

		// Quantifier with Exprs as the bound variables.
		{
			Expr x = ctx.mkConst("x", ctx.getIntSort());
			Expr y = ctx.mkConst("y", ctx.getIntSort());
			Expr f_x = ctx.mkApp(f, x);
			Expr f_y = ctx.mkApp(f, y);
			Expr g_y = ctx.mkApp(g, y);
			@SuppressWarnings("unused")
			Pattern[] pats = new Pattern[] { ctx.mkPattern(f_x, g_y) };
			Expr[] no_pats = new Expr[] { f_y };
			Expr[] bound = new Expr[] { x, y };
			Expr body = ctx.mkAnd(ctx.mkEq(f_x, f_y), ctx.mkEq(f_y, g_y));

			q1 = ctx.mkForall(bound, body, 1, null, no_pats, ctx.mkSymbol("q"),
					ctx.mkSymbol("sk"));

			System.out.println(q1);
		}

		// Quantifier with de-Brujin indices.
		{
			Expr x = ctx.mkBound(1, ctx.getIntSort());
			Expr y = ctx.mkBound(0, ctx.getIntSort());
			Expr f_x = ctx.mkApp(f, x);
			Expr f_y = ctx.mkApp(f, y);
			Expr g_y = ctx.mkApp(g, y);
			@SuppressWarnings("unused")
			Pattern[] pats = new Pattern[] { ctx.mkPattern(f_x, g_y) };
			Expr[] no_pats = new Expr[] { f_y };
			Symbol[] names = new Symbol[] { ctx.mkSymbol("x"),
					ctx.mkSymbol("y") };
			Sort[] sorts = new Sort[] { ctx.getIntSort(), ctx.getIntSort() };
			Expr body = ctx.mkAnd(ctx.mkEq(f_x, f_y), ctx.mkEq(f_y, g_y));

			q2 = ctx.mkForall(sorts, names, body, 1, null, // pats,
					no_pats, ctx.mkSymbol("q"), ctx.mkSymbol("sk"));
			System.out.println(q2);
		}

		System.out.println(q1.equals(q2));
	}

	// / Prove that <tt>f(x, y) = f(w, v) implies y = v</tt> when
	// / <code>f</code> is injective in the second argument. <seealso
	// cref="inj_axiom"/>

	public void quantifierExample3(Context ctx) throws TestFailedException,
			Z3Exception {
		System.out.println("QuantifierExample3");
		Log.append("QuantifierExample3");

		/*
		 * If quantified formulas are asserted in a logical context, then the
		 * model produced by Z3 should be viewed as a potential model.
		 */

		/* declare function f */
		Sort I = ctx.getIntSort();
		FuncDecl f = ctx.mkFuncDecl("f", new Sort[] { I, I }, I);

		/* f is injective in the second argument. */
		BoolExpr inj = injAxiom(ctx, f, 1);

		/* create x, y, v, w, fxy, fwv */
		Expr x = ctx.mkIntConst("x");
		Expr y = ctx.mkIntConst("y");
		Expr v = ctx.mkIntConst("v");
		Expr w = ctx.mkIntConst("w");
		Expr fxy = ctx.mkApp(f, x, y);
		Expr fwv = ctx.mkApp(f, w, v);

		/* f(x, y) = f(w, v) */
		BoolExpr p1 = ctx.mkEq(fxy, fwv);

		/* prove f(x, y) = f(w, v) implies y = v */
		BoolExpr p2 = ctx.mkEq(y, v);
		prove(ctx, p2, false, inj, p1);

		/* disprove f(x, y) = f(w, v) implies x = w */
		BoolExpr p3 = ctx.mkEq(x, w);
		disprove(ctx, p3, false, inj, p1);
	}

	// / Prove that <tt>f(x, y) = f(w, v) implies y = v</tt> when
	// / <code>f</code> is injective in the second argument. <seealso
	// cref="inj_axiom"/>

	public void quantifierExample4(Context ctx) throws TestFailedException,
			Z3Exception {
		System.out.println("QuantifierExample4");
		Log.append("QuantifierExample4");

		/*
		 * If quantified formulas are asserted in a logical context, then the
		 * model produced by Z3 should be viewed as a potential model.
		 */

		/* declare function f */
		Sort I = ctx.getIntSort();
		FuncDecl f = ctx.mkFuncDecl("f", new Sort[] { I, I }, I);

		/* f is injective in the second argument. */
		BoolExpr inj = injAxiomAbs(ctx, f, 1);

		/* create x, y, v, w, fxy, fwv */
		Expr x = ctx.mkIntConst("x");
		Expr y = ctx.mkIntConst("y");
		Expr v = ctx.mkIntConst("v");
		Expr w = ctx.mkIntConst("w");
		Expr fxy = ctx.mkApp(f, x, y);
		Expr fwv = ctx.mkApp(f, w, v);

		/* f(x, y) = f(w, v) */
		BoolExpr p1 = ctx.mkEq(fxy, fwv);

		/* prove f(x, y) = f(w, v) implies y = v */
		BoolExpr p2 = ctx.mkEq(y, v);
		prove(ctx, p2, false, inj, p1);

		/* disprove f(x, y) = f(w, v) implies x = w */
		BoolExpr p3 = ctx.mkEq(x, w);
		disprove(ctx, p3, false, inj, p1);
	}

	@Test
	public void basicTests() throws TestFailedException, Z3Exception {
		System.out.println("BasicTests");

		Symbol fname = ctx.mkSymbol("f");
		Symbol x = ctx.mkSymbol("x");
		Symbol y = ctx.mkSymbol("y");

		Sort bs = ctx.mkBoolSort();


		Sort[] domain = { bs, bs };
		FuncDecl f = ctx.mkFuncDecl(fname, domain, bs);
		Expr fapp = ctx.mkApp(f, ctx.mkConst(x, bs), ctx.mkConst(y, bs));

		Expr[] fargs2 = { ctx.mkFreshConst("cp", bs) };
		Sort[] domain2 = { bs };
		Expr fapp2 = ctx.mkApp(ctx.mkFreshFuncDecl("fp", domain2, bs), fargs2);
		
		BoolExpr trivial_eq = ctx.mkEq(fapp, fapp);
		BoolExpr nontrivial_eq = ctx.mkEq(fapp, fapp2);

		Goal g = ctx.mkGoal(true, false, false);
		g.add(trivial_eq);
		g.add(nontrivial_eq);
		System.out.println("Goal: " + g);

		Solver solver = ctx.mkSolver();

		for (BoolExpr a : g.getFormulas())
			solver.add(a);

		if (solver.check() != Status.SATISFIABLE)
			throw new TestFailedException();
		System.out.println(solver.getModel());

	}

	ApplyResult applyTactic(Context ctx, Tactic t, Goal g) throws Z3Exception {
		System.out.println("\nGoal: " + g);

		ApplyResult res = t.apply(g);
		System.out.println("Application result: " + res);

		Status q = Status.UNKNOWN;
		for (Goal sg : res.getSubgoals())
			if (sg.isDecidedSat())
				q = Status.SATISFIABLE;
			else if (sg.isDecidedUnsat())
				q = Status.UNSATISFIABLE;

		switch (q) {
		case UNKNOWN:
			System.out.println("Tactic result: Undecided");
			break;
		case SATISFIABLE:
			System.out.println("Tactic result: SAT");
			break;
		case UNSATISFIABLE:
			System.out.println("Tactic result: UNSAT");
			break;
		}

		return res;
	}

	void prove(Context ctx, BoolExpr f, boolean useMBQI)
			throws TestFailedException, Z3Exception {
		BoolExpr[] assumptions = new BoolExpr[0];
		prove(ctx, f, useMBQI, assumptions);
	}

	void prove(Context ctx, BoolExpr f, boolean useMBQI,
			BoolExpr... assumptions) throws TestFailedException, Z3Exception {
		System.out.println("Proving: " + f);
		Solver s = ctx.mkSolver();
		Params p = ctx.mkParams();
		p.add("mbqi", useMBQI);
		s.setParameters(p);
		for (BoolExpr a : assumptions)
			s.add(a);
		s.add(ctx.mkNot(f));
		Status q = s.check();

		switch (q) {
		case UNKNOWN:
			System.out.println("Unknown because: " + s.getReasonUnknown());
			break;
		case SATISFIABLE:
			throw new TestFailedException();
		case UNSATISFIABLE:
			System.out.println("OK, proof: " + s.getProof());
			break;
		}
	}

	void disprove(Context ctx, BoolExpr f, boolean useMBQI)
			throws TestFailedException, Z3Exception {
		BoolExpr[] a = {};
		disprove(ctx, f, useMBQI, a);
	}

	void disprove(Context ctx, BoolExpr f, boolean useMBQI,
			BoolExpr... assumptions) throws TestFailedException, Z3Exception {
		System.out.println("Disproving: " + f);
		Solver s = ctx.mkSolver();
		Params p = ctx.mkParams();
		p.add("mbqi", useMBQI);
		s.setParameters(p);
		for (BoolExpr a : assumptions)
			s.add(a);
		s.add(ctx.mkNot(f));
		Status q = s.check();

		switch (q) {
		case UNKNOWN:
			System.out.println("Unknown because: " + s.getReasonUnknown());
			break;
		case SATISFIABLE:
			System.out.println("OK, model: " + s.getModel());
			break;
		case UNSATISFIABLE:
			throw new TestFailedException();
		}
	}

	// / Create axiom: function f is injective in the i-th argument.

	// / <remarks>
	// / The following axiom is produced:
	// / <code>
	// / forall (x_0, ..., x_n) finv(f(x_0, ..., x_i, ..., x_{n-1})) = x_i
	// / </code>
	// / Where, <code>finv</code>is a fresh function declaration.

	public BoolExpr injAxiom(Context ctx, FuncDecl f, int i) throws Z3Exception {
		Sort[] domain = f.getDomain();
		int sz = f.getDomainSize();

		if (i >= sz) {
			System.out.println("failed to create inj axiom");
			return null;
		}

		/* declare the i-th inverse of f: finv */
		Sort finv_domain = f.getRange();
		Sort finv_range = domain[i];
		FuncDecl finv = ctx.mkFuncDecl("f_fresh", finv_domain, finv_range);

		/* allocate temporary arrays */
		Expr[] xs = new Expr[sz];
		Symbol[] names = new Symbol[sz];
		Sort[] types = new Sort[sz];

		/* fill types, names and xs */

		for (int j = 0; j < sz; j++) {
			types[j] = domain[j];
			names[j] = ctx.mkSymbol("x_" + Integer.toString(j));
			xs[j] = ctx.mkBound(j, types[j]);
		}
		Expr x_i = xs[i];

		/* create f(x_0, ..., x_i, ..., x_{n-1}) */
		Expr fxs = f.apply(xs);

		/* create f_inv(f(x_0, ..., x_i, ..., x_{n-1})) */
		Expr finv_fxs = finv.apply(fxs);

		/* create finv(f(x_0, ..., x_i, ..., x_{n-1})) = x_i */
		Expr eq = ctx.mkEq(finv_fxs, x_i);

		/* use f(x_0, ..., x_i, ..., x_{n-1}) as the pattern for the quantifier */
		Pattern p = ctx.mkPattern(fxs);

		/* create & assert quantifier */
		BoolExpr q = ctx.mkForall(types, /* types of quantified variables */
				names, /* names of quantified variables */
				eq, 1, new Pattern[] { p } /* patterns */, null, null, null);

		return q;
	}

	// / Create axiom: function f is injective in the i-th argument.

	// / <remarks>
	// / The following axiom is produced:
	// / <code>
	// / forall (x_0, ..., x_n) finv(f(x_0, ..., x_i, ..., x_{n-1})) = x_i
	// / </code>
	// / Where, <code>finv</code>is a fresh function declaration.

	public BoolExpr injAxiomAbs(Context ctx, FuncDecl f, int i)
			throws Z3Exception {
		Sort[] domain = f.getDomain();
		int sz = f.getDomainSize();

		if (i >= sz) {
			System.out.println("failed to create inj axiom");
			return null;
		}

		/* declare the i-th inverse of f: finv */
		Sort finv_domain = f.getRange();
		Sort finv_range = domain[i];
		FuncDecl finv = ctx.mkFuncDecl("f_fresh", finv_domain, finv_range);

		/* allocate temporary arrays */
		Expr[] xs = new Expr[sz];

		/* fill types, names and xs */
		for (int j = 0; j < sz; j++) {
			xs[j] = ctx.mkConst("x_" + Integer.toString(j), domain[j]);
		}
		Expr x_i = xs[i];

		/* create f(x_0, ..., x_i, ..., x_{n-1}) */
		Expr fxs = f.apply(xs);

		/* create f_inv(f(x_0, ..., x_i, ..., x_{n-1})) */
		Expr finv_fxs = finv.apply(fxs);

		/* create finv(f(x_0, ..., x_i, ..., x_{n-1})) = x_i */
		Expr eq = ctx.mkEq(finv_fxs, x_i);

		/* use f(x_0, ..., x_i, ..., x_{n-1}) as the pattern for the quantifier */
		Pattern p = ctx.mkPattern(fxs);

		/* create & assert quantifier */
		BoolExpr q = ctx.mkForall(xs, /* types of quantified variables */
				eq, /* names of quantified variables */
				1, new Pattern[] { p } /* patterns */, null, null, null);

		return q;
	}
	
	 Model check(Context ctx, BoolExpr f, Status sat) throws TestFailedException, Z3Exception
	    {
	        Solver s = ctx.mkSolver();
	        s.add(f);
	        if (s.check() != sat)
	            throw new TestFailedException();
	        if (sat == Status.SATISFIABLE)
	            return s.getModel();
	        else
	            return null;
	    }

	// / Assert the axiom: function f is commutative.

	// / <remarks>
	// / This example uses the SMT-LIB parser to simplify the axiom
	// construction.
	// / </remarks>
	private BoolExpr commAxiom(Context ctx, FuncDecl f) throws Exception {
		Sort t = f.getRange();
		Sort[] dom = f.getDomain();

		if (dom.length != 2 || !t.equals(dom[0]) || !t.equals(dom[1])) {
			System.out.println(Integer.toString(dom.length) + " "
					+ dom[0].toString() + " " + dom[1].toString() + " "
					+ t.toString());
			throw new Exception(
					"function must be binary, and argument types must be equal to return type");
		}

		String bench = "(benchmark comm :formula (forall (x " + t.getName()
				+ ") (y " + t.getName() + ") (= (" + f.getName() + " x y) ("
				+ f.getName() + " y x))))";
		ctx.parseSMTLIBString(bench, new Symbol[] { t.getName() },
				new Sort[] { t }, new Symbol[] { f.getName() },
				new FuncDecl[] { f });
		return ctx.getSMTLIBFormulas()[0];
	}
}
