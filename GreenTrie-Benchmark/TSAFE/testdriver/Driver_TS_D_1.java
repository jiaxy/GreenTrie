package testdriver;

import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.Point2D;
import tsafe.common_datastructures.PointXY;
import tsafe.common_datastructures.Route;
import tsafe.common_datastructures.Trajectory;
import tsafe.server.calculation.Calculator;
import tsafe.server.computation.data.RouteTrack;
import tsafe.server.computation.sub_computation.RouteTracker;
import tsafe.server.computation.sub_computation.TrajectorySynthesizer;

/**
 * Driver for TS_D_1 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_D_1 {
	//Atomic propositions
	
	/** {@code true} iff the trajectory has two points. */
	public static boolean _TRAJ_HAS_TWO_POINTS = false;
	
	//Result
	private Trajectory traj;

	public static void main(String[] args) {
		Driver_TS_D_1 driver = new Driver_TS_D_1();
		FlightTrack ft = null;
		TrajectorySynthesizer ts = null;
		driver.TS_D_1(ft, ts);
	}
	
	private void TS_D_1(FlightTrack flightTrack, TrajectorySynthesizer trajSynth) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(flightTrack);

			traj = trajSynth.getDeadReckoningTrajectory(flightTrack);
			_TRAJ_HAS_TWO_POINTS = (traj.pointList().size() == 2); //works because traj.pointList() is concrete (call to Analysis.force is redundant)
			assert _TRAJ_HAS_TWO_POINTS;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
