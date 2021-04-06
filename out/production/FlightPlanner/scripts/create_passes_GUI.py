# Create Passes
import math
import numpy as np
import shapely.geometry as sg
from dubins_3D import *
from scipy.interpolate import griddata
from Image_Classes_V2 import *

import time

G = 9.81

class Edge:
    """
    Edge class used to test whether or not edges intercept
    parameters:
        x1  - x coordinate of start point
        y1  - y coordinate of start point
        x2  - x coordinate of end point
        y2  - y coordinate of end point
    A linestring is produced from the shapely.geometry package to represent an edge
    """
    def __init__(self,x1,y1,x2,y2):
        self.x1 = x1
        self.y1 = y1
        self.x2 = x2
        self.y2 = y2
        self.edge = sg.LineString([(x1,y1),(x2,y2)])

    def getEdge(self):
        return self.edge

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

def getDistance(point1,point2):
    """
    Get the distance between two points in 2D
    parameters:
        point1 - First 2D coordinate
        point2 - Second 2D coordinate
    returns:
        Distance between points using pythagorus
    """
    dx = point2[0]-point1[0]
    dy = point2[1]-point1[1]
    return math.sqrt(dy*dy + dx*dx)

def convertCoords(vertices,angle,coord_system):
    """
    Performs coordinate transformatin between uv <=> xy
    Using   [u] = [ cos(theta) + sin(theta)][x]
            [v]   [-sin(theta) + cos(theta)][y]
    """
    theta = angle
    new_coords = []
    # Matrix multiplication?
    if coord_system == 'uv':
        for vertex in vertices:
            u = vertex[0]*math.cos(theta) + vertex[1]*math.sin(theta)
            v = -vertex[0]*math.sin(theta) + vertex[1]*math.cos(theta)
            new_coord = [u,v]
            new_coords.append(new_coord)
    elif coord_system == 'xy':
        scaler = 1/(math.cos(theta) * math.cos(theta) + math.sin(theta) * math.sin(theta))
        for vertex in vertices:
            x = scaler*(vertex[0]*math.cos(theta) - vertex[1]*math.sin(theta))
            y = scaler*(vertex[0]*math.sin(theta) + vertex[1]*math.cos(theta))
            new_coord = [x,y]
            new_coords.append(new_coord)
    else:
        print("Unknown coord system - Choose either 'xy' or 'uv'")
    return new_coords

