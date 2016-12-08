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
 * Driver for TS_D_6 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_D_6 {
	//Atomic propositions
	/**
	 * {@code true} iff the distance between the first and last point 
	 * in the trajectory equals the flight track's speed times the time horizon.
	 */
	public static boolean _TRAJ_TRAVELS_MAX_DISTANCE = false;
	
	//Result
	private Trajectory traj;

	public static void main(String[] args) {
		Driver_TS_D_6 driver = new Driver_TS_D_6();
		FlightTrack ft = null;
		TrajectorySynthesizer ts = null;
		
		driver.TS_D_6(ft, ts);
	}
		
	private void TS_D_6(FlightTrack flightTrack, TrajectorySynthesizer trajSynth) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(flightTrack);

			traj = trajSynth.getDeadReckoningTrajectory(flightTrack);
			final Point4D firstPoint = traj.firstPoint();
			final Point4D lastPoint = traj.lastPoint();
			final double distance = trajSynth.calculator.distanceLL(new Point2D(firstPoint.getLatitude(), firstPoint.getLongitude()), new Point2D(lastPoint.getLatitude(), lastPoint.getLongitude()));
			_TRAJ_TRAVELS_MAX_DISTANCE = 
					(distance == flightTrack.getSpeed() * new UserParameters().tsTimeHorizon);
			assert _TRAJ_TRAVELS_MAX_DISTANCE;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
