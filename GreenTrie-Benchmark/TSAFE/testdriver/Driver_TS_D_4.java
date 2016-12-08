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
 * Driver for TS_D_4 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_D_4 {
	//Atomic propositions
	
	/**
	 * {@code true} iff the angle between the first and the last point 
	 * in the trajectory equals the flight track's heading.
	 */
	public static boolean _TRAJ_ANGLE_IS_TRACK_HEADING = false;
	
	//Result
	private Trajectory traj;

	public static void main(String[] args) {
		Driver_TS_D_4 driver = new Driver_TS_D_4();
		FlightTrack ft = null;
		TrajectorySynthesizer ts = null;
		
		driver.TS_D_4(ft, ts);
	}
	
	private void TS_D_4(FlightTrack flightTrack, TrajectorySynthesizer trajSynth) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(flightTrack);

			traj = trajSynth.getDeadReckoningTrajectory(flightTrack);
			Point4D firstPoint = traj.firstPoint();
			Point4D lastPoint = traj.lastPoint();
			PointXY firstPointXY = trajSynth.calculator.toXY(new Point2D(firstPoint.getLatitude(), firstPoint.getLongitude()));
			PointXY lastPointXY = trajSynth.calculator.toXY(new Point2D(lastPoint.getLatitude(), lastPoint.getLongitude()));
			final double diffX = lastPointXY.getX() - firstPointXY.getX();
			final double diffY = lastPointXY.getY() - firstPointXY.getY();
			final double distance = flightTrack.getSpeed() * new UserParameters().tsTimeHorizon;
			_TRAJ_ANGLE_IS_TRACK_HEADING = ( 
					Math.sin(flightTrack.getHeading()) == diffY / distance &&
					Math.cos(flightTrack.getHeading()) == diffX / distance);
			assert _TRAJ_ANGLE_IS_TRACK_HEADING;
		} catch (AssumptionException e) {
			//does nothing
		}
	}
}
