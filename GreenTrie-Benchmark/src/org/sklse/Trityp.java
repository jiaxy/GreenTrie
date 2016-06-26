package org.sklse;


//import gov.nasa.jpf.symbc.probsym.Analyze;

/*
 * Taken from Figure 6 on p. 908 of
 * 
 * DeMillo, Offutt: Constraint-Based Automatic Test Data Generation, IEEE
 * Transactions on Software Engineering, Volume 17, Number 9, September 1991,
 * pp. 900--910.
 */

class Trityp {

	public static void covered(int br) {
		//Analyze.coverage(""+br);
	}

	// Our code (DeMillo/Offutt without coverage 
	public static int classify(int i, int j, int k) {
		if ((i <= 0) || (j <= 0) || (k <= 0)) {
			return 4;
		}
		int type = 0;
		if (i == j) {
			type = type + 1;
		}
		if (i == k) {
			type = type + 2;
		}
		if (j == k) {
			type = type + 3;
		}
		if (type == 0) {
			// Confirm it is a legal triangle before declaring it to be scalene.
			if ((i + j <= k) || (j + k <= i) || (i + k >= j)) {
				type = 4;
			} else {
				type = 1;
			}
			return type;
		}
		// Confirm it is a legal triangle before declaring it to be isosceles or
		// equilateral.
		if (type > 3) {
			type = 3;
		} else if ((type == 1) && (i + j > k)) {
			type = 2;
		} else if ((type == 2) && (i + k > j)) {
			type = 2;
		} else if ((type == 3) && (j + k > i)) {
			type = 2;
		} else {
			type = 4;
		}
		return type;
	}


	// Greg isosceles error
	public static int classify2(int a, int b, int c) {
	    if (a <= 0 || b <= 0 || c <= 0) {
	        return 4;
	    }
	    if (a == b && b == c) {
	        return 1;
	    }
	    if (a == b || b == c || c == a) {
	        return 2;
	    }
	    if (a <= c-b || b <= a-c || c <= b-a) {
	        return 4;
	    }
	    return 3;
	}
	
	// Dave, correct
	public static int classify3(int a, int b, int c) {
        
        //////////////////////////////////////////////////
        // test for triangle-ness

        // test all sides are positive
        if(a <= 0 || b <= 0 || c <= 0)
                return 4;

        // test that each side is less than the sum of the other two sides
        // convert to unsigned to avoid the case of two sides being greater than INT_MAX
        long ua = a;
        long ub = b;
        long uc = c;

        if(ua >= ub+uc)
                return 4;
        if(ub >= ua+uc)
                return 4;
        if(uc >= ua+ub)
                return 4;

        //////////////////////////////////////////////////
        // determine what kind of triangle
        if(a == b)
        {
                if(a == c)
                        return 1;
                else
                        return 2;
        }
        else
        {
                if(a == b || a == c || b == c)
                        return 2;
                else
                        return 3;
        }

	}
	
	// Russ, correct
	public static int classify4(int a, int b, int c) {
		if (a <= 0 || b <= 0 || c <= 0) {
	        return 4;
	    }
	    if (! (a > c - b && a > b - c && b > a - c)) {
	        return 4;
	    }
	    if (a == b && b == c) {
	        return 1;
	    }
	    if (a == b || b == c || a == c) {
	        return 2;
	    }
	    return 3;
	}

	
	public static void main(String[] Argv) {
		// assert classify(0, 1, 2) == 4;
		int type = classify(0, 1, 2);
		covered(type);
	}

}
