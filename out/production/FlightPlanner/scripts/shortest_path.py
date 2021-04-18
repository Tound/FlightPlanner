# Shortest path
# Use TSP to find shortest route
from Passes_TSP_GUI import *
from create_terraces_GUI import *
from create_passes_GUI import convertCoords
from Image_Classes_V2 import *
from dotenv import load_dotenv
import json
import os

PATH_API_URL = "https://maps.googleapis.com/maps/api/elevation/json?path="
LOCATION_API_URL = "https://maps.googleapis.com/maps/api/elevation/json?locations="
load_dotenv()
API_KEY = os.getenv('API_KEY')


def start_loc_alt(gps_coords):
    data = requests.get(LOCATION_API_URL + gps_coords + "&key=" + API_KEY)
    data = data.json()
    alt = float(data['results'][0]['elevation'])
    return alt

def getAltitudeProfile(real_length,loc_string,uav_altitude):
    """
    Obtain altitude data for entire pass across generated terrain
    """
    samples = int(real_length+1)
    sample_distance = real_length/samples   # Find the distance between each sample
    altitude_profile = []
    request = requests.get(PATH_API_URL + loc_string + "&samples=" + f"{samples}" + "&key=" + API_KEY)
    request = request.json()["results"]
    for result in request:
        elevation = float(result['elevation']) + uav_altitude
        altitude_profile.append(elevation)
    return altitude_profile, sample_distance

image_passes = []
# Make terraces
# Get altitude data

data = open("src/intermediate/settings.json")
settings = json.load(data)

gps_coords = open("src/intermediate/altitude_profile.json")
gps_data = json.load(gps_coords)

passes_data = open("src/intermediate/passes.json")
passes = json.load(passes_data)


# Store required settings
wind_angle = 90-float(settings['wind_direction'])
scale = float(settings['scale'])
max_alt_diff = float(settings['max_alt_diff'])
altitude =  float(settings['altitude'])
min_terrace_len = float(settings['min_terrace_length'])
uav_mass = float(settings['uav_weight'])
uav_speed = float(settings['uav_speed'])
min_turn = float(settings['uav_min_radius'])
max_incline_grad = float(settings['uav_max_incline'])
battery_capacity = float(settings['battery_capacity'])
start_loc_string = settings['start_loc']
start_loc_gps = settings['start_loc_gps']
start_loc_string = start_loc_string.split(",")
start_loc = [float(start_loc_string[0]),float(start_loc_string[1]),start_loc_alt(start_loc_gps)]
glide_slope = 10
NFZs = []
NFZ_edges = []
if 'nfzs' in settings:
    NFZ_coords = settings['nfzs']
    for NFZ_coord in NFZ_coords:
        NFZ_points = []
        for NFZ_point in NFZ:
            coords = NFZ_point.split(",")
            NFZ_points.append([float(coords[0]),float(coords[1])])
        NFZs.append(NFZ_points)

    # Create NFZ edges

    for NFZ in NFZs:
        for NFZ_points,index in enumerate(NFZ):
            NFZ_edges.append(sg.LineString([(NFZ_points[0],NFZ_points[1]),(NFZ[index-1][0],NFZ[index-1][1])]))
data.close()

pass_coords = gps_data['gps']   # Get list of GPS coords
pass_data = passes['passes']    # Get list of pixel points on passes

# Cycle through all GPS points and corresponding pass coords
for index,coord in enumerate(pass_coords):
    start_gps = f"{coord['lat1']},{coord['long1']}"
    end_gps = f"{coord['lat2']},{coord['long2']}"

    start = pass_data[index]['start']
    end = pass_data[index]['end']
    pass_length = float(pass_data[index]['length']) # Length in px
    real_length = scale*pass_length

    start_contents =  start.split(",")
    end_contents = end.split(",")

    coords = convertCoords([[float(start_contents[0]),float(start_contents[1])],
                            [float(end_contents[0]),float(end_contents[1])]],
                            wind_angle,'uv')
    
    u = coords[0][0]
    v = coords[0][1]
    loc_string = f"{start_gps}|{end_gps}"
    altitude_profile, sample_distance = getAltitudeProfile(real_length,loc_string,altitude)
    image_passes = createTerraces(u,v,altitude_profile,sample_distance,wind_angle,pass_length,image_passes,max_alt_diff,min_terrace_len)

# Get pass coords
# Get altitude profile for pass
# Split into terraces


print(image_passes)

start_time = time.clock()
shortest_path = TSP(image_passes,wind_angle,min_turn,uav_mass,NFZs,NFZ_edges,max_incline_grad,glide_slope,start_loc,populationSize=50,generations=200,mutationRate=0.3)

end_time = time.clock() - start_time    # Calculate time taken to create passes and findest shortest route


max_current_draw = 20

#Print flight stats
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

dubins_file = open("src/intermediate/dubins.json",'w')
dubins_data = {}
dubins_data['dubins'] = []
for dpath in dpaths:
    points = dubins_path_sample_many(dpath,stepSize)
    dubins_points = {}
    dubins_points['points'] = []
    for point in points:
        dubins_points['points'].append(f"{point[0],point[1],point[2]}")

    dubins_data['dubins'].append(dubins_points)
json.dump(dubins_data,dubins_file,indent=4)

dubins_file.close()

# Convert into GPS coords
# Requires API for elevation
# Create waypoints file