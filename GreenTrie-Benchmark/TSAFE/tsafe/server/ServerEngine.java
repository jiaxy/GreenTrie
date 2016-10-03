package tsafe.server;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Timer;

import tsafe.client.ClientEngine;
import tsafe.common_datastructures.Flight;
import tsafe.common_datastructures.FlightPlan;
import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.LatLonBounds;
import tsafe.common_datastructures.TSAFEProperties;
import tsafe.common_datastructures.Trajectory;
import tsafe.common_datastructures.communication.ComputationResults;
import tsafe.common_datastructures.communication.UserParameters;
import tsafe.server.computation.Calculator;
import tsafe.server.computation.ConformanceMonitor;
import tsafe.server.computation.RouteTrack;
import tsafe.server.computation.RouteTracker;
import tsafe.server.computation.TrajectorySynthesizer;
import tsafe.server.database.DatabaseInterface;
import tsafe.server.database.RuntimeDatabase;
import tsafe.server.parser.ParserInterface;
import tsafe.server.parser.asdi.ASDIParser;
import tsafe.server.server_gui.ConfigConsole;
import tsafe.server.server_gui.SplashScreen;
import tsafe.server.server_gui.utils.WaitCursorEventQueue;

/**
 * @author Christopher Ackermann
 * 
 * Manages the communication between the logical components of the server
 * component. It also provides an interface for the communication with the
 * client component.
 */
public class ServerEngine implements ActionListener {

	/**
	 * The object of the ServerInterface is needed in order to communicate with
	 * the clients.
	 */


	/**
	 * List of all graphicaloutputs that have subscribed to the subject.
	 */
	private Vector clients;

	/**
	 * Timer that triggers a repaint call
	 */
	private Timer timer;

	/**
	 * Initial time between successive repaints
	 */
	private static final int REPAINT_STEP = 3000;

	/**
	 * Handle to the computation component
	 */

	private RouteTracker routeTracker;
	private ConformanceMonitor confMonitor;
	private TrajectorySynthesizer trajSynth;
	
	/**
	 * Collection of flights over which algorithms calculate
	 */
	private Collection flights = new LinkedList();

	/**
	 * Collections of observers of the engine
	 */
	private Collection observers = new LinkedList();
	
	

	/**
	 * Handle to the computation component
	 */
	private DatabaseInterface database;

	public ServerEngine() {

		// Create a new Vector object for the clients.
		clients = new Vector();
		
		// Create a timer to periodically repaint the flight map
		this.timer = new Timer(REPAINT_STEP, this);

		//this.launchTsafe();
		// Shows the configuration console.
		ConfigConsole console = new ConfigConsole(this);
	}
	
	
	/**
	 * Attach the new client to the list of observers.
	 * 
	 * @param newClient
	 *            Object of
	 */
	public void attachObserver(ClientEngine newClienEngine) {
		this.clients.add(newClienEngine);
	}

	/**
	 * Deletes the client object from the list of the observer.
	 * 
	 * @param newClient
	 *            Object of the client interface.
	 */
	public void detachObserver(ClientEngine newClienEngine) {
		this.clients.remove(newClienEngine);
	}

	/**
	 * Notify the clients for an update.
	 */
	public void notifyObservers() {

		Iterator clientIterator = this.clients.iterator();

		while (clientIterator.hasNext()) {
			ClientEngine client = (ClientEngine) clientIterator.next();
			client.notifyClient();
		}
	}
	
	


	/**
	 * Creates instances of the interface classes of each component in the
	 * server.
	 * 
	 * @return A list of errors that occured while reading the properties file.
	 */
	private List launchTsafe(LatLonBounds bounds) {

		List errorMessages = new Vector();
		
		//Make the database.
		this.database = new RuntimeDatabase(bounds);

		// Make the Engine Calculator.
		Calculator calculator = new Calculator();

		// Retrieve the data files
		String[] dataFiles = TSAFEProperties.getDataFiles();

		// Make the feed reader.
		Reader feedReader = TSAFEProperties.getFeedSource();

		// Make the feed parser.
		ParserInterface feedParser = new ASDIParser(feedReader, this.database, calculator);

		feedParser.readStaticData(dataFiles);

		// Make the Tsafe Engine.

		this.routeTracker = new RouteTracker(this, calculator, bounds);
		this.confMonitor = new ConformanceMonitor(this, calculator, bounds);
		this.trajSynth = new TrajectorySynthesizer(this, calculator, bounds);
		
		// Start parsing the dynamic feed source
		feedParser.startParsing();

		// Start the timer that runs the program
		timer.start();
		return errorMessages;
	}

