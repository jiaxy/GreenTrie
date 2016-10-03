include ./env.mk

default: all

all: main

run: all
	java -Xmx134217728 -classpath '$(CLASSPATH)' tsafe.server.ServerEngine

######################################
## All classes from Tsafe
## Add new ones only here
######################################
OBJ = 	tsafe/client/ClientEngine.class \
	tsafe/client/Client_Gui.class \
	tsafe/client/FlightList.class \
	tsafe/client/FlightMap.class \
	tsafe/client/ParametersDialog.class \
	tsafe/client/TsafeMenu.class \
	tsafe/common_datastructures/Airway.class \
	tsafe/common_datastructures/communication/ComputationResults.class \
	tsafe/common_datastructures/communication/UserParameters.class \
	tsafe/common_datastructures/Fix.class \
	tsafe/common_datastructures/Flight.class \
	tsafe/common_datastructures/FlightPlan.class \
	tsafe/common_datastructures/FlightTrack.class \
	tsafe/common_datastructures/LatLonBounds.class \
	tsafe/common_datastructures/Point2D.class \
	tsafe/common_datastructures/Point4D.class \
	tsafe/common_datastructures/PointXY.class \
	tsafe/common_datastructures/Route.class \
	tsafe/common_datastructures/Sid.class \
	tsafe/common_datastructures/Star.class \
	tsafe/common_datastructures/Trajectory.class \
	tsafe/common_datastructures/TSAFEProperties.class \
	tsafe/common_datastructures/TSAFEResourceAnchor.class \
	tsafe/server/computation/Calculator.class \
	tsafe/server/computation/ConformanceMonitor.class \
	tsafe/server/computation/RouteTrack.class \
	tsafe/server/computation/RouteTracker.class \
	tsafe/server/computation/TrajectorySynthesizer.class \
	tsafe/server/database/DatabaseInterface.class \
	tsafe/server/database/RuntimeDatabase.class \
	tsafe/server/parser/asdi/ASDIParser.class \
	tsafe/server/parser/asdi/FixParser.class \
	tsafe/server/parser/asdi/Message.class \
	tsafe/server/parser/asdi/MessageExtractor.class \
	tsafe/server/parser/asdi/NASFields.class \
	tsafe/server/parser/asdi/RouteParser.class \
	tsafe/server/parser/asdi/ServerReader.class \
	tsafe/server/parser/ParserInterface.class \
	tsafe/server/ServerEngine.class \
	tsafe/server/server_gui/AboutDialog.class \
	tsafe/server/server_gui/ConfigConsole.class \
	tsafe/server/server_gui/ConfigPanel.class \
	tsafe/server/server_gui/DataSourcesConfigPanel.class \
	tsafe/server/server_gui/feedsourcechooser/AirlineFilterPanel.class \
	tsafe/server/server_gui/feedsourcechooser/FacilityFilterPanel.class \
	tsafe/server/server_gui/feedsourcechooser/FeedSourceChooserDialog.class \
	tsafe/server/server_gui/feedsourcechooser/FIGFileChooser.class \
	tsafe/server/server_gui/feedsourcechooser/FIGFileContentFilterDialog.class \
	tsafe/server/server_gui/feedsourcechooser/FIGFileSourceDialog.class \
	tsafe/server/server_gui/feedsourcechooser/FilterPanel.class \
	tsafe/server/server_gui/feedsourcechooser/MessageTypeFilterPanel.class \
	tsafe/server/server_gui/feedsourcechooser/OtherFilterPanel.class \
	tsafe/server/server_gui/feedsourcechooser/ServerSourceDialog.class \
	tsafe/server/server_gui/MapConfigPanel.class \
	tsafe/server/server_gui/preferences/AirlinePreferencePanel.class \
	tsafe/server/server_gui/preferences/ContentFilterPreferencePanel.class \
	tsafe/server/server_gui/preferences/FacilityPreferencePanel.class \
	tsafe/server/server_gui/preferences/FeedSourcePreferencePanel.class \
	tsafe/server/server_gui/preferences/GUIPreferencePanel.class \
	tsafe/server/server_gui/preferences/MessageTypePreferencePanel.class \
	tsafe/server/server_gui/preferences/PreferencePanel.class \
	tsafe/server/server_gui/preferences/PreferencesDialog.class \
	tsafe/server/server_gui/SplashScreen.class \
	tsafe/server/server_gui/utils/DoubleField.class \
	tsafe/server/server_gui/utils/LayoutUtils.class \
	tsafe/server/server_gui/utils/LongField.class \
	tsafe/server/server_gui/utils/table/ColumnComparator.class \
	tsafe/server/server_gui/utils/table/DefaultSortTableModel.class \
	tsafe/server/server_gui/utils/table/FixedTableCellRenderer.class \
	tsafe/server/server_gui/utils/table/JSortTable.class \
	tsafe/server/server_gui/utils/table/SortArrowIcon.class \
	tsafe/server/server_gui/utils/table/SortHeaderRenderer.class \
	tsafe/server/server_gui/utils/table/SortTableModel.class \
	tsafe/server/server_gui/utils/table/TableUtils.class \
	tsafe/server/server_gui/utils/WaitCursorEventQueue.class

main: $(OBJ)

%.class: %.java
	$(JAVAC) -classpath '$(CLASSPATH)' $^

clean: 
	rm -f $(OBJ)
	rm -fr build

build: all
	rm -fr build
	mkdir build
	mkdir build/datafiles
	mkdir build/lib
	cp datafiles/* build/datafiles 
	cp lib/* build/lib
	cp tsafe.properties build/
	jar cvfm build/Tsafe2a.jar manifest -C ./ tsafe/
