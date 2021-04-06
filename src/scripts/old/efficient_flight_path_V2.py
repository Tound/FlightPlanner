# Efficient flight path
"""
Main file to be called to create an efficient flight path for a polygon
and corresponding NFZs for a given terrain
"""
import math
import numpy as np
import matplotlib.pyplot as plt
from opensimplex import OpenSimplex

from create_passes_V2 import *
from Passes_TSP_V2 import *
from camera_calculations import *
from Image_Classes_V2 import *

import time

import sys

class Camera:
    """
    Camera class holds all camera settings for a specific flight
    """
    def __init__(self,sensor_x,sensor_y,focal_length,resolution,aspect_ratio,image_x=None,image_y=None,fov=None):
        self.sensor_x = sensor_x
        self.sensor_y = sensor_y
        self.focal_length = focal_length
        self.resolution = resolution
        self.aspect_ratio = aspect_ratio
        self.image_x = image_x
        self.image_y = image_y
        self.fov = fov

class UAV:
    """
    UAV class holds all UAV settings for a specific flight
    """
    def __init__(self,weight,velocity,min_turn,max_incline_grad,min_speed = None,max_speed = None):
        self.weight = weight
        self.velocity = velocity
        self.min_turn = min_turn
        self.max_incline_grad = max_incline_grad

class Configuration:
    """
    Configuration class holds the settings for an entire flight 
    including Camera object and UAV object
    """
    def __init__(self,uav,camera,side_overlap,forward_overlap,coverage_resolution,wind_angle):
        self.uav = uav
        self.camera = camera
        self.side_overlap = side_overlap
        self.forward_overlap = forward_overlap
        self.coverage_resolution = coverage_resolution
        self.wind = wind

"""
For testing purposes only, a set of predefined settings 
for a test case are present here
"""
if len(sys.argv) == 2 and sys.argv[1] == 'test':
    ########################
    # CREATE TERRAIN
    ########################
    height = 750        # 750m
    width = 750         # 750m
    freq = 5            # Hz
    multiplier = 10

    gen = OpenSimplex()#seed=random.randint(0,100))
    def noise(nx, ny):
        # Rescale from -1.0:+1.0 to 0.0:1.0
        return gen.noise2d(nx, ny) / 2.0 + 0.5

    terrain = np.zeros((height,width))
    for y in range(height):
        for x in range(width):
            nx = x/width - 0.5
            ny = y/height - 0.5
            elevation = multiplier*noise(freq*nx, freq*ny)
            terrain[y][x] =  multiplier*math.pow(elevation,0.5)

    ######################
    # Setup
    ######################
    # UAV settings
    min_turn = 20 #m
    max_incline_grad = 31 #degs
    glide_slope = 20
    uav_mass = 18 # Kg
    uav_speed = 8

    # Camera settings
    side_overlap = 0.2          # Percentage
    forward_overlap = 0.50       # Percentage
    sensor_x = 5.62    *10**-3  # mm
    sensor_y = 7.4     *10**-3  # mm
    focal_length = 3.6 *10**-3  # mm
    aspect_ratio = (4,3)        # x:y
    cam_resolution = 12         # MP
    image_x = 4000              # px
    image_y = 3000              # px
    fov = 20                    # degs

    # Flight settings
    wind = (5,math.radians(90)) #Polar coords (Mag, degrees)
    coverage_resolution = 0.02  # m/px

    max_current_draw = 20
    battery_capacity = 2200

    # Test cases
    polygon = [[100,100],[100,650],[650,650],[750,350],[650,100]]
    NFZ = [[300,450],[450,450],[450,200],[300,200]]
    NFZ2 = [[200,450],[300,450],[300,350],[200,350]]
    NFZs = []#[NFZ,NFZ2]
    start_loc = [400,730,terrain[730][400]]

