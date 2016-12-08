package testdriver;

import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.Point2D;
import tsafe.common_datastructures.PointXY;
import tsafe.common_datastructures.Route;
import tsafe.server.calculation.Calculator;
import tsafe.server.computation.data.RouteTrack;
import tsafe.server.computation.sub_computation.RouteTracker;
import tsafe.server.computation.sub_computation.TrajectorySynthesizer;
/**
 * Driver for RT_S_2 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_RT_S_2 {
	//Atomic propositions
	
	/** 
	 * {@code true} when RouteTracker.snapPointToRouteSegment detect 
	 * that the snap point is a vertex of the route segment.
	 */
	public static boolean _SNAP_POINT_IS_A_VERTEX = false;
	
	/**
	 * {@code true} when the route from the aircraft to the snap point 
	 * returned by RouteTracker.snapPointToRouteSegment
	 * is orthogonal to the fix1--fix2 straight line.
	 */
	public static boolean _SNAP_POINT_PROJECTS_FLIGHT_POINT_ON_ROUTE_SEGMENT = false;

	public static void main(String[] args) {
		Driver_RT_S_2 driver = new Driver_RT_S_2();
		RouteTracker rt = null;
		Point2D fp = null, fx1 = null, fx2 = null;
		
		driver.RT_S_2(rt, fp, fx1, fx2);
	}
	
	private void RT_S_2(RouteTracker routeTracker, Point2D flightPoint, Point2D fix1, Point2D fix2) {
		try {
			Common.assume_RT_S_preconditions_routeTracker(routeTracker);
			Common.assume_RT_S_preconditions_flightPoint(flightPoint);
			Common.assume_RT_S_preconditions_fix1(fix1);
			Common.assume_RT_S_preconditions_etc(flightPoint, fix1, fix2);
			Point2D snapPoint = routeTracker.snapPointToRouteSegment(flightPoint, fix1, fix2, false);
			_SNAP_POINT_IS_A_VERTEX = 
					(fix1.getLatitude() == snapPoint.getLatitude() && fix1.getLongitude() == snapPoint.getLongitude()) ||
					(fix2.getLatitude() == snapPoint.getLatitude() && fix2.getLongitude() == snapPoint.getLongitude());
			//slight optimization: do not calculate _SNAP_POINT_PROJECTS_FLIGHT_POINT_ON_ROUTE_SEGMENT 
			//if _SNAP_POINT_IS_A_VERTEX is true
			if (!_SNAP_POINT_IS_A_VERTEX) { 
				_SNAP_POINT_PROJECTS_FLIGHT_POINT_ON_ROUTE_SEGMENT = Common.orthogonal(routeTracker.calculator, fix1, fix2, snapPoint, flightPoint);
			}
			assert (_SNAP_POINT_IS_A_VERTEX || _SNAP_POINT_PROJECTS_FLIGHT_POINT_ON_ROUTE_SEGMENT);
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
