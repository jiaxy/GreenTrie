package testdriver;

import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.Point2D;
import tsafe.common_datastructures.PointXY;
import tsafe.common_datastructures.Route;
import tsafe.server.calculation.Calculator;
import tsafe.server.computation.data.RouteTrack;
import tsafe.server.computation.sub_computation.RouteTracker;
import tsafe.server.computation.sub_computation.TrajectorySynthesizer;

public final class Common {
	public static Route r;
	
	static void assume_RT_S_preconditions_routeTracker(RouteTracker routeTracker) {
		if (routeTracker == null) throw new AssumptionException();
		if (routeTracker.calculator == null) throw new AssumptionException();
	}

	static void assume_RT_S_preconditions_flightPoint(Point2D flightPoint) {
		if (flightPoint == null) throw new AssumptionException();
	}

	static void assume_RT_S_preconditions_fix1(Point2D fix1) {
		if (fix1 == null) throw new AssumptionException();
	}

	static void assume_RT_S_preconditions_etc(Point2D flightPoint, Point2D fix1, Point2D fix2) {
		if (fix2 == null) throw new AssumptionException();
		if (fix1 == fix2) throw new AssumptionException();
		if (fix1 == flightPoint) throw new AssumptionException();
		if (fix2 == flightPoint) throw new AssumptionException();
	}

	static void assume_TS_preconditions_trajSynth(TrajectorySynthesizer trajSynth) {
		if (trajSynth == null) throw new AssumptionException();
		if (trajSynth.calculator == null) throw new AssumptionException();

	}	

	static void assume_TS_preconditions_flightTrack(FlightTrack flightTrack) {
		if (flightTrack == null) throw new AssumptionException();
		if (flightTrack.getSpeed() <= 0) throw new AssumptionException();
	}
	
	static void assume_TS_R_preconditions_routeTrack(TrajectorySynthesizer trajSynth, RouteTrack routeTrack) {
		if (routeTrack.getPrevFix() == null) throw new AssumptionException();
		if (routeTrack.getNextFix() == null) throw new AssumptionException(); 
		if (!collinear(trajSynth.calculator, routeTrack.getPrevFix(), routeTrack.getNextFix(), new Point2D(routeTrack.getLatitude(), routeTrack.getLongitude()))) throw new AssumptionException();
		if (!inRectangle(trajSynth.calculator, routeTrack.getPrevFix(), routeTrack.getNextFix(), new Point2D(routeTrack.getLatitude(), routeTrack.getLongitude()))) throw new AssumptionException();
	}

	static void assume_TS_R_preconditions_route(Route route) {
		if (route == null) throw new AssumptionException();
		if (route.fixes == null) throw new AssumptionException();
		if (route.fixes.size() > 3) throw new AssumptionException(); //bounds the analysis
	}

	/**
	 * Checks whether three {@link Point2D}s are collinear according 
	 * to a linear approximation. 
	 * 
	 * @param calc an {@link EngineCalculator}, used to perform the 
	 *        linear approximation.
	 * @param first a {@link Point2D}.
	 * @param second a {@link Point2D}.
	 * @param third a {@link Point2D}.
	 * @return {@code true} iff its parameters are on a same straight line.
	 */
	static boolean collinear(Calculator calc, Point2D first, Point2D second, Point2D third) {
		PointXY firstXY = calc.toXY(first);
		PointXY secondXY = calc.toXY(second);
		PointXY thirdXY = calc.toXY(third);
		double diff = firstXY.getX() * (secondXY.getY() - thirdXY.getY())
		           +  secondXY.getX() * (thirdXY.getY() - firstXY.getY())
		           +  thirdXY.getX() * (firstXY.getY() - secondXY.getY());
		return (diff == 0); //works only in the symbolic realm
	}
	
	/**
	 * Checks whether two straight lines are orthogonal according 
	 * to {@link #routeTracker}'s linear approximation.
	 * 
	 * @param calc an {@link EngineCalculator}, used to perform the 
	 *        linear approximation.
	 * @param firstS1 a {@link Point2D}.
	 * @param secondS1 a {@link Point2D}.
	 * @param firstS2 a {@link Point2D}.
	 * @param secondS2 a {@link Point2D}.
	 * @return {@code true} iff the line on which {@code firstS1} and {@code secondS1}
	 *         lie is orthogonal to the line on which {@code firstS2} and {@code secondS2}
	 *         lie.
	 */
	static boolean orthogonal(Calculator calc, Point2D firstS1, Point2D secondS1, Point2D firstS2, Point2D secondS2) {
		PointXY firstS1XY = calc.toXY(firstS1);
		PointXY secondS1XY = calc.toXY(secondS1);
		PointXY firstS2XY = calc.toXY(firstS2);
		PointXY secondS2XY = calc.toXY(secondS2);
		double diff = (firstS1XY.getX() - secondS1XY.getX()) * (firstS2XY.getX() - secondS2XY.getX()) 
		            + (firstS1XY.getY() - secondS1XY.getY()) * (firstS2XY.getY() - secondS2XY.getY());
		return (diff == 0); //works only in the symbolic realm
	}
	
	/**
	 * Checks that a point is in a rectangle according 
	 * to a linear approximation.
	 * 
	 * @param calc an {@link EngineCalculator}, used to perform the 
	 *        linear approximation.
	 * @param firstCorner a {@link Point2D}, a corner of the rectangle.
	 * @param secondCorner a {@link Point2D}, the corner of the rectangle
	 *        opposite to {@code firstCorner}.
	 * @param toCheck another {@link Point2D}.
	 * @return {@code true} iff {@code toCheck} is in the rectangle
	 *         (according to {@link #trajSynth}'s linear approximation)
	 *         with opposite vertices {@code firstCorner} and {@code secondCorner}.
	 */
	private static boolean inRectangle(Calculator calc, Point2D firstCorner, Point2D secondCorner, Point2D toCheck) {
		final PointXY firstCornerXY = calc.toXY(firstCorner);
		final PointXY secondCornerXY = calc.toXY(secondCorner);
		final PointXY toCheckXY = calc.toXY(toCheck);
		return inInterval(firstCornerXY.getX(), secondCornerXY.getX(), toCheckXY.getX()) &&
				inInterval(firstCornerXY.getY(), secondCornerXY.getY(), toCheckXY.getY());
	}

	/**
	 * Checks if a value falls in an interval.
	 * 
	 * @param first One of the bounds of the interval.
	 * @param second The other bound of the interval. It is NOT required that {@code first <= second}.
	 * @param toCheck The value to be checked.
	 * @return {@code true} iff {@code toCheck} belongs to the interval {@code first..second}
	 *         or {@code second..first}, depending on which is the greater.
	 */
	private static boolean inInterval(double first, double second, double toCheck) {
		if (first == toCheck) {
			return true;
		} else if (first < toCheck) {
			return (toCheck <= second);
		} else { //toCheck < first
			return (second <= toCheck);
		}
	}
	
	//Do not make instances!
	private Common() { }
}
