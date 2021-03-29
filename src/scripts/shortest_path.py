# Shortest path
# Use TSP to find shortest route
from Passes_TSP import *


# Add altitude to passes and make terraces
f = open("src/intermediate/altitude.txt")
f.close()

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