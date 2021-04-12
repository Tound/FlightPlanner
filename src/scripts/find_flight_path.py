# Efficient flight path
"""
Main file to be called to create an efficient flight path for a polygon
and corresponding NFZs for a given terrain
"""
import math
import numpy as np
import matplotlib.pyplot as plt
from opensimplex import OpenSimplex

from create_passes_GUI import *
from Passes_TSP_GUI import *
from camera_calculations import *

import time
import sys
import traceback

import json

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
    def __init__(self,uav,camera,side_overlap,wind_angle,scale,altitude,ground_sample_distance):
        self.uav = uav
        self.camera = camera
        self.side_overlap = side_overlap
        self.wind = wind
        self.scale = scale
        self.altitude = altitude
        self.ground_sample_distance = ground_sample_distance



polygon = []
NFZs = []
wind = [None,None]

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
    max_incline_angle = 31 #degs
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
    #print("Reading text file")
    # Read intermediate file
    try:
        data = open("src/intermediate/settings.json","r")
        settings = json.load(data)
        
        scale = float(settings['scale'])

        start_loc = settings['start_loc']
        start_loc = start_loc.split(",")
        start_loc = [float(start_loc[0]),float(start_loc[1])]

        uav_mass = float(settings['uav_weight'])
        uav_speed = float(settings['uav_speed'])
        min_turn = float(settings['uav_min_radius'])
        max_incline_angle = float(settings['uav_max_incline'])
        battery_capacity = float(settings['battery_capacity'])

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

if scale == 0:
    scale = 1

# Viablility checks
if wind[0] > uav_speed:
    print("Too windy for this flight")
    exit(1)
elif wind[0] > uav_speed/2:
    print("Heading angle will be steep")

# Calculate heading angle
heading_angle = math.asin(wind[0]/uav_speed)
print(math.degrees(wind[1]))
print(f"Plane will fly with a heading angle of {round(math.degrees(heading_angle),2)} degrees towards the wind!")

# Create UAV, camera and configuration object and store all variables
uav = UAV(uav_mass,uav_speed,min_turn,max_incline_angle)

image_x,image_y = imageDimensions(cam_resolution,aspect_ratio)

camera = Camera(sensor_x,sensor_y,focal_length,cam_resolution,aspect_ratio,image_x,image_y)
config = Configuration(uav,camera,side_overlap,wind,scale,altitude,ground_sample_distance)

polygon_edges = []
for i in range(0,len(polygon)):
    polygon_edges.append(Edge(polygon[i-1][0],polygon[i-1][1],
                    polygon[i][0],polygon[i][1]))

NFZ_edges = []
for NFZ in NFZs:
    for i in range(0,len(NFZ)):
        NFZ_edges.append(Edge(NFZ[i-1][0],NFZ[i-1][1],
                        NFZ[i][0],NFZ[i][1]))


createPasses(polygon,polygon_edges,NFZs,config) # Create pass objects for current configuration
