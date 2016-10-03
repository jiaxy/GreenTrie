/*
 TSAFE Prototype: A decision support tool for air traffic controllers
 Copyright (C) 2003  Gregory D. Dennis

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tsafe.client;

import java.util.Collection;

import tsafe.common_datastructures.LatLonBounds;
import tsafe.common_datastructures.TSAFEProperties;
import tsafe.common_datastructures.communication.ComputationResults;
import tsafe.common_datastructures.communication.UserParameters;
import tsafe.server.ServerEngine;

/**
 * Main executable class
 */
public class ClientEngine extends Thread  {

	//	*** Interface attributes **************************************
	/**
	 * Handle to the interface class of the server, necessary for the
	 * communication between client and server.
	 */
	protected ServerEngine server;

	/**
	 * The bounds for the area within the client is supposed to show the
	 * flights.
	 */
	protected LatLonBounds bounds;

	/**
	 * Stores the parameters such as thresholds that can be changed by the user
	 * and that are used for calculating the flights.
	 */
	protected UserParameters parameters;
	
	/**
	 * The graphical main user interface
	 */
	private Client_Gui window;


	/**
	 * Construct a GraphicalClient
	 */
	public ClientEngine(ServerEngine server) {
		this.server = server;
		this.server = server;
		this.parameters = new UserParameters();

		bounds = TSAFEProperties.getLatLonBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		this.window = new Client_Gui(this);
		this.window.startWindow();
	}
	

	/**
	 * Recieves the notification from the client after the timer goes off.
	 * 
	 * @see tsafe.client.ClientInterface#notifyClient()
	 */
	public void notifyClient() {
		// Get the new flight data after the notification.
		this.getFlightData();
	}
	
	/**
	 * Queries the server for new flight data.
	 * 
	 * @see tsafe.client.ClientInterface#getFlightData()
	 */
	public void getFlightData() {

		
		ComputationResults results = this.server.computeFlights(this.parameters);
		updateClient(results);

	}
	

	/**
	 * Queries the server for new flight data.
	 * 
	 * @see tsafe.client.ClientInterface#getFlightData()
	 */
	public void updateClient(ComputationResults results) {
		this.window.updateWindow(results);
	}

	/**
	 * @return Returns the parameters.
	 */
	public UserParameters getParameters() {
		return parameters;
	}

	/**
	 * Interface method for the client for reading the fixes.
	 * 
	 * @return All fixes, stored in the database.
	 */
	public Collection getFixes() {
		return this.server.getFixes();
	}

	/**
	 * @return Returns the server.
	 */
	public ServerEngine getServer() {
		return server;
	}

	/**
	 * @param server
	 *            The server to set.
	 */
	public void setServer(ServerEngine server) {
		this.server = server;
	}

	/**
	 * @return Returns the bounds.
	 */
	public LatLonBounds getBounds() {
		return bounds;
	}
		
	/**
	 * Sets the bounds 
	 */
	public void setBounds(LatLonBounds bounds) {
		this.bounds = bounds;
	}
	

	/*public void testing() {
		ClientInterface client = new GraphicalClient(this.server);
		client.setBounds(bounds);
		client.run();
	}*/
}