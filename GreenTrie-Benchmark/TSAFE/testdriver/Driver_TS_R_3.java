package testdriver;

import tsafe.common_datastructures.Fix;
import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.Point2D;
import tsafe.common_datastructures.Point4D;
import tsafe.common_datastructures.PointXY;
import tsafe.common_datastructures.Route;
import tsafe.common_datastructures.Trajectory;
import tsafe.common_datastructures.client_server_communication.UserParameters;
import tsafe.server.calculation.Calculator;
import tsafe.server.computation.data.RouteTrack;
import tsafe.server.computation.sub_computation.RouteTracker;
import tsafe.server.computation.sub_computation.TrajectorySynthesizer;
/**

/**
 * Drivers for TS_R_* properties.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_R_3 {
	//Atomic propositions
	
	/**
	 * {@code true} iff the time difference between the first and last point 
	 * in the trajectory is the time horizon. 
	 */
	public static boolean _TRAJ_TRAVELS_MAX_TIME;
	
	/** 
	 * {@code true} iff the last point is in the trajectory is the
	 * last point in the route. 
	 */
	public static boolean _TRAJ_ENDS_AT_ROUTE_END;
	
	//Result
	private Trajectory traj;

	
	public static void main(String[] args) {
		Driver_TS_R_3 driver = new Driver_TS_R_3();
		TrajectorySynthesizer ts = null;
		RouteTrack t = null;
		Route r = null;
		
		driver.TS_R_3(ts, t, r);
	}

	private void TS_R_3(TrajectorySynthesizer trajSynth, RouteTrack track, Route route) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(track);
			Common.assume_TS_R_preconditions_routeTrack(trajSynth, track);
			Common.assume_TS_R_preconditions_route(route);
			Common.r = route;
			traj = trajSynth.getRouteTrajectory(track, route);
			Point4D lastPoint = traj.lastPoint();
			_TRAJ_TRAVELS_MAX_TIME = (lastPoint.getTime() == traj.firstPoint().getTime() +  new UserParameters().tsTimeHorizon);
			if (!_TRAJ_TRAVELS_MAX_TIME) {
				if (route.isEmpty()) {
					_TRAJ_ENDS_AT_ROUTE_END = false;
				} else {
					Fix lastFix = route.lastFix();
					_TRAJ_ENDS_AT_ROUTE_END = (lastFix.getLatitude() == lastPoint.getLatitude() && lastFix.getLongitude() == lastPoint.getLongitude());
				}
			}
			assert (_TRAJ_TRAVELS_MAX_TIME || _TRAJ_ENDS_AT_ROUTE_END); 
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
