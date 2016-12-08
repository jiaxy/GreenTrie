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
 * Drivers for TS_R_4 properties.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_R_4 {
	//Atomic propositions
	/** 
	 * {@code true} iff the altitude of the points in the
	 * trajectory equals the route track's altitude.
	 */	
	public static boolean _TRAJ_HAS_TRACK_ALTITUDE;

	//Result
	private Trajectory traj;

	
	public static void main(String[] args) {
		Driver_TS_R_4 driver = new Driver_TS_R_4();
		TrajectorySynthesizer ts = null;
		RouteTrack t = null;
		Route r = null;
		
		driver.TS_R_4(ts, t, r);
	}

	private void TS_R_4(TrajectorySynthesizer trajSynth, RouteTrack track, Route route) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(track);
			Common.assume_TS_R_preconditions_routeTrack(trajSynth, track);
			Common.assume_TS_R_preconditions_route(route);
			Common.r = route;
			traj = trajSynth.getRouteTrajectory(track, route);
			@SuppressWarnings("unchecked")
			List<Point4D> l = traj.pointList();
			for (Point4D p : l) { 
				_TRAJ_HAS_TRACK_ALTITUDE = (p.getAltitude() == track.getAltitude());
				if (!_TRAJ_HAS_TRACK_ALTITUDE) { break; } //accelerates calculation
			}
			assert _TRAJ_HAS_TRACK_ALTITUDE;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
