package testdriver;

import java.util.List;
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
 * Drivers for TS_R_5 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_R_5 {
	//Atomic propositions
	/**
	 * {@code true} iff the times of the points along the trajectory
	 * increase by the route track's speed times the distance from
	 * its predecessor.
	 */
	public static boolean _TRAJ_TIMES_MATCH_SPEED_AND_DISTANCE;

	//Result
	private Trajectory traj;

	
	public static void main(String[] args) {
		Driver_TS_R_5 driver = new Driver_TS_R_5();
		TrajectorySynthesizer ts = null;
		RouteTrack t = null;
		Route r = null;
		
		driver.TS_R_5(ts, t, r);
	}

	private void TS_R_5(TrajectorySynthesizer trajSynth, RouteTrack track, Route route) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(track);
			Common.assume_TS_R_preconditions_routeTrack(trajSynth, track);
			Common.assume_TS_R_preconditions_route(route);
			Common.r = route;
			traj = trajSynth.getRouteTrajectory(track, route);
			@SuppressWarnings("unchecked")
			List<Point4D> l = traj.pointList();
			boolean firstPassed = false;
			Point4D p_prev = null;
			for (Point4D p : l) { 
				if (firstPassed) {
					_TRAJ_TIMES_MATCH_SPEED_AND_DISTANCE = 
							(trajSynth.calculator.distanceLL(new Point2D(p_prev.getLatitude(), p_prev.getLongitude()), new Point2D(p.getLatitude(), p.getLongitude())) == (p.getTime() - p_prev.getTime()) * track.getSpeed());				
					assert _TRAJ_TIMES_MATCH_SPEED_AND_DISTANCE;
				} else {
					firstPassed = true;
				}
				p_prev = p;
			}
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
