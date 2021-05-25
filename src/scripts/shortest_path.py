# Shortest path
"""
Find the shortest path for the settings in the GUI
Last Updated 1/5/21
"""
from Passes_TSP_GUI import *
from create_terraces_GUI import *
from create_passes_GUI import convertCoords
from Image_Classes import *
from dotenv import load_dotenv
import json
import os

import shapely.geometry as sg

PATH_API_URL = "https://maps.googleapis.com/maps/api/elevation/json?path="
LOCATION_API_URL = "https://maps.googleapis.com/maps/api/elevation/json?locations="
load_dotenv()
API_KEY = os.getenv('API_KEY')


def start_loc_alt(gps_coords):
    """
    Find the altitude of the start location
    """
    data = requests.get(LOCATION_API_URL + gps_coords + "&key=" + API_KEY)
    data = data.json()
    alt = float(data['results'][0]['elevation'])

    return alt

def getAltitudeProfile(real_length,loc_string,uav_altitude):
    """
    Obtain altitude data for entire pass across generated terrain
    """
    sample_distance = 2         # Required sample distance is 5m
    samples = int(real_length/sample_distance)+1

    if samples < 2:
        samples = 2
        sample_distance = real_length/samples 

    if samples > 512:
        sample_distance += 1
        samples = int(real_length/sample_distance)+1


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

settings_data = open("src/intermediate/settings.json")
settings = json.load(settings_data)

gps_coords = open("src/intermediate/altitude_profile.json")
gps_data = json.load(gps_coords)

passes_data = open("src/intermediate/passes.json")
passes = json.load(passes_data)

# Store required settings
wind_angle = math.radians(90-float(settings['wind_direction']))
scale = float(settings['scale'])
max_alt_diff = float(settings['max_alt_diff'])
altitude =  float(settings['altitude'])
min_terrace_len = float(settings['min_terrace_length'])/scale   # Covert min turn length into pixel values

max_pass_length = float(settings['max_pass_length'])/scale

uav_mass = float(settings['uav_weight'])
uav_speed = float(settings['uav_speed'])
min_turn = float(settings['uav_min_radius'])/scale              # Covert min turn radius to pixel values
max_incline_grad = float(settings['uav_max_incline'])
battery_capacity = float(settings['battery_capacity'])
start_loc_string = settings['start_loc']
start_loc_gps = settings['start_loc_gps']
start_loc_string = start_loc_string.split(",")
start_loc = [float(start_loc_string[0]),float(start_loc_string[1]),round(start_loc_alt(start_loc_gps),2)]
population_size = int(settings['population_size'])
generations = int(settings['generations'])

# Error prevention
#max_pass_length = 2000

glide_slope = 10
NFZs = []
NFZ_edges = []
if 'nfzs' in settings:
    NFZ_coords = settings['nfzs']

    for NFZ_coord in NFZ_coords:
        NFZ_points = []
        for NFZ_point in NFZ_coord:
            coords = NFZ_point.split(",")
            NFZ_points.append([float(coords[0]),float(coords[1])])
        NFZs.append(NFZ_points)

    # Create NFZ edges

    for NFZ in NFZs:
        for index,NFZ_points in enumerate(NFZ):
            NFZ_edges.append(sg.LineString([(NFZ_points[0],NFZ_points[1]),(NFZ[index-1][0],NFZ[index-1][1])]))

settings_data.close()           # Close settings file

pass_coords = gps_data['gps']   # Get list of GPS coords
pass_data = passes['passes']    # Get list of pixel points on passes

# Cycle through all GPS points and corresponding pass coords
for index,coord in enumerate(pass_coords):
    start_gps = f"{coord['lat1']},{coord['long1']}"
    end_gps = f"{coord['lat2']},{coord['long2']}"

    start = pass_data[index]['start']               # Get pass start coord in px
    end = pass_data[index]['end']                   # Get pass end coord in px


    pass_length = float(pass_data[index]['length']) # Length in px
    real_length = scale*pass_length                 # Calculate the length in metres

    if real_length > max_pass_length:
        raise Exception("Pass is too large")
    start_contents =  start.split(",")              # Split into x and y
    end_contents = end.split(",")                   # Split into x and y

    # Convert the xy pass coords to uv coords


    x = np.array([float(start_contents[0]),float(end_contents[0])])
    y = np.array([float(start_contents[1]),float(end_contents[1])])
    plt.plot(x,y,'-bo',markersize=2)

    coords = convertCoords([[float(start_contents[0]),float(start_contents[1])],
                            [float(end_contents[0]),float(end_contents[1])]],
                            wind_angle,'uv')
    
    u = coords[0][0]
    v = coords[0][1]
    loc_string = f"{start_gps}|{end_gps}"                                                   # Create a string with the gps coords for the 
    altitude_profile, sample_distance_m = getAltitudeProfile(real_length,loc_string,altitude) # Create the altitude profile
    sample_distance = sample_distance_m/scale # Sample distance in pixels
    # Create image passes
    image_passes = createTerraces(u,v,altitude_profile,sample_distance,wind_angle,pass_length,image_passes,max_alt_diff,min_terrace_len)


start_time = time.perf_counter()
shortest_path = TSP(image_passes,wind_angle,min_turn,uav_mass,NFZs,NFZ_edges,max_incline_grad,glide_slope,start_loc,populationSize=population_size,generations=generations,mutationRate=0.3)

end_time = time.perf_counter() - start_time    # Calculate time taken to create passes and findest shortest route


max_current_draw = 20   # Initialise the maximum current draw for the worst case scenario

print("Flight path created successfully!\n")
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

spirals =  shortest_path.get_spirals()
dpaths = shortest_path.getDPaths()  # Get the Dubins paths that make up the shortest route

step_size = 1                        # Specify step size for sampling each dubins path

dubins_file = open("src/intermediate/dubins.json","w")
dubins_data = {}
dubins_data['dubins'] = []
for dpath in dpaths:
    points = dubins_path_sample_many(dpath,step_size)
    dubins_points = {}
    dubins_points['points'] = []
    for point in points:
        dubins_points['points'].append(f"{point[0],point[1],point[2]}")
    dubins_data['dubins'].append(dubins_points)

dubins_data['spirals'] = []
for spiral in spirals:  
    points = sample_spiral(spiral,step_size)    # Sample the spiral paths and obtain the sampled points
    spiral_points = {}
    spiral_points['points'] = []
    for point in points:
        spiral_points['points'].append(f"{round(point[0],2),round(point[1],2),round(point[2],2)}")
    dubins_data['spirals'].append(spiral_points)

json.dump(dubins_data,dubins_file,indent=4)

dubins_file.close()

with open("src/intermediate/output.json",'w') as output:
    gene_string = shortest_path.getOutput()
    json.dump(gene_string,output,indent=4)
output.close()
