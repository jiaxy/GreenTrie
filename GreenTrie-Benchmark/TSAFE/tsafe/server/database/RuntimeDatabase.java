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

package tsafe.server.database;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tsafe.common_datastructures.Airway;
import tsafe.common_datastructures.Fix;
import tsafe.common_datastructures.Flight;
import tsafe.common_datastructures.FlightPlan;
import tsafe.common_datastructures.FlightTrack;
import tsafe.common_datastructures.LatLonBounds;
import tsafe.common_datastructures.Sid;
import tsafe.common_datastructures.Star;

/**
 * The database of air traffic control information. Because this database
 * manages runtime objects, it filters out data that it finds to be unneccessary
 * so as to avoid OutOfMemory errors.
 */
public class RuntimeDatabase extends DatabaseInterface {
	// In memory database tables
	private Map flights = new HashMap();

	private Map fixes = new HashMap();

	private Map airways = new HashMap();

	private Map sids = new HashMap();

	private Map stars = new HashMap();

	/**
	 * RuntimeDatabase constructor
	 */
	public RuntimeDatabase(LatLonBounds bounds) {
		super(bounds);
		
	}

	// ****************************
	// ***** Managing Flights *****
	// ****************************

	/**
	 * Inserts flights to the database
	 */
	
	
    // Flights
    public final void insertFlight(Flight f) {
        FlightTrack ft = f.getFlightTrack();

        boolean inBounds = ft != null &&
                bounds.contains(ft.getLatitude(), ft.getLongitude());
        
        insertFlight(f, inBounds);
    }
	
    protected synchronized void insertFlight(Flight f, boolean inBounds) {
        FlightPlan fp = f.getFlightPlan();
        boolean routeInBounds = fp != null && super.routeInBounds(fp.getRoute());
        
        // Excludes noncommercial flights? if (id.charAt(0) == 'N') return;
        
        // If the track or the plan is in bounds, add it as is
        if (inBounds || routeInBounds) {
            flights.put(f.getAircraftId(), f);
        }
      
        // If neither are in bounds, ignore the flight
    }

	public synchronized void updateFlight(Flight f) {
		deleteFlight(f.getAircraftId());
		insertFlight(f);
	}

	public synchronized void deleteFlight(String aircraftId) {
		delete(flights, aircraftId);
	}

	public synchronized Flight selectFlight(String aircraftId) {
		Object selected = select(flights, aircraftId);
		return selected == null ? null : new Flight((Flight) selected);
	}
	
	
    public synchronized Collection selectFlightsInBounds() {
        Set deepCopyFlights = new HashSet();
        Iterator flightIter = flights.values().iterator();

        while(flightIter.hasNext()) {
            Flight f = (Flight)flightIter.next();
            if (f.getFlightTrack() != null)
            deepCopyFlights.add(new Flight(f));
        }
        return deepCopyFlights;

    }
    
    
	// **************************
	// ***** Managing Fixes *****
	// **************************

	public synchronized void insertFix(Fix fix) {
		insert(fixes, fix.getId(), fix);
	}

	public synchronized void deleteFix(String fixId) {
		delete(fixes, fixId);
	}

	public synchronized Fix selectFix(String fixId) {
		return (Fix) select(fixes, fixId);
	}

	public synchronized Collection selectFixesInBounds() {
		return selectInBounds(fixes);
	}

	// ****************************
	// ***** Managing Airways *****
	// ****************************

	public synchronized void insertAirway(Airway awy) {
		insert(airways, awy.getId(), awy);
	}

	public synchronized void deleteAirway(String awyId) {
		delete(airways, awyId);
	}

	public synchronized Airway selectAirway(String airwayId) {
		return (Airway) select(airways, airwayId);
	}

	public synchronized Collection selectAirwaysInBounds() {
		return selectInBounds(airways);
	}

	// ****************************
	// ***** Managing Sids *****
	// ****************************

	public synchronized void insertSid(Sid sid) {
		insert(sids, sid.getId(), sid);
	}

	public synchronized void deleteSid(String sidId) {
		delete(sids, sidId);
	}

	public synchronized Sid selectSid(String sidId) {
		return (Sid) select(sids, sidId);
	}

	public synchronized Collection selectSidsInBounds() {
		return selectInBounds(sids);
	}

	// ****************************
	// ***** Managing Stars *****
	// ****************************

	public synchronized void insertStar(Star star) {
		insert(stars, star.getId(), star);
	}

	public synchronized void deleteStar(String starId) {
		delete(stars, starId);
	}

	public synchronized Star selectStar(String starId) {
		return (Star) select(stars, starId);
	}

	public synchronized Collection selectStarsInBounds() {
		return selectInBounds(stars);
	}

	// ***************************
	// ***** Helpers Methods *****
	// ***************************

	private void insert(Map inMap, String id, Object data) {
		inMap.put(id, data);
	}

	private void delete(Map inMap, String id) {
		inMap.remove(id);
	}

	private Object select(Map inMap, String id) {
		Object data = inMap.get(id);
		return data ;
	}

	private Collection selectInBounds(Map inBounds) {
		return Collections.unmodifiableCollection(inBounds.values());
	}
}