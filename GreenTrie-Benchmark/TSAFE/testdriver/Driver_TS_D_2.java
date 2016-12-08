package testdriver;

import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.Point2D;
import tsafe.common_datastructures.Point4D;
import tsafe.common_datastructures.PointXY;
import tsafe.common_datastructures.Route;
import tsafe.common_datastructures.Trajectory;
import tsafe.server.calculation.Calculator;
import tsafe.server.computation.data.RouteTrack;
import tsafe.server.computation.sub_computation.RouteTracker;
import tsafe.server.computation.sub_computation.TrajectorySynthesizer;

/**
 * Driver for TS_D_2 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_D_2 {
	//Atomic propositions
	
	/** {@code true} iff the trajectory has the flight track as a first point. */
	public static boolean _TRAJ_STARTS_WITH_TRACK = false;
	
	//Result
	private Trajectory traj;

	public static void main(String[] args) {
		Driver_TS_D_2 driver = new Driver_TS_D_2();
		FlightTrack ft = null;
		TrajectorySynthesizer ts = null;
		
		driver.TS_D_2(ft, ts);
	}
	
	private void TS_D_2(FlightTrack flightTrack, TrajectorySynthesizer trajSynth) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(flightTrack);

			traj = trajSynth.getDeadReckoningTrajectory(flightTrack);
			Point4D firstPoint = traj.firstPoint();
			_TRAJ_STARTS_WITH_TRACK = (
					firstPoint.getLatitude() == flightTrack.getLatitude() && 
					firstPoint.getLongitude() == flightTrack.getLongitude() &&
					firstPoint.getAltitude() == flightTrack.getAltitude() &&
					firstPoint.getTime() == flightTrack.getTime());
			assert _TRAJ_STARTS_WITH_TRACK;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
