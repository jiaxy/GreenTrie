package testdriver;

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
 * Drivers for TS_R_1 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_R_1 {
	//Atomic propositions
	
	/** {@code true} iff the first point in the trajectory is the flight track. */
	public static boolean _TRAJ_STARTS_WITH_TRACK = false;

	//Result
	private Trajectory traj;
	
	public static void main(String[] args) {
		Driver_TS_R_1 driver = new Driver_TS_R_1();
		TrajectorySynthesizer ts = null;
		RouteTrack t = null;
		Route r = null;
		
		driver.TS_R_1(ts, t, r);
	}

	private void TS_R_1(TrajectorySynthesizer trajSynth, RouteTrack track, Route route) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(track);
			Common.assume_TS_R_preconditions_routeTrack(trajSynth, track);
			Common.assume_TS_R_preconditions_route(route);
			traj = trajSynth.getRouteTrajectory(track, route);
			Point4D firstPoint = traj.firstPoint();
			_TRAJ_STARTS_WITH_TRACK = (
					firstPoint.getLatitude() == track.getLatitude() && 
					firstPoint.getLongitude() == track.getLongitude() &&
					firstPoint.getAltitude() == track.getAltitude() &&
					firstPoint.getTime() == track.getTime());
			assert _TRAJ_STARTS_WITH_TRACK;
		} catch (AssumptionException e) {
			//does nothing
		}
}	
}