else:
    print("Reading text file")
    # Read intermediate file
    f = open("intermediate/intermediate.txt","r")
    while True:
        line = f.readline()
        if line.startswith("====START===="):
            line = f.readline()
            if line.startswith("START_LOC"):
                line = line.strip("\n")
                contents = line.split("\t")
                contents = contents[1].split(",")
                start_loc = [int(float(contents[0])),int(float(contents[1])),None]
            line = f.readline()
            while not line.startswith("====NFZ===="):
                line = line.strip("\n")
                contents = line.split(",")
                point = [int(float(contents[0])),int(float(contents[1]))]
                polygon.append(point)
                line = f.readline()
            line = f.readline()
            NFZ = []
            while line != "====END====":
                if line.startswith("NFZ START"):
                    if len(NFZ) > 0:
                        NFZs.append(NFZ)
                        NFZ = []
                else:
                    line = line.strip("\n")
                    contents = line.split(",")
                    NFZ.append([int(float(contents[0])),int(float(contents[1]))])
                line = f.readline()
            if len(NFZ) > 0:
                NFZs.append(NFZ)
                NFZ = []

        else:
            contents = line.split("\t")
            if line.startswith("SCALE"):
                scale = contents[1]
            wlif line.startswith("UAV_WEIGHT"):
                uav_mass = contents[1]
            elif line.startswith("UAV_MIN_RADIUS"):
                min_turn = contents[1]
            elif line.startswith("UAV_MAX_INCLINE"):
                max_incline_angle = contents[1]
            elif line.startswith("BATTERY_CAPACITY"):
                battery_capacity = contents[1]
            elif line.startswith("CAM_SENSOR_X"):
                sensor_x = float(contents[1])*10**-3
            elif line.startswith("CAM_SENSOR_Y"):
                sensor_y = float(contents[1])*10**-3
            elif line.startswith("CAM_FOCAL_LENGTH"):
                focal_length = float(contents[1])*10**-3
            elif line.startswith("CAM_RESOLUTION"):
                cam_resolution = int(contents[1])
            elif line.startswith("CAM_ASPECT_RATIO"):
                contents = contents[1].strip("\n")
                contents = contents.split(":")
                aspect_ratio = (int(contents[0]),int(contents[1]))
            elif line.startswith("UAV_SPEED"):
                uav_speed = contents[1]
            elif line.startswith("WIND_SPEED"):
                wind[0] = contents[1]
            elif line.startswith("WIND_DIRECTION"):
                wind[1] = math.radians(float(contents[1])-90)
            elif line.startswith("ALTITUDE"):
                altitude = contents[1]
            elif line.startswith("FORWARD_OVERLAP"):
                forward_overlap = float(contents[1])/100
            elif line.startswith("SIDE_OVERLAP"):
                side_overlap = float(contents[1])/100
            elif line.startswith("GSD"):
                ground_sample_distance = float(contents[1])
            elif line == "":
                break
            else:
                print("Unknown line")
                print("Error")
                exit(1)
    f.close()


# Viablility checks
if wind[0] > uav_speed:
    print("Too windy for this flight")
    exit(1)
elif wind[0] > uav_speed/2:
    print("Heading angle will be steep")

# Calculate heading angle
heading_angle = math.asin(wind[0]/uav_speed)
print(f"Plane will fly with a heading angle of {round(math.degrees(heading_angle),2)} degrees towards the wind!")

# Create UAV, camera and configuration object and store all variables
uav = UAV(uav_mass,uav_speed,min_turn,max_incline_grad)
camera = Camera(sensor_x,sensor_y,focal_length,cam_resolution,aspect_ratio,image_x,image_y)
config = Configuration(uav,camera,side_overlap,forward_overlap,coverage_resolution,wind)

start_time = time.clock()   # Get current time for measuring solve time

image_passes = createPasses(polygon,NFZs,terrain,config) # Create pass objects for current configuration

# Update passes with altitudes from API

# Use TSP to find shortest route
shortest_path = TSP(image_passes,wind[1],min_turn,uav_mass,NFZs,max_incline_grad,start_loc,populationSize=50,generations=200,mutationRate=0.3)

end_time = time.clock() - start_time    # Calculate time taken to create passes and findest shortest route

# Print flight stats
print(f"Total time to solve: {round(end_time/60,2)}mins")
print(f"Total length of route: {round(shortest_path.getLength(),2)}m")

time_of_flight = shortest_path.getLength()/uav_speed
print(f"Estimated time of flight: {round(time_of_flight/60,2)}mins")
current_used = max_current_draw*time_of_flight/3600
print(f"Estimated Current draw (Worst case): {round(current_used,2)}A")
if current_used > battery_capacity*10**-3:
    print("Current battery capacity will not suffice")
else:
    print("Current battery capacity will suffice")

dpaths = shortest_path.getDPaths()  # Get the Dubins paths that make up the shortest route

stepSize = 0.5  # Specify step size for sampling each dubins path

# Convert into GPS coords
# Requires API for elevation
# Create waypoints file
