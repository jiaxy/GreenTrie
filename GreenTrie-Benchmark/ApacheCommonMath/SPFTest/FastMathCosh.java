package SPFTest;

import org.apache.commons.math3.util.FastMath;

public class FastMathCosh {

	public static void main(String[] args) {

		System.out.println(cosh(0));
	}


	public static double cosh(double num){
		return FastMath.cosh(num);
	}
	
	public static double test(double num){
		if(num>0){
			return num;
		}else if(-1/num<1){
			return 1/num;
		}
		return -num;
	}

}
