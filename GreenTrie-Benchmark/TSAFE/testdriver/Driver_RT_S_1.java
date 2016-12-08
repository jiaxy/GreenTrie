package testdriver;

import tsafe.common_datastructures.Point2D;
import tsafe.common_datastructures.PointXY;
import tsafe.server.computation.sub_computation.RouteTracker;

/**
 * Driver for RT_S_1 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_RT_S_1 {
	/**
	 * {@code true} when the snap point returned by RouteTracker.snapPointToRouteSegment
	 * is on the line from the aircraft orthogonal to the input segment.
	 */
	public static boolean _SNAP_POINT_COLLINEAR_WITH_ROUTE_SEGMENT = false;
	
	public static void main(String[] args) {
		Driver_RT_S_1 driver = new Driver_RT_S_1();
		RouteTracker rt = null;
		Point2D fp = null, fx1 = null, fx2 = null;
		
		driver.RT_S_1(rt, fp, fx1, fx2);
	}
	
	public void RT_S_1(RouteTracker routeTracker, Point2D flightPoint, Point2D fix1, Point2D fix2) {
		
			Common.assume_RT_S_preconditions_routeTracker(routeTracker);
			Common.assume_RT_S_preconditions_flightPoint(flightPoint);
			Common.assume_RT_S_preconditions_fix1(fix1);
			Common.assume_RT_S_preconditions_etc(flightPoint, fix1, fix2);
			Point2D snapPoint = routeTracker.snapPointToRouteSegment(flightPoint, fix1, fix2, false);
			_SNAP_POINT_COLLINEAR_WITH_ROUTE_SEGMENT = collinear(routeTracker, fix1, fix2, snapPoint);
			assert _SNAP_POINT_COLLINEAR_WITH_ROUTE_SEGMENT;
		
	}
	
	/**
	 * Checks whether three {@link Point2D}s are collinear according 
	 * to {@link #routeTracker}'s linear approximation. 
	 * 
	 * @param first a {@link Point2D}.
	 * @param second a {@link Point2D}.
	 * @param third a {@link Point2D}.
	 * @return {@code true} iff its parameters are on a same straight line.
	 */
	private boolean collinear(RouteTracker routeTracker, Point2D first, Point2D second, Point2D third) {
		PointXY firstXY = routeTracker.calculator.toXY(first);
		PointXY secondXY = routeTracker.calculator.toXY(second);
		PointXY thirdXY = routeTracker.calculator.toXY(third);
		double diff = firstXY.getX() * (secondXY.getY() - thirdXY.getY())
		           +  secondXY.getX() * (thirdXY.getY() - firstXY.getY())
		           +  thirdXY.getX() * (firstXY.getY() - secondXY.getY());
		return (diff == 0); //works only in the symbolic realm
	}
}
