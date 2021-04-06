# Shortest path
# Use TSP to find shortest route
from Passes_TSP import *
from create_terraces_GUI import *
from create_passes_GUI import convertCoords

def getAltitudeProfile(pass_length,terrain,uav_altitude,u,start_v,wind_angle):
    """
    Obtain altitude data for entire pass across generated terrain
    """
    altitude_profile = []
    v = start_v
    for k in range(0,round(pass_length)):
        coord = convertCoords([[u,v]],wind_angle,'xy')
        x = coord[0][0]
        y = coord[0][1]
        x_points = [int(x),int(x),int(x)+1,int(x)+1]
        y_points = [int(y),int(y)+1,int(y)+1,int(y)]
        z_points = [terrain[int(y)][int(x)],terrain[int(y)+1][int(x)],
                    terrain[int(y)+1][int(x)+1],terrain[int(y)][int(x)+1]]

        # For created terrain ONLY
        z = griddata((x_points,y_points),z_points,(x,y))    # Interpolate        
        altitude = z + uav_altitude

        altitude_profile.append(altitude)
        v +=1
    return altitude_profile

# Add altitude to passes and make terraces
class Terrace:
    def __init__(self,x,y,length,wind_angle):
        self.x = x
        self.y = y
        self.length = length
        self.wind_angle = wind_angle
        coords = convertCoords([[x,y]],wind_angle,'uv')

        self.u = coords[0][0]
        self.v = coords[0][1]

terraces = []
# Make terraces
api_url = "https://api.opentopodata.org/v1/test-dataset?locations=56,123"
# Get altitude data
gpsCoords = open("intermediate/altitudeCoords")
line = gpsCoords.readline()
while line != None:
    line = line.strip("\n")
    contents = line.split("\t")
    if line.startswith("SCALE"):
        scale = float(contents[1])
    elif line.startswith("WIND_ANGLE"):
        wind_angle = float(contents[1])
    elif line.startswith("NEW_TERRACE")
        x = contents[1]
        y = contents[2]
        length = contents[3]
        terrace = Terrace(x,y,length,wind_angle)
        terraces.append(terrace)
    else:
        api_url = api_url + "|"

gpsCoords.close()

data = requests.get(api_url)
data.json()
altitude_profile

altitude_profile = getAltitudeProfile(pass_length,terrain,uav_altitude,u,start_v,wind_angle)
for image_pass in image_passes:
    createTerraces()


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

f = open("src/intermediate/dubins.txt")
for dpath in dpaths:
    points = dubins_path_sample_many(dpath,stepSize)
    f.write("DUBINS\n")
    for point in points:
        f.write(f"{point[0],point[1],point[2]}\n")
f.close()

# Convert into GPS coords
# Requires API for elevation
# Create waypoints file