"""
Creates terraces from passes
Created by Thomas Pound
Last updated 25/5/21
"""

import numpy as np
from create_passes_GUI import convertCoords
from Image_Classes import *

image_passes = []

def createTerraces(u,v,altitude_profile,sample_distance,wind_angle,pass_length,image_passes,max_alt_diff,min_terrace_len):
    """
    Splits pass into terraces using the altitude profile
    Params:
        u - Current u value
        v - Current v value
        altitude_profile - The altitude profile for this pass
        wind_angle - The wind direction in radians
        pass_length - The length of the entire pass in metres
        image_passes - The current list of all image passes
        max_alt_diff - The maximum altitude different allowed
        min_terrace_len - The minimum length of a terrace/pass
    Returns:
        image_passes - Updated list of all image passes
    """
    
    lookahead = 3                                       # Initialise how many values to look ahead in altitude profile
    current_terrace = []                                # Initilise current terrace points as empty
    average_alt = 0                                     # Store the average altitude
    current_terrace_length = 0                          # Current length of the terrace in pixels
    if len(altitude_profile) > 0:                       # If there is some altitude data for this pass
        max_altitude = np.max(altitude_profile)         # Store the maximum altitude for the pass
        min_altitude = np.min(altitude_profile)         # Store the minimum altitude for the pass
        if max_altitude - min_altitude < max_alt_diff:  # If the entire pass is within the altitude limits
            # Make entire pass into a terrace of average height
            
            # Find the mean altitude
            for i in range(0,len(altitude_profile)):
                average_alt += altitude_profile[i]
            average_alt = average_alt/len(altitude_profile)
            terrace_start = (u,v,average_alt)
            terrace_end = (u,v+pass_length,average_alt)

            # Convert the terrace coords back to xy and set the start and end points of the terrace
            coords = convertCoords([[terrace_start[0],terrace_start[1]]],wind_angle,'xy')   # Convert coords back to x and y
            terrace_start = (coords[0][0],coords[0][1],average_alt)

            coords = convertCoords([[terrace_end[0],terrace_end[1]]],wind_angle,'xy')       # Convert coords back to x and y
            terrace_end = (coords[0][0],coords[0][1],average_alt)

            # Add new image pass
            image_passes.append(Image_Pass(terrace_start,terrace_end,average_alt,wind_angle))
        else:
            # Calculate terraces
            index = 0

            # Initialise terrace parameters
            current_altitude = -1
            current_min_altitude = -1
            current_max_altitude = -1

            while index < len(altitude_profile):
                if index + lookahead > len(altitude_profile):   # If unable to look ahead 3 samples
                    if current_altitude == -1:              # If no points are currently in the terrace (current altitude is invalid)
                        for alt in range(0,lookahead-1):        # Take average of the rest of the data
                            current_altitude += altitude_profile[index+alt]
                        current_altitude = current_altitude/3

                    # Use current altitude previously calculated from the terrace
                    for val in range(0,lookahead-1):
                        coords = convertCoords([[u,v+(index+val)*sample_distance]],wind_angle,'xy')
                        current_terrace.append([coords[0][0],coords[0][1],current_altitude])    # Add points to terrace at the current altitude
                    
                    # Add all to current pass
                    terrace_start = current_terrace[0]
                    terrace_end = current_terrace[len(current_terrace)-1]
                    image_passes.append(Image_Pass(terrace_start,terrace_end,current_altitude,wind_angle))
                    break

                if len(current_terrace) == 0:
                    # Look ahead to find gradient
                    grad = altitude_profile[index+1] - altitude_profile[index]
                    grad += altitude_profile[index+2] - altitude_profile[index]
                    grad = grad/2               # Take the average of the gradient
                    coords = convertCoords([[u,v+index*sample_distance]],wind_angle,'xy')
                    x = coords[0][0]
                    y = coords[0][1]
                    if grad > 0:
                        current_altitude = altitude_profile[index]+max_alt_diff/2
                        current_terrace.append([x,y,current_altitude])
                    elif grad < 0:
                        current_altitude = altitude_profile[index]-max_alt_diff/2
                        current_terrace.append([x,y,current_altitude])
                    else:
                        current_altitude = altitude_profile[index]
                        current_terrace.append([x,y,current_altitude])

                    current_terrace_length += sample_distance
                    current_min_altitude = current_altitude - max_alt_diff/2
                    current_max_altitude = current_altitude + max_alt_diff/2

                else:
                    # Add to the terrace
                    # If the altitude value is within limits
                    if altitude_profile[index] > current_min_altitude and  altitude_profile[index] < current_max_altitude:
                        coords = convertCoords([[u,v+index*sample_distance]],wind_angle,'xy')
                        x = coords[0][0]
                        y = coords[0][1]
                        current_terrace.append([x,y,current_altitude])
                        current_terrace_length += sample_distance
                    # If the next altitude value is within limits
                    elif altitude_profile[index+1] > current_min_altitude and altitude_profile[index+1] < current_max_altitude:
                        coords = convertCoords([[u,v+index*sample_distance]],wind_angle,'xy')
                        x = coords[0][0]
                        y = coords[0][1]
                        current_terrace.append([x,y,current_altitude])  # Add current point
                        index += 1
                        coords = convertCoords([[u,v+index*sample_distance]],wind_angle,'xy')
                        x = coords[0][0]
                        y = coords[0][1]
                        current_terrace.append([x,y,current_altitude])  # Add next point
                        current_terrace_length += 2*sample_distance
                    # If the following altitude value is within limits
                    elif altitude_profile[index+2] > current_min_altitude and altitude_profile[index+2] < current_max_altitude:
                        for val in range(0,2):
                            coords = convertCoords([[u,v+(index+val)*sample_distance]],wind_angle,'xy')
                            x = coords[0][0]
                            y = coords[0][1]
                            current_terrace.append([x,y,current_altitude])  # Add current, next and following point to the terrace
                            index += 1
                        current_terrace_length += lookahead*sample_distance
                        index -= 1
                    else:
                        if current_terrace_length > min_terrace_len:
                            # Create new terrace
                            terrace_start = current_terrace[0]
                            terrace_end = current_terrace[len(current_terrace)-1]
                            image_passes.append(Image_Pass(terrace_start,terrace_end,current_altitude,wind_angle))
                            current_terrace = []        # Initialise the array of terrace points
                            current_altitude = -1       # Initialise the terrace altitude to -1
                            current_terrace_length = 0
                        else:
                            # Requires more image locations
                            print("Not long enough")
                            coords = convertCoords([[u,v+index*sample_distance]],wind_angle,'xy')
                            x = coords[0][0]
                            y = coords[0][1]
                            current_terrace.append([x,y,current_altitude])  # Add point to terrace
                            current_terrace_length += sample_distance

                index += 1
    else:
        pass
    return image_passes
