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
 * Driver for TS_D_3 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_D_3 {
	//Atomic propositions

	/** 
	 * {@code true} iff the time difference between the first and last point 
	 * in the trajectory is the time horizon. 
	 */
	public static boolean _TRAJ_TRAVELS_MAX_TIME = false;

	//Result
	private Trajectory traj;

	public static void main(String[] args) {
		Driver_TS_D_3 driver = new Driver_TS_D_3();
		FlightTrack ft = null;
		TrajectorySynthesizer ts = null;
		
		driver.TS_D_3(ft, ts);
	}
	
	private void TS_D_3(FlightTrack flightTrack, TrajectorySynthesizer trajSynth) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(flightTrack);

			traj = trajSynth.getDeadReckoningTrajectory(flightTrack);
			Point4D lastPoint = traj.lastPoint();
			_TRAJ_TRAVELS_MAX_TIME = 
					(lastPoint.getTime() == flightTrack.getTime() + 
					new UserParameters().tsTimeHorizon);
			assert _TRAJ_TRAVELS_MAX_TIME;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
