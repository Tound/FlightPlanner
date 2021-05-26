#!/usr/bin/env python
"""
Main file to be called to create an efficient flight path for a polygon
and corresponding NFZs for a given terrain
Last updated 25/5/21
"""
import math
import numpy as np
import matplotlib.pyplot as plt
from opensimplex import OpenSimplex
from camera_calculations import *

from create_passes_GUI import *
from Passes_TSP_GUI import *

import sys
import traceback

import json

class UAV:
    """
    UAV class holds all UAV settings for a specific flight
    """
    def __init__(self,weight,velocity,max_velocity,min_turn,max_incline_grad,heading_angle):
        self.weight = weight                        # Weight of UAV in kg
        self.velocity = velocity                    # Constant velocity of UAV in m/s
        self.max_velocity = max_velocity            # Maximum velocity of UAV in m/s
        self.min_turn = min_turn                    # Minimum turn radius of the UAV in m
        self.max_incline_grad = max_incline_grad    # Maximum incline gradient of the UAV
        self.heading_angle = heading_angle          # Required heading angle of UAV

class Configuration:
    """
    Configuration class holds the settings for an entire flight 
    including Camera object and UAV object
    """
    def __init__(self,uav,camera,side_overlap,wind_angle,scale,altitude,ground_sample_distance,min_pass_length,max_pass_length):
        self.uav = uav                                              # UAV object used in the flight
        self.camera = camera                                        # Camera object used in the flight
        self.side_overlap = side_overlap                            # Side overlap of the images as a decimal
        self.wind = wind                                            # Wind properties in the form (velocity (m/s), direction (radians))
        self.scale = scale                                          # Scaling factor for pixels to metres
        self.altitude = altitude                                    # Desired UAV altitude above the ground
        self.ground_sample_distance = ground_sample_distance        # The desired ground sample distance in m/px
        self.min_pass_length = min_pass_length                      # Minimum pass length in metres
        self.max_pass_length = max_pass_length                      # Maximum pass length in metres

polygon = []
NFZs = []
wind = [None,None]

"""
For testing purposes only, a set of predefined settings 
for a test case are present here
"""
if len(sys.argv) == 2 and sys.argv[1] == 'test':

    # Create terrain

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


    # Setup
    # UAV settings
    min_turn = 20 #m
    max_incline_angle = 31 #degs
    glide_slope = 20
    uav_mass = 18 # Kg
    uav_speed = 8

    # Camera settings
    side_overlap = 0.2          # Percentage
    forward_overlap = 0.50      # Percentage
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
    ground_sample_distance = 0.02  # m/px

    max_current_draw = 20
    battery_capacity = 2200

    # Test cases
    polygon = [[100,100],[100,650],[650,650],[750,350],[650,100]]
    NFZ = [[300,450],[450,450],[450,200],[300,200]]
    NFZ2 = [[200,450],[300,450],[300,350],[200,350]]
    NFZs = []#[NFZ,NFZ2]
    start_loc = [400,730,terrain[730][400]]

else:
    # Read intermediate settigns file
    try:
        data = open("src/intermediate/settings.json","r")
        settings = json.load(data)
        
        scale = float(settings['scale'])

        start_loc = settings['start_loc']
        start_loc = start_loc.split(",")
        start_loc = [float(start_loc[0]),float(start_loc[1])]

        uav_mass = float(settings['uav_weight'])
        uav_speed = float(settings['uav_speed'])
        uav_max_speed = float(settings['uav_max_speed'])
        min_turn = float(settings['uav_min_radius'])
        max_incline_angle = float(settings['uav_max_incline'])
        battery_capacity = float(settings['battery_capacity'])
        min_pass_length = float(settings['min_terrace_length'])
        max_pass_length = float(settings['max_pass_length'])

        sensor_x = float(settings['cam_sensor_x'])  *10**-3
        sensor_y= float(settings['cam_sensor_y'])   *10**-3
        focal_length = float(settings['cam_focal_length'])  *10**-3
        cam_resolution = float(settings['cam_resolution'])

        aspect_ratio = settings['cam_aspect_ratio']
        values = aspect_ratio.split(":")
        aspect_ratio = (int(values[0]),int(values[1]))

        wind = [float(settings['wind_speed']),math.radians(90-float(settings['wind_direction']))]
        altitude = settings['altitude']
        if altitude == "":
            altitude = None
        else:
            altitude = float(altitude)

        side_overlap = float(settings['side_overlap'])/100
        ground_sample_distance = settings['gsd']
        if ground_sample_distance == "":
            ground_sample_distance = None
        else:
            ground_sample_distance = float(ground_sample_distance)
        
        for point in settings['points']:
            values = point.split(",")
            polygon.append([float(values[0]),float(values[1])])

        if 'nfzs' in settings:
            for nfz in settings['nfzs']:
                NFZ = []
                for nfz_points in nfz:
                    values = nfz_points.split(",")
                    NFZ.append([float(values[0]),float(values[1])])
                NFZs.append(NFZ)

        data.close()
    except Exception:
        traceback.print_exc()
        print("Cannot find settings.json")
        sys.stderr.write("Cannot find settings.json")
        exit(1)

# Viablility checks to ensure constant ground speed
speed_required = math.sqrt(wind[0]*wind[0] + uav_speed*uav_speed)
if speed_required > uav_max_speed:
    print("Too windy for this flight as the UAV would exceed maximum speed")
    raise Exception("Too windy for this flight as the UAV would exceed maximum speed")
else:
    # Calculate heading angle
    heading_angle = math.atan(wind[0]/uav_speed)
    print(f"Plane will fly with a heading angle of {round(math.degrees(heading_angle),2)} degrees towards the wind!")
    print(f"Required UAV speed: {round(speed_required,2)} m/s")

# Create UAV, camera and configuration object and store all variables
uav = UAV(uav_mass,uav_speed,uav_max_speed,min_turn,max_incline_angle,heading_angle)
camera = Camera(sensor_x,sensor_y,focal_length,cam_resolution,aspect_ratio)
config = Configuration(uav,camera,side_overlap,wind,scale,altitude,ground_sample_distance,min_pass_length,max_pass_length)

# Define all edges for the polygon
polygon_edges = []
for i in range(0,len(polygon)):     # Cycle through each polygon vertex
    # Create a new edge and add to the list of polygon edges
    polygon_edges.append(Edge(polygon[i-1][0],polygon[i-1][1],
                    polygon[i][0],polygon[i][1]))

# Define all edges for the NFZs
NFZ_edges = []
for NFZ in NFZs:                    # Cycle through each NFZ
    for i in range(0,len(NFZ)):     # Cycle through the vertices of the NFZ
        # Create a new edge and add to the list of NFZ edges
        NFZ_edges.append(Edge(NFZ[i-1][0],NFZ[i-1][1],
                        NFZ[i][0],NFZ[i][1]))


createPasses(polygon,polygon_edges,NFZs,config) # Create pass objects for current configuration
