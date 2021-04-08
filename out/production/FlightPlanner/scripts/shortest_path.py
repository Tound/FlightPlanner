# Shortest path
# Use TSP to find shortest route
from Passes_TSP_GUI import *
from create_terraces_GUI import *
from create_passes_GUI import convertCoords
from Image_Classes_V2 import *
from dotenv import load_dotenv
import json
import os

API_URL = "https://maps.googleapis.com/maps/api/elevation/json?path="
load_dotenv()
API_KEY = os.getenv('API_KEY')

def getAltitudeProfile(pass_length,loc_string,uav_altitude,u,start_v,wind_angle):
    """
    Obtain altitude data for entire pass across generated terrain
    """
    samples = 10
    altitude_profile = []
    request = requests.get(API_URL + loc_string + "&samples=" + f"samples" + "&key=" + API_KEY)
    request = request.json()['results']
    for result in request:
        altitude_profile.append(float(result['elevation']))
    print(altitude_profile)

    return altitude_profile

image_passes = []
# Make terraces
# Get altitude data
gpsCoords = open("src/intermediate/altitudeProfile.txt")
line = gpsCoords.readline()
while line != None:
    print(line)
    line = line.strip("\n")
    contents = line.split("\t")
    if line.startswith("SCALE"):
        scale = float(contents[1])
    elif line.startswith("WIND_ANGLE"):
        wind_angle = math.radians(float(contents[1]))
    elif line.startswith("ALTITUDE"):
        altitude = float(contents[1])
        max_alt_diff = float(contents[2])
    elif line.startswith("MIN_TERRACE_LENGTH"):
        min_terrace_len = float(contents[1])
    elif line.startswith("NEW_TERRACE"):
        x = float(contents[1])
        y = float(contents[2])
        coords = convertCoords([[x,y]],wind_angle,'uv')
        u = coords[0][0]
        v = coords[0][1]
        pass_length = float(contents[3])
        #terrace = Terrace(x,y,length,wind_angle)
        #terraces.append(terrace)
    elif line.startswith("MIN_TURN_RADIUS"):
        min_turn = float(contents[1])
    elif line == "":
        break
    else:
        loc_string = f"{contents[0]},{contents[1]}|{contents[2]},{contents[3]}"
        altitude_profile = getAltitudeProfile(pass_length,loc_string,altitude,u,v,wind_angle)
        print("MAKING TERRACE")
        image_passes = createTerraces(u,v,altitude_profile,wind_angle,pass_length,image_passes,max_alt_diff,min_terrace_len)
    line = gpsCoords.readline()

# Get pass coords
# Get altitude profile for pass
# Split into terraces

gpsCoords.close()

start_time = time.clock()
shortest_path = TSP(image_passes,wind_angle,min_turn,uav_mass,NFZs,max_incline_grad,start_loc,populationSize=50,generations=200,mutationRate=0.3)

end_time = time.clock() - start_time    # Calculate time taken to create passes and findest shortest route

# Print flight stats
# print(f"Total time to solve: {round(end_time/60,2)}mins")
# print(f"Total length of route: {round(shortest_path.getLength(),2)}m")

# time_of_flight = shortest_path.getLength()/uav_speed
# print(f"Estimated time of flight: {round(time_of_flight/60,2)}mins")
# current_used = max_current_draw*time_of_flight/3600
# print(f"Estimated Current draw (Worst case): {round(current_used,2)}A")
# if current_used > battery_capacity*10**-3:
#     print("Current battery capacity will not suffice")
# else:
#     print("Current battery capacity will suffice")

# dpaths = shortest_path.getDPaths()  # Get the Dubins paths that make up the shortest route

# stepSize = 0.5  # Specify step size for sampling each dubins path

# f = open("src/intermediate/dubins.txt")
# for dpath in dpaths:
#     points = dubins_path_sample_many(dpath,stepSize)
#     f.write("DUBINS\n")
#     for point in points:
#         f.write(f"{point[0],point[1],point[2]}\n")
# f.close()

# Convert into GPS coords
# Requires API for elevation
# Create waypoints file