	/**
	 * Timer event handler When timer goes off, notify the clients
	 */
	public void actionPerformed(ActionEvent e) {
		// The timer has gone off
		if (e.getActionCommand() == null) {
			notifyObservers();
		}
	}

	/**
	 * Manages the flight data of a client.
	 */

	

	/**
	 * Manages the flight data of a client and 
	 *
	 * Runs the TSAFE Engine Performs Conformance Monitoring and Trajectory
	 * Synthesis
	 * 
	 * @return
	 **************************************************************************/
	public ComputationResults computeFlights(UserParameters parameters) {

		confMonitor.setParameters(parameters);
		routeTracker.setParameters(parameters);
		trajSynth.setParameters(parameters);
		
		//Query the database for flight in bounds, parse the flight list to the
		// computation component and start it
		
		flights = this.database.selectFlightsInBounds();


		// Instantiate an empty collection of blunders and an empty flight2traj map

		Collection blunders = new LinkedList();
		Map flight2TrajMap = new HashMap();

		// For each flight:
		// 1) If it has no flight plan, assign it a dr traj and continue
		// 2) If it has a flight plan, determine if it is blundering
		// 3) If it is, assign its dr trajectory as its predicted trajectory
		//    If it isn't, assign its route trajectory as its predicted trajectory
		Iterator flightIter = flights.iterator(); 


		while (flightIter.hasNext()) {
			Flight flight = (Flight) flightIter.next();
			FlightTrack ft = flight.getFlightTrack();
			
			FlightPlan fp = flight.getFlightPlan();

			// If the flight doesn't have a flight plan, assign it a dr
			// trajectory
			// Don't check its conformance, just continue to the next flight
			if (fp == null) {
				Trajectory drTraj = trajSynth.getDeadReckoningTrajectory(ft);
				flight2TrajMap.put(flight, drTraj);
				continue;
			}

			// Determine if flight is blundering by comparing its actual track
			// to its route track
			RouteTrack rt = routeTracker.findRouteTrack(ft, fp);
			boolean blundering = confMonitor.isBlundering(ft, rt);

			// If the flight is bludering, add it to the set of blunders
			// and assign a dead reckoning trajectory as its assigned trajectory
			if (blundering) {
				blunders.add(flight);

				Trajectory drTraj = trajSynth.getDeadReckoningTrajectory(ft);
				flight2TrajMap.put(flight, drTraj);
			}
			// If the flight is conforming, synthesize a route trajectory for
			// it,
			// assuming it's current track is its route track
			else {
				Trajectory rtTraj = trajSynth.getRouteTrajectory(rt, fp
						.getRoute());
				flight2TrajMap.put(flight, rtTraj);
			}
		}

		// Notify the observers of the results
		ComputationResults results = new ComputationResults(flights, blunders,
				flight2TrajMap);

		return results;
	}
	
	
	
	public Collection getFixes() {
		return this.database.selectFixesInBounds();
	}
	
	public void displayClient(LatLonBounds bounds) {

		Iterator clientIterator = this.clients.iterator();

		while (clientIterator.hasNext()) {
			ClientEngine client = (ClientEngine) clientIterator.next();
			client.setBounds(bounds);
			client.run();
		}
	}
	
	
	/*
	 * Method necessary for the communication to the client while launchin
	 * Tsafe.
	 */

	public void startTsafe(LatLonBounds bounds) {
		this.launchTsafe(bounds);
		displayClient(bounds);
		timer.start();
	}
	
	
	//
	// MAIN METHOD
	//

	//-------------------------------------------
	public static void main(String args[]) {

		ServerEngine engine;
		
		long hideSplashTime = 0;
		
		// CASE #1: Startup using splash screen.
		if (TSAFEProperties.getShowSplashScreenFlag()) {

			// Show the splash screen (for a minimum of 3.3 seconds).
			hideSplashTime = System.currentTimeMillis() + 3300;
			SplashScreen.show();
		}

			// Start the server.
			engine = new ServerEngine();
			
			// Create the clients
			ClientEngine client = new ClientEngine(engine);
			engine.attachObserver(client);
        

		if (TSAFEProperties.getShowSplashScreenFlag()) {

			// Wait for time to expire.
			while (System.currentTimeMillis() < hideSplashTime) {
			}
		
			// Show the console.
			SplashScreen.hide();
		}
		
		

		// Install the Event Queue decorator to automatically change the cursor
		// to an hourglass if a GUI-event takes too much time (exceeds 5
		// seconds).
		EventQueue waitQueue = new WaitCursorEventQueue(500);
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);
	}

	
	
	
	
	
	
	
	
	
}