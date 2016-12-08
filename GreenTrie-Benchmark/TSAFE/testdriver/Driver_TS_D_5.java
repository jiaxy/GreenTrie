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
 * Driver for TS_D_5 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_D_5 {
	//Atomic propositions
	
	/** 
	 * {@code true} iff the altitude of the points in the
	 * trajectory equals the flight track's altitude.
	 */
	public static boolean _TRAJ_HAS_TRACK_ALTITUDE = false;
	
	//Result
	private Trajectory traj;

	public static void main(String[] args) {
		Driver_TS_D_5 driver = new Driver_TS_D_5();
		FlightTrack ft = null;
		TrajectorySynthesizer ts = null;
		
		driver.TS_D_5(ft, ts);
	}
	
	private void TS_D_5(FlightTrack flightTrack, TrajectorySynthesizer trajSynth) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(flightTrack);

			traj = trajSynth.getDeadReckoningTrajectory(flightTrack);
			final Point4D lastPoint = traj.lastPoint();
			_TRAJ_HAS_TRACK_ALTITUDE = 
					(lastPoint.getAltitude() == flightTrack.getAltitude());
			assert _TRAJ_HAS_TRACK_ALTITUDE;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
