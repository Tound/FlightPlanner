-- FlightPlanner Setup and instructions for use --
Created by Thomas Pound
Last updated 2/5/21

This software created by Thomas Pound is available for reuse and modification.
The software is designed to create viable and optimal flight routes for aerial photogrammetry 
surveys using a fixed-wing UAV. The software is built using a Java GUI and backend Python scripts.
The software is cross-platform but some issues may appear on various versions of operating systems.
Internet access is required for the Java GUI as a mapping API and Google Maps APIs are 
used to gain real geolocation and terrain data however the seperate Python scripts do not
 require internet access for simple use and testing.

-- Prerequisites --
Python version must be 3+
Java version must be version 8 (Built-in JavaFX is required)
Both Python3 and Java 8 must be installed and on the PATH
A valid Google Maps API key must be found in a .env file as the API_KEY variable in the scripts directory

-- Install the required Python packages --
Before being able to use the software the following python packages must be installed.
In the terminal or cmd, the command:

	pip3 install -r requirements.txt

can be used to install all the required python packages.

-- Running of the software --
Double pressing on FlightPlanner.jar will begin the program (.jar file must be in the same directory as src).

The program can also be ran from the intelliJ IDE by loading the project.

-- Trouble-shooting --
Ensure that all Python scripts are visible in the scripts folder
Ensure that the intermediate folder is visible in the src folder
The src folder should contain the following directories:
	uavs
	cameras
	flight_settings
	flight plan
	assets
	intermediate
	main
	style
	scripts
.env is required for the Java GUI which should be in the scripts folder
Make sure the correct version of Python is being used on line 675 of FlightSettings.java