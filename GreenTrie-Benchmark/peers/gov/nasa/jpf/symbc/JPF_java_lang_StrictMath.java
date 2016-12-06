package gov.nasa.jpf.symbc;


import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.symbc.numeric.MathFunction;
import gov.nasa.jpf.symbc.numeric.MathRealExpression;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_java_lang_StrictMath extends NativePeer {

	 @MJI
	  public static double log__D__D (final MJIEnv env, final int clsObjRef, final double a) {
		 return StrictMath.log(a);
	  }
}