def createPasses(area,polygon_edges,NFZs,config):
    """
    Create passes across specified area for known area
    Parameters
        area            - Vertices to chosen area of interest
        polygon_edges   - All edges of the polygons
        NFZs            - 2D array of vertices for any NFZs
        config          - Configuration containing settings for the flight
    """
    camera = config.camera
    wind_angle = config.wind[1]

    #image_passes = []   # Initialise list of image_passes as empty

    # IF WINDLESS
    if config.wind[0] == 0:
        # Find the largest edge
        length = 0
        largest_edge = None
        for edge in polygon_edges:
            edge_length = edge.getEdge().length
            if edge_length > length:
                length = edge_length
                largest_edge = edge.getEdge()
        coords = largest_edge.coords
        dx = coords[1][0] - coords[0][0]
        dy = coords[1][1] - coords[0][1]
        wind_angle = math.atan2(dy,dx) + math.pi/2
        if wind_angle >= 2*math.pi:
            wind_angle -= 2*math.pi
        elif wind_angle < 0:
            wind_angle += 2*math.pi
        print(f"New wind angle: {wind_angle}")


    # Update coordinate system
    new_area_coords = convertCoords(area,wind_angle,'uv')
    new_NFZs = []
    for NFZ in NFZs:
        new_NFZ_coords = convertCoords(NFZ,wind_angle,'uv')
        new_NFZs.append(new_NFZ_coords)



    # Find footprint size
    if config.altitude is None:
        coverage_width = config.scale * (config.coverage_resolution * camera.image_x)
        coverage_height = config.scale * (config.coverage_resolution * camera.image_y)

        uav_altitude = coverage_width *camera.focal_length/camera.sensor_x
        max_uav_alt = config.scale * (camera.image_x * (config.coverage_resolution + 0.0015)) * camera.focal_length/camera.sensor_x
        min_uav_alt = config.scale * (camera.image_x * (config.coverage_resolution - 0.0015)) * camera.focal_length/camera.sensor_x

    else:
        coverage_width = config.scale * (camera.sensor_x*config.altitude/camera.focal_length)
        coverage_height = config.scale * (camera.sensor_y*config.altitude/camera.focal_length)

        uav_altitude = config.altitude

        ground_sample_distance = uav_altitude*camera.sensor_x/(camera.focal_length * camera.image_x)

        max_uav_alt = config.scale * (camera.image_x * (ground_sample_distance + 0.0015)) * camera.focal_length/camera.sensor_x
        min_uav_alt = config.scale * (camera.image_x * (ground_sample_distance - 0.0015)) * camera.focal_length/camera.sensor_x

    distance_between_photos_width = coverage_width - coverage_width*config.side_overlap

    # Obtain properties about the area
    sorted_vertices = sorted(new_area_coords, key=lambda u:u[0])
    length_of_area = sorted_vertices[len(sorted_vertices)-1][0] - sorted_vertices[0][0]

    np_area = np.array(new_area_coords)
    max_height = np.max(np_area[:,1])           # Store highest V value
    min_height = np.min(np_area[:,1])           # Store lowest V value
    start_u = np.min(np_area[:,0])
    
    number_of_passes = (length_of_area-config.side_overlap*coverage_width)/distance_between_photos_width
    # Overlap must be above 0 or the pass shift will not work
    if number_of_passes % 1 > 0 or config.wind[0] > 0: # If number of passes is not an integer to create complete coverage
        number_of_passes +=1 # If the number of passes is an integer and wind is present and the side overlap is above
        # Create shift value to center the passes
        remainder = length_of_area - (int(number_of_passes) * coverage_width - int(number_of_passes-1)*config.side_overlap*coverage_width) 
        pass_shift = remainder/2
    else:
        print("Shift is not required due to no wind and exact amount of passes")
        pass_shift = 0

    polygon_edges = []
    NFZ_edges = []

    # Create all new polygon edges
    for i in range(0,len(new_area_coords)):
        polygon_edges.append(Edge(new_area_coords[i-1][0],new_area_coords[i-1][1],
                                    new_area_coords[i][0],new_area_coords[i][1]))
    
    # Create all new NFZ edges
    for NFZ_coords in new_NFZs:
        for i in range(0,len(NFZ_coords)):
            NFZ_edges.append(Edge(NFZ_coords[i-1][0],NFZ_coords[i-1][1],
                                    NFZ_coords[i][0],NFZ_coords[i][1]))

    # Set terrace properties
    # Min terrace length
    min_length = 10
    max_alt_diff = max_uav_alt - min_uav_alt

    pass_file = open("src/intermediate/passes.txt",'w')

    # Shift passes to allow for even distribution
    u = start_u + coverage_width/2 + pass_shift
    for i in range(0,int(number_of_passes)):        # Cycle through all full-length passes across entirety of area
        # Find points where passes intersect with ROI
        intersection_points = []
        pass_edge = sg.LineString([(u,min_height-1),(u,max_height+1)])
        max_intersect = (-math.inf,-math.inf)
        min_intersect = (math.inf,math.inf)
        for edge in polygon_edges:
            intersection = edge.getEdge().intersection(pass_edge)
            if intersection.is_empty:
                continue
            if type(intersection) == sg.Point:
                if intersection.y >= max_intersect[1]:               # Even though we are using uv axis, shapely requires xy
                    max_intersect = [intersection.x,intersection.y]
                if intersection.y <= min_intersect[1]:
                    min_intersect = [intersection.x,intersection.y]
                intersection_points.append([intersection.x,intersection.y])
            elif type(intersection) == sg.LineString:
                for point in intersection.coords:
                    if point[1] >= max_intersect[1]:
                        max_intersect = [point[0],point[1]]
                    if point[1] <= min_intersect[1]:
                        min_intersect = [point[0],point[1]]
                intersection_points.append([intersection.x,intersection.y])

        total_pass_length = getDistance(min_intersect,max_intersect)    # Pass length for entire area

        # Find where passes interesect with NFZs
        for edge in NFZ_edges:        # Cycle through all NFZs
            intersection = edge.getEdge().intersection(pass_edge)
            if intersection.is_empty:
                continue
            else:
                intersection_points.append([intersection.x,intersection.y])

        points_on_pass = sorted(intersection_points,key=lambda point:point[1])   # List vertically
        subpasses = len(points_on_pass)/2

        for j in range(0,int(subpasses)):   # Split full length passes into sub passes if obstacles are found
            start = points_on_pass[j*2]
            end = points_on_pass[j*2 + 1]
            pass_length = getDistance(start,end)-coverage_height

            start_v = start[1] + coverage_height/2  # Shift pass up by half the size of an image height

            v = start_v
            #pass_file.write("NEW PASS\n")
            coords = convertCoords([[u,v]],wind_angle,'xy')
            pass_file.write(f"{coords[0][0]},{coords[0][1]}\n")
            coords = convertCoords([[u,v+pass_length]],wind_angle,'xy')
            pass_file.write(f"{coords[0][0]},{coords[0][1]}\n")
            #image_passes = createTerraces(u,v,altitude_profile,wind_angle,pass_length,image_passes,max_alt_diff,min_length)


            # Must make sure TSP does not go through land or through NFZ
        u += distance_between_photos_width          # Increase U value on each loop

    pass_file.close()

    print(f"length = {length_of_area}")
    print(f"Footprint of image: {coverage_width}x{coverage_height}")
    print(f"Distance between passes: {round(distance_between_photos_width,2)} m")
    print(f"number_of_passes = {number_of_passes}")
    print(f"Min altitude: {round(min_uav_alt,2)} m\nMax altitude: {round(max_uav_alt,2)}m"
            +f"\nDesired altitude: {round(uav_altitude,2)}")
