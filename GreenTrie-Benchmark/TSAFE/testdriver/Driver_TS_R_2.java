package testdriver;

import java.util.Iterator;

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

import tsafe_jpf.AssumptionException;
import tsafe_jpf.data.Fix;
import tsafe_jpf.data.Point4D;
import tsafe_jpf.data.Route;
import tsafe_jpf.data.Trajectory;

/**
 * Drivers for TS_R_2 property.
 * 
 * @author Pietro Braione
 *
 */
public class Driver_TS_R_2 {
	//Atomic propositions
	
	/** 
	 * {@code true} iff all the points in the trajectory but the first and the
	 * last one are consecutive point in the route. 
	 */
	public static boolean _TRAJ_INTERNAL_POINTS_ARE_IN_ROUTE = false;
	
	//Other instrumentation variables

	//Result
	private Trajectory traj;

	
	public static void main(String[] args) {
		Driver_TS_R_2 driver = new Driver_TS_R_2();
		TrajectorySynthesizer ts = null;
		RouteTrack t = null;
		Route r = null;
		
		driver.TS_R_2(ts, t, r);
	}

	private void TS_R_2(TrajectorySynthesizer trajSynth, RouteTrack track, Route route) {
		try {
			Common.assume_TS_preconditions_trajSynth(trajSynth);
			Common.assume_TS_preconditions_flightTrack(track);
			Common.assume_TS_R_preconditions_routeTrack(trajSynth, track);
			Common.assume_TS_R_preconditions_route(route);
			Common.r = route;
			traj = trajSynth.getRouteTrajectory(track, route);
			@SuppressWarnings("unchecked")
			Iterator<Fix> routeIterator = route.fixIterator();
			if (routeIterator.hasNext()) {
				while (!routeIterator.next().equals(track.getNextFix())) 
					; //skips
				//eventually ends here because by TS_R preconditions
				//routeTrack.next belongs to route

				@SuppressWarnings("unchecked")
				Iterator<Point4D> trajIterator = traj.pointIterator();
				trajIterator.next();
				final Point4D trajLast = traj.lastPoint();
				_TRAJ_INTERNAL_POINTS_ARE_IN_ROUTE = true; //trivially if traj has no internal points
				boolean atSecond = true;
				while (trajIterator.hasNext()) {
					Point4D p = trajIterator.next();
					Fix f;
					if (p == trajLast) {
						break; //skips last point
					} else if (atSecond) {
						f = track.getNextFix(); //second fix in route is routeTrack.nextFix
						atSecond = false;
					} else {
						f = routeIterator.next();
					}
					_TRAJ_INTERNAL_POINTS_ARE_IN_ROUTE = (p.getLatitude() == f.getLatitude() && p.getLongitude() == f.getLongitude());
					if (! _TRAJ_INTERNAL_POINTS_ARE_IN_ROUTE) { break; } //acceleration
				}
			} else {
				//no route == dead reckoning trajectory; the check reduces to verifying
				//that the trajectory has 2 points
				_TRAJ_INTERNAL_POINTS_ARE_IN_ROUTE = (traj.pointList().size() == 2);
			}
			assert _TRAJ_INTERNAL_POINTS_ARE_IN_ROUTE;
		} catch (AssumptionException e) {
			//does nothing
		}
	}	
}
