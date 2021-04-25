# Create Passes
import math
import numpy as np
import shapely.geometry as sg
from shapely import affinity
from dubins_3D import *
from scipy.interpolate import griddata
from Image_Classes import *

import json

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

def coverage_check(heading_angle,start_u,pass_shift,coverage_width,coverage_height):
    """
    Ensures that the heading angle does not affect complete coverage of the area
    Params:
        heading_angle - The heading angle of the UAV in radians
        start_u - The current u value
        pass_shift - The u shift of the passes
        coverage_width - Coverage width of the image footprint in metres
        coverage_height - Coverage height of the image footprint in metres
    Returns:
        complete_coverage -  Boolean value indicating whether the coverage of the image footprints is complete
    """
    complete_coverage = True
    # Create edges
    area_bound_point = start_u
    # Find center points of 2 camera footprints that are placed on top of each other (0% overlap)
    center1 = (start_u + coverage_width/2 + pass_shift,0)
    center2 = (start_u + coverage_width/2 + pass_shift,coverage_height)
    # Create path for first camera footprint
    footprint1 = sg.Polygon([(center1[0] - coverage_width/2,center1[1] + coverage_height/2),
                            (center1[0] + coverage_width/2,center1[1] + coverage_height/2),
                            (center1[0] + coverage_width/2,center1[1] - coverage_height/2),
                            (center1[0] - coverage_width/2,center1[1] - coverage_height/2)])
    # Create path for second camera footprint
    footprint2 = sg.Polygon([(center2[0] - coverage_width/2,center2[1] + coverage_height/2),
                            (center2[0] + coverage_width/2,center2[1] + coverage_height/2),
                            (center2[0] + coverage_width/2,center2[1] - coverage_height/2),
                            (center2[0] - coverage_width/2,center2[1] - coverage_height/2)])
    # Rotate footprints accordingly
    rotated_footprint1 = affinity.rotate(footprint1,heading_angle,center1,use_radians=True)
    rotated_footprint2 = affinity.rotate(footprint2,heading_angle,center2,use_radians=True)

    # Get intersect of footprints
    intersection_points = rotated_footprint1.intersection(rotated_footprint2)

    # If coverage is complete
    # Get vertices
    # Sutherland-Hodgman for zero overlap
    u,v = intersection_points.exterior.xy
    sorted_points = sorted(u)
    if sorted_points[0] > start_u:
        complete_coverage = False

    return complete_coverage

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

    data = open("src/intermediate/settings.json",'r')
    settings = json.load(data)
    data.close()

    data = open("src/intermediate/settings.json",'w')

    # Find camera footprint size
    # If GSD is available
    if config.altitude is None and config.ground_sample_distance is not None:
        coverage_width = config.ground_sample_distance * camera.image_x
        coverage_height = config.ground_sample_distance * camera.image_y

        uav_altitude = coverage_width *camera.focal_length/camera.sensor_x
        max_uav_alt = (camera.image_x * (config.ground_sample_distance + config.ground_sample_distance/10)) * camera.focal_length/camera.sensor_x
        min_uav_alt = (camera.image_x * (config.ground_sample_distance - config.ground_sample_distance/10)) * camera.focal_length/camera.sensor_x

        config.altitude = uav_altitude

    # If altitude is available
    elif config.altitude is not None and config.ground_sample_distance is None:
        coverage_width = (camera.sensor_x*config.altitude/camera.focal_length)  # In meters
        coverage_height = (camera.sensor_y*config.altitude/camera.focal_length) # In meters

        uav_altitude = config.altitude

        ground_sample_distance = uav_altitude*camera.sensor_x/(camera.focal_length * camera.image_x)    # Coverage width / image width
        config.ground_sample_distance = ground_sample_distance
        max_uav_alt = (camera.image_x * (ground_sample_distance + config.ground_sample_distance/10)) * camera.focal_length/camera.sensor_x # In meters
        min_uav_alt = (camera.image_x * (ground_sample_distance - config.ground_sample_distance/10)) * camera.focal_length/camera.sensor_x # In meters

    # If both have been initialised
    elif config.altitude is not None and config.ground_sample_distance is not None:     
        coverage_width = (camera.sensor_x*config.altitude/camera.focal_length)          # In meters
        coverage_height = (camera.sensor_y*config.altitude/camera.focal_length)         # In meters

        # Look for conflicts
        uav_altitude = config.altitude                                                  # In meters
        ground_sample_distance = uav_altitude*camera.sensor_x/(camera.focal_length * camera.image_x)
        if ground_sample_distance != config.ground_sample_distance:
            print(f"Conflict with GSD, taking altitude as true. New GSD: {ground_sample_distance}")
            config.ground_sample_distance = ground_sample_distance  # Sd
        
        max_uav_alt = (camera.image_x * (config.ground_sample_distance + config.ground_sample_distance/10)) * camera.focal_length/camera.sensor_x # In meters
        min_uav_alt = (camera.image_x * (config.ground_sample_distance - config.ground_sample_distance/10)) * camera.focal_length/camera.sensor_x # In meters

    else:
        print("Requires atleast one value of altitude or gsd")

    settings['altitude'] = f"{config.altitude}" # Update altitude in settings json file
    settings['gsd'] = f"{config.ground_sample_distance}" # Update gsd in settings json file

    distance_between_photos_width = coverage_width - coverage_width*config.side_overlap

    # Check if UAV is likely to enter an NFZ
    if coverage_height/2 > config.uav.min_turn and len(NFZs) > 0:
        print("Warning - At this altitude the UAV will mostly likely fly into an NFZ\n " + 
        " as the minimum turn radius is larger than the area between the end of passes\n "+ 
        " and the NFZ boundary. Consider increasing altitude or the ground sample_resolution.")

    # Obtain properties about the area
    sorted_vertices = sorted(new_area_coords, key=lambda u:u[0])
    length_of_area = config.scale * abs(sorted_vertices[len(sorted_vertices)-1][0] - sorted_vertices[0][0]) # In meters

    np_area = np.array(new_area_coords)
    max_height = np.max(np_area[:,1])           # Store highest V value
    min_height = np.min(np_area[:,1])           # Store lowest V value
    start_u = np.min(np_area[:,0])
    
    # Need scaling for calculations and drawing need pixels in meters by using scale

    number_of_passes = (length_of_area-config.side_overlap*coverage_width)/distance_between_photos_width    # All in meters

    # If the number of passes required is not an integer, add another to ensure coverage
    if number_of_passes % 1 > 0: # If number of passes is not an integer to create complete coverage
        number_of_passes = int(number_of_passes+1)
        # Center the passes
        # Create shift value to center the passes
        remainder = length_of_area - (number_of_passes * coverage_width - (number_of_passes-1)*config.side_overlap*coverage_width) 
        pass_shift = remainder/2
    else:
        pass_shift = 0
        print("Passes are integer value and therefore there is no overlap and do no require shifting")


    # Check if wind is present, if so the heading angle will shift and so will the coverage
    if config.wind[0] > 0:
        if not coverage_check(config.uav.heading_angle,start_u,pass_shift,coverage_width,coverage_height):   # Check if coverage is still complete
            print("Coverage is no longer complete, adding another pass")
            # Check if another one is required
            # If another pass is required, add another then recenter the passes
            number_of_passes += 1
            remainder = length_of_area - (number_of_passes * coverage_width - (number_of_passes-1)*config.side_overlap*coverage_width) 
            pass_shift = remainder/2
        else:
            print("Another pass is not required due to the current configuration allowing complete coverage")
    else:
        print("There is no wind and therefore no heading angle compensation")

    # Create polygon and NFZ paths to check for intersections with passes
    polygon_path = sg.Polygon(new_area_coords)
    NFZ_paths = []
    for NFZ in new_NFZs:
        NFZ_path = sg.Polygon(NFZ)
        NFZ_paths.append(NFZ_path)

    # Set maximum altitude difference
    max_alt_diff = max_uav_alt - min_uav_alt

    settings['max_alt_diff'] = max_alt_diff
    json.dump(settings,data,indent=4)
    data.close()

    pass_file = open("src/intermediate/passes.txt",'w')
    pass_file.write(f"ALTITUDE\t{uav_altitude}\t{max_alt_diff}\n")
    pass_file.write(f"GSD\t{config.ground_sample_distance}\n")

    passes_data =  open("src/intermediate/passes.json","w")
    data = {}
    data['passes'] = []

    # Shift passes to allow for even distribution
    u = start_u + (coverage_width/2 + pass_shift)/config.scale  # In pixels
    for i in range(0,int(number_of_passes)):        # Cycle through all full-length passes across entirety of area
        # Find points where passes intersect with ROI
        intersection_points = []
        pass_edge = sg.LineString([(u,min_height-1),(u,max_height+1)])

        # Fixed bug with edge crossing, tried removing duplicates
        intersection = polygon_path.intersection(pass_edge)
        if intersection.is_empty:   # If there is no intersection with the ROI, must be an error
            print("Pass did not intersect anywhere on the ROI")
            exit(1)
        elif type(intersection) == sg.Point:        # If the intersection is at a single point (Corner of the path), add the point twice
            intersection_points.append([intersection.x,intersection.y])
            intersection_points.append([intersection.x,intersection.y])
        elif type(intersection) == sg.LineString:   # If the intersection is multiple coordinates, cycle through and add all
            for point in intersection.coords:
                intersection_points.append([point[0],point[1]])

        # Find where passes intersect with NFZs
        for NFZ_path in NFZ_paths:
            intersection = NFZ_path.intersection(pass_edge)
            if intersection.is_empty:   # If there is no intersection with an NFZ, continue
                continue
            elif type(intersection) == sg.Point:        # If the intersection is at a single point (Corner of the path), add the point twice
                intersection_points.append([intersection.x,intersection.y])
                intersection_points.append([intersection.x,intersection.y])
            elif type(intersection) == sg.LineString:   # If the intersection is multiple coordinates, cycle through and add all
                for point in intersection.coords:
                    intersection_points.append([point[0],point[1]])

        points_on_pass = sorted(intersection_points,key=lambda point:point[1])   # List vertically
        subpasses = len(points_on_pass)/2

        for j in range(0,int(subpasses)):   # Split full length passes into sub passes if obstacles are found
            start = points_on_pass[j*2]
            end = points_on_pass[j*2 + 1]
            pass_length = getDistance(start,end)-coverage_height/config.scale   # Pixel values

            if pass_length <= config.min_pass_length:               # Check to see if pass is to size
                print("Pass length is too small")                   # If pass is too small, continue
                continue

            v = start[1] + coverage_height/(2 * config.scale)  # Shift pass up by half the size of an image height

            start = convertCoords([[u,v]],wind_angle,'xy')
            pass_file.write(f"{start[0][0]},{start[0][1]}\n")

            end = convertCoords([[u,v+pass_length]],wind_angle,'xy')
            pass_file.write(f"{end[0][0]},{end[0][1]}\n")

            data['passes'].append({
                'start':f'{start[0][0]},{start[0][1]}',
                'end':f'{end[0][0]},{end[0][1]}',
                'length':f'{pass_length}'
            })

        u += distance_between_photos_width/config.scale          # Increase U value on each loop

    pass_file.close()
    
    json.dump(data,passes_data,indent=4)
    passes_data.close()

    print(f"length = {length_of_area}, px length = {length_of_area/config.scale}")
    print(f"Footprint of image: {coverage_width}x{coverage_height}")
    print(f"Distance between passes: {round(distance_between_photos_width,2)} m")
    print(f"number_of_passes = {number_of_passes}")
    print(f"Min altitude: {round(min_uav_alt,2)} m\nMax altitude: {round(max_uav_alt,2)}m"
            +f"\nDesired altitude: {round(uav_altitude,2)}")
