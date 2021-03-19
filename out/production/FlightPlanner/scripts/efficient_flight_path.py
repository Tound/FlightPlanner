# Efficient flight path
"""
Main file to be called to create an efficient flight path for a polygon
"""
import math
import numpy as np
import matplotlib.pyplot as plt
from opensimplex import OpenSimplex

from create_passes import *
from Passes_TSP import *
from camera_calculations import *
from Image_Classes import *

import time

class Camera:
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
    def __init__(self,weight,velocity,min_turn,max_incline_grad,min_speed = None,max_speed = None):
        self.weight = weight
        self.velocity = velocity
        self.min_turn = min_turn
        self.max_incline_grad = max_incline_grad

class Configuration:
    def __init__(self,uav,camera,side_overlap,forward_overlap,coverage_resolution,wind_angle):
        self.uav = uav
        self.camera = camera
        self.side_overlap = side_overlap
        self.forward_overlap = forward_overlap
        self.coverage_resolution = coverage_resolution
        self.wind = wind

polygon = []
start_loc = [None,None,None]
NFZs = []

# UAV settings
min_turn = None
max_incline_grad = None
glide_slope = None
uav_mass = None
uav_speed = None
altitude = None

# Camera settings
side_overlap = None
forward_overlap = None
sensor_x = None
sensor_y = None
focal_length = None
aspect_ratio = None
cam_resolution = None
image_x = None
image_y = None
fov = None

# Flight settings
wind = [None,None] #Polar coords (Mag, degrees)
ground_sample_distance = None  # m/px

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
        if line.startswith("UAV_WEIGHT"):
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

image_x, image_y = imageDimensions(cam_resolution,aspect_ratio)
coverage_width, coverage_height = getCoverageSize(ground_sample_distance,image_x,image_y)
if altitude is None and ground_sample_distance is None:
    print("Altitude or GSD must be a suitable value")
elif altitude is None:
    altitude = getAltitude(focal_length,coverage_width,sensor_x)
elif ground_sample_distance is None:
    ground_sample_distance = getGSD(focal_length,altitude,coverage_width)
else:
    print("All inputs are ok")

print(NFZs)

########################
# CREATE TERRAIN
########################
height = 750        # 750m
width = 750         # 750m
freq = 2            # Hz

gen = OpenSimplex()#seed=random.randint(0,100))
def noise(nx, ny):
    # Rescale from -1.0:+1.0 to 0.0:1.0
    return gen.noise2d(nx, ny) / 2.0 + 0.5

terrain = np.zeros((height,width))
for y in range(height):
    for x in range(width):
        nx = x/width - 0.5
        ny = y/height - 0.5
        elevation = 20*noise(freq*nx, freq*ny)
        terrain[y][x] =  20*math.pow(elevation,0.5)

######################
# Setup
######################

glide_slope = 20

start_loc[2] = terrain[int(start_loc[1])][int(start_loc[0])]

uav = UAV(uav_mass,uav_speed,min_turn,max_incline_grad)
camera = Camera(sensor_x,sensor_y,focal_length,cam_resolution,aspect_ratio,image_x,image_y)
config = Configuration(uav,camera,side_overlap,forward_overlap,ground_sample_distance,wind)

# Create canvas/ choose area
# Get startpoint, 2D points from canvas and elevation data via intermediate text file + flight settings
# Read file and create points

# Polygon
# NFZ
# UAV Settings
# Camera settings
# Flight settings

# Multiple angles?

start_time = time.clock()

image_passes = createPasses(polygon,NFZs,terrain,config)

with open("intermediate/gpslookup.txt","w") as f:
    for image_pass in image_passes:
        for image_loc in image_pass.image_locs:
            f.write(f"{round(image_loc.x,0)},{round(image_loc.y,0)}\n")
        f.write("NEW PASS\n")
f.close()

# DRAW FOR TESTING
fig = plt.figure(num=1,clear=True,figsize=(12,8))
ax = fig.add_subplot(1,1,1,projection='3d')
(x,y) = np.meshgrid(np.arange(0,width,1),np.arange(0,height,1))
ax.plot_surface(x, y, terrain,cmap='terrain',zorder=5)
ax.set(title='Terrain Generated',xlabel='x', ylabel='y', zlabel='z = Height (m)')
#ax.set_zlim(0,150)

ax.set_aspect(aspect='auto')
fig.tight_layout()

polygon = np.array([polygon])
plt.plot(polygon[:,0],polygon[:,1],100,'-bo',zorder=15)
for NFZ in NFZs:
    NFZ = np.array([NFZ])
    plt.plot(NFZ[:,0],NFZ[:,1],'-ro')


for image_pass in image_passes:
    loc_x = np.array([])
    loc_y = np.array([])
    loc_z = np.array([])
    for image_loc in image_pass.image_locs:
        loc_x = np.append(loc_x,image_loc.x)
        loc_y = np.append(loc_y,image_loc.y)
        loc_z = np.append(loc_z,image_loc.altitude)
        
    plt.plot(loc_x,loc_y,loc_z,'-ro',zorder=10)

plt.show()
# Need GPS and altitude before TSP
# Update passes with altitudes from API

# Use TSP to find shortest route
shortest_path = TSP(image_passes,wind[1],min_turn,uav_mass,NFZs,max_incline_grad,start_loc,populationSize=50,generations=2000,mutationRate=0.3)
end_time = time.clock() - start_time
print(f"Total time: {round(end_time/60,2)}mins")
print(f"Total length: {round(shortest_path.getLength(),2)}m")

dpaths = shortest_path.getDPaths()

stepSize = 0.5


for dpath in dpaths:
    dubinsX = np.array([])
    dubinsY = np.array([])
    dubinsZ = np.array([])  
    points = dubins_path_sample_many(dpath,stepSize)
    for point in points:
        dubinsX = np.append(dubinsX,point[0])
        dubinsY = np.append(dubinsY,point[1])
        dubinsZ = np.append(dubinsZ,point[2])
    plt.plot(dubinsX,dubinsY,dubinsZ,'-yo',zorder=15,markersize = 1)

plt.show()

# Convert into GPS coords
# Requires API for elevation
# Create waypoints file
