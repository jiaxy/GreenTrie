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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;


/**
 * The menu bar for the Tsafe client
 */
class TsafeMenu extends JMenuBar implements ItemListener {

	/**
	 * Show submenus
	 */
	public static final String FIXES_TEXT = "Fixes";

	public static final String FLIGHTS_TEXT = "Flights";

	public static final String ROUTES_TEXT = "Routes";

	public static final String TRAJS_TEXT = "Trajectories";

	/**
	 * Show Submenu items
	 */
	public static final String ALL_TEXT = "all";

	public static final String SELECTED_TEXT = "selected";

	public static final String WITH_PLAN_TEXT = "with flight plans";

	public static final String CONFORMING_TEXT = "conforming";

	public static final String BLUNDERING_TEXT = "blundering";

	public static final String NONE_TEXT = "none";

	/**
	 * Parameters Menu items
	 */
	public static final String CONF_MONITOR_TEXT = "Conformance Monitor . . .";

	public static final String TRAJ_SYNTH_TEXT = "Trajectory Synthesizer . . .";

	/**
	 * Tsafe Client
	 */
	private Client_Gui window;

	/**
	 * Submenu Items
	 */
	private JMenuItem fixesAll, fixesNone;

	private JMenuItem flightsAll, flightsSelected, flightsWithPlan,
			flightsConforming, flightsBlundering, flightsNone;

	private JMenuItem routesAll, routesSelected, routesConforming,
			routesBlundering, routesNone;

	private JMenuItem trajsAll, trajsSelected, trajsWithPlan, trajsConforming,
			trajsBlundering, trajsNone;

	/**
	 * Constructs the TsafeMenu
	 */
	public TsafeMenu(Client_Gui window, String initialFixesText,
			String initialFlightsText, String initialRoutesText,
			String initialTrajectoriesText) {
		super();
		this.window = window;
		super.add(makeShowMenu(initialFixesText, initialFlightsText,
				initialRoutesText, initialTrajectoriesText));
		super.add(makeParametersMenu(window));
	}

	/**
	 * Handles the menu events
	 */
	public void itemStateChanged(ItemEvent e) {
		Object item = e.getItem();
		String text = ((JMenuItem) e.getItem()).getText();

		if (item.equals(fixesAll) || item.equals(fixesNone)) {
			window.showMenuChanged(FIXES_TEXT, text);
		} else if (item.equals(flightsAll) || item.equals(flightsSelected)
				|| item.equals(flightsWithPlan)
				|| item.equals(flightsConforming)
				|| item.equals(flightsBlundering) || item.equals(flightsNone)) {
			window.showMenuChanged(FLIGHTS_TEXT, text);
		} else if (item.equals(routesAll) || item.equals(routesSelected)
				|| item.equals(routesConforming)
				|| item.equals(routesBlundering) || item.equals(routesNone)) {
			window.showMenuChanged(ROUTES_TEXT, text);
		} else if (item.equals(trajsAll) || item.equals(trajsSelected)
				|| item.equals(trajsWithPlan) || item.equals(trajsConforming)
				|| item.equals(trajsBlundering) || item.equals(trajsNone)) {
			window.showMenuChanged(TRAJS_TEXT, text);
		} else {
			throw new RuntimeException("Invalid Menu Item Changed");
		}
	}

	/**
	 * Construct the show menu
	 */
	private JMenu makeShowMenu(String initialFixesText,
			String initialFlightsText, String initialRoutesText,
			String initialTrajectoriesText) {
		// Make Show submenus
		JMenu fixes = makeFixesSubmenu(initialFixesText);
		JMenu flights = makeFlightsSubmenu(initialFlightsText);
		JMenu routes = makeRoutesSubmenu(initialRoutesText);
		JMenu trajs = makeTrajectoriesSubmenu(initialTrajectoriesText);

		// Make Show Menu
		JMenu showMenu = new JMenu("Show");
		showMenu.add(fixes);
		showMenu.add(flights);
		showMenu.add(routes);
		showMenu.add(trajs);

		return showMenu;
	}

	/**
	 * Construct the fixes submenu
	 */
	private JMenu makeFixesSubmenu(String initialText) {
		// Make the radio button menu items
		fixesAll = new JRadioButtonMenuItem(ALL_TEXT);
		fixesNone = new JRadioButtonMenuItem(NONE_TEXT);

		// Add thiss to the buttons
		fixesAll.addItemListener(this);
		fixesNone.addItemListener(this);

		// Make a button group out of the buttons
		ButtonGroup bg = new ButtonGroup();
		bg.add(fixesAll);
		bg.add(fixesNone);
		setSelectedButton(bg, initialText);

		// Make the flights submenu
		JMenu fixes = new JMenu(FIXES_TEXT);
		fixes.add(fixesAll);
		fixes.add(fixesNone);
		return fixes;
	}

	/**
	 * Construct the flights submenu
	 */
	private JMenu makeFlightsSubmenu(String initialText) {
		// Make the radio button menu items
		flightsAll = new JRadioButtonMenuItem(ALL_TEXT);
		flightsSelected = new JRadioButtonMenuItem(SELECTED_TEXT);
		flightsWithPlan = new JRadioButtonMenuItem(WITH_PLAN_TEXT);
		flightsConforming = new JRadioButtonMenuItem(CONFORMING_TEXT);
		flightsBlundering = new JRadioButtonMenuItem(BLUNDERING_TEXT);
		flightsNone = new JRadioButtonMenuItem(NONE_TEXT);

		// Add thiss to the buttons
		flightsAll.addItemListener(this);
		flightsSelected.addItemListener(this);
		flightsWithPlan.addItemListener(this);
		flightsConforming.addItemListener(this);
		flightsBlundering.addItemListener(this);
		flightsNone.addItemListener(this);

		// Make a button group out of the buttons
		ButtonGroup bg = new ButtonGroup();
		bg.add(flightsAll);
		bg.add(flightsSelected);
		bg.add(flightsWithPlan);
		bg.add(flightsConforming);
		bg.add(flightsBlundering);
		bg.add(flightsNone);
		setSelectedButton(bg, initialText);

		// Make the flights submenu
		JMenu flights = new JMenu(FLIGHTS_TEXT);
		flights.add(flightsAll);
		flights.add(flightsSelected);
		flights.add(flightsWithPlan);
		flights.add(flightsConforming);
		flights.add(flightsBlundering);
		flights.add(flightsNone);
		return flights;
	}

	/**
	 * Construct the routes submenu
	 */
	private JMenu makeRoutesSubmenu(String initialText) {
		// Make the radio button menu items
		routesAll = new JRadioButtonMenuItem(ALL_TEXT);
		routesSelected = new JRadioButtonMenuItem(SELECTED_TEXT);
		routesConforming = new JRadioButtonMenuItem(CONFORMING_TEXT);
		routesBlundering = new JRadioButtonMenuItem(BLUNDERING_TEXT);
		routesNone = new JRadioButtonMenuItem(NONE_TEXT);

		// Add thiss to the buttons
		routesAll.addItemListener(this);
		routesSelected.addItemListener(this);
		routesConforming.addItemListener(this);
		routesBlundering.addItemListener(this);
		routesNone.addItemListener(this);

		// Make a button group out of the buttons
		ButtonGroup bg = new ButtonGroup();
		bg.add(routesAll);
		bg.add(routesSelected);
		bg.add(routesConforming);
		bg.add(routesBlundering);
		bg.add(routesNone);
		setSelectedButton(bg, initialText);

		// Make the routes submenu
		JMenu routes = new JMenu(ROUTES_TEXT);
		routes.add(routesAll);
		routes.add(routesSelected);
		routes.add(routesConforming);
		routes.add(routesBlundering);
		routes.add(routesNone);
		return routes;
	}

	/**
	 * Construct the trajectories submenu
	 */
	private JMenu makeTrajectoriesSubmenu(String initialText) {
		// Make the radio button menu items
		trajsAll = new JRadioButtonMenuItem(ALL_TEXT);
		trajsSelected = new JRadioButtonMenuItem(SELECTED_TEXT);
		trajsWithPlan = new JRadioButtonMenuItem(WITH_PLAN_TEXT);
		trajsConforming = new JRadioButtonMenuItem(CONFORMING_TEXT);
		trajsBlundering = new JRadioButtonMenuItem(BLUNDERING_TEXT);
		trajsNone = new JRadioButtonMenuItem(NONE_TEXT);

		// Add thiss to the buttons
		trajsAll.addItemListener(this);
		trajsSelected.addItemListener(this);
		trajsWithPlan.addItemListener(this);
		trajsConforming.addItemListener(this);
		trajsBlundering.addItemListener(this);
		trajsNone.addItemListener(this);

		// Make a button group out of the buttons
		ButtonGroup bg = new ButtonGroup();
		bg.add(trajsAll);
		bg.add(trajsSelected);
		bg.add(trajsWithPlan);
		bg.add(trajsConforming);
		bg.add(trajsBlundering);
		bg.add(trajsNone);
		setSelectedButton(bg, initialText);

		// Make the trajectories submenu
		JMenu trajectories = new JMenu(TRAJS_TEXT);
		trajectories.add(trajsAll);
		trajectories.add(trajsSelected);
		trajectories.add(trajsWithPlan);
		trajectories.add(trajsConforming);
		trajectories.add(trajsBlundering);
		trajectories.add(trajsNone);
		return trajectories;
	}

	/**
	 * Selects the button with the given text in the button group
	 */
	private static void setSelectedButton(ButtonGroup bg, String text) {
		Enumeration buttonEnum = bg.getElements();

		while (buttonEnum.hasMoreElements()) {
			AbstractButton button = (AbstractButton) buttonEnum.nextElement();
			if (button.getText().equals(text)) {
				button.setSelected(true);
				return;
			}
		}

		throw new RuntimeException("Selected button not found: " + text);
	}

	/**
	 * Construct the parameters menu
	 */
	private static JMenu makeParametersMenu(Client_Gui window) {
		// Make Preferences Menu Items
		JMenuItem confMonitor = new JMenuItem(CONF_MONITOR_TEXT);
		JMenuItem trajSynth = new JMenuItem(TRAJ_SYNTH_TEXT);

		// Add Preferences Menu Item Listeners
		confMonitor.addActionListener(window);
		trajSynth.addActionListener(window);

		// Make Preferences Menu
		JMenu prefMenu = new JMenu("Parameters");
		prefMenu.add(confMonitor);
		prefMenu.add(trajSynth);

		return prefMenu;
	}

}