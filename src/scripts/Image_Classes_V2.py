# These classes are to be used with the Efficient flight planning software
# created by Thomas Pound for the MEng Project
# The classes include an Image_Pass and Image_Location

import math
from dubins_3D import *

G = 9.81    # Acceleration caused by gravity in m/s^2

class Image_Pass:
    """
    Config is used to determine whether the pass is in the standard direction 
    or to be reversed.
    If config is True, the pass should be used as normal
    If config is False, the pass should be used in reverse
    """
    def __init__(self,start,end,altitude,wind_angle):
        self.wind_angle = wind_angle
        self.energy = [None,None]
        self.length = None
        self.heading = [None,None]
        self.start = start
        self.end = end
        self.altitude = altitude

    def getStart(self,config):  # Obtain start location of a pass for a particular configuration
        if config:
            return self.start
        else:
            return self.end

    def getEnd(self,config):    # Get end location of the pass for a known configuration
        if config:
            return self.end
        else:
            return self.start

    def getLength(self):
        """
        Get length of pass for a known configuration
        """
        if self.length is None:
            length = 0

            dx = self.end[0] - self.start[0]
            dy = self.end[1] - self.start[1]
            dz = self.end[2] - self.start[2]
            length += math.sqrt(dx*dx + dy*dy + dz*dz)
            self.length = length
        return self.length

    def getEnergy(self,config,routemanager):
        """
        Get the energy needed to traverse the pass
        """
        if config:
            if self.energy[0] is None:
                energy = 0
                dx = self.end[0] - self.start[0]
                dy = self.end[1] - self.start[1]
                dz = self.end[2] - self.start[2]

                if dz>0:
                    if math.atan2(dz,math.sqrt(dx*dx +dy*dy)) > routemanager.max_grad:
                        print("PASS IS TOO STEEP!")
                    gpe = routemanager.uav_mass*G*dz
                    energy += math.sqrt(dx*dx +dy*dy + dz*dz) + gpe
                else:
                    energy += math.sqrt(dx*dx +dy*dy)
                self.energy[0] = energy
            return self.energy[0]

        else:
            if self.energy[1] is None:
                energy = 0
                dx = self.start[0] - self.end[0]
                dy = self.start[1] - self.end[1]
                dz = self.start[2] - self.end[2]
                if dz>0:
                    if math.atan2(dz,math.sqrt(dx*dx +dy*dy)) > routemanager.max_grad:
                        print("PASS IS TOO STEEP!")
                    gpe = routemanager.uav_mass*G*dz
                    energy += math.sqrt(dx*dx +dy*dy + dz*dz) + gpe
                else:
                    energy += math.sqrt(dx*dx +dy*dy)
                self.energy[1] = energy
            return self.energy[1]

    def __repr__(self):
        string = ''
        string += f"({self.start},{self.end},{self.altitude}),"
        return string[:-1]

    def getHeading(self,config):
        """
        Retrieve or calculate the heading angle for any config
        Uses the direction of the pass to calculate the heading angle
        Parameters:
            config  - Boolean value to choose which direction the pass should be traversed
                      True = Normal direction
                      False = Reverse Direction
        Returns:
            The heading for the specified configuration
        """
        if config:
            if self.heading[0] is None:
                if self.start == self.end :
                    heading = self.wind_angle - math.pi/2
                    if heading >= 2*math.pi:
                        heading -= 2*math.pi
                    elif heading <= -2*math.pi:
                        heading += 2*math.pi

                    self.heading[0] = heading
                    return self.heading[0]

                else:
                    dx = self.end[0] - self.start[0]
                    dy = self.end[1] - self.start[1]
                    heading = math.atan2(dy,dx)

                    self.heading[0] = heading
            return self.heading[0]

        else:
            if self.heading[1] is None:
                if self.start == self.end:
                    self.heading[1] = self.wind_angle + math.pi/2
                    return self.heading[1]
                else:
                    dx = self.start[0] - self.end[0]
                    dy = self.start[1] - self.end[1]
                    heading = math.atan2(dy,dx)

                    self.heading[1] = heading
            return self.heading[1]

    def energyTo(self,config,other_pass,other_pass_config,routemanager):
        """
        Calculate the energy require to traverse between two passes
        Parameters:
            config              - Configuration of pass for direction of traversal
            other_pass          - The next pass that on the route
            other_pass_config   - The configuration of the next pass in the route, decides the direction of traversal
            routemanger         - Routemanager that holds all the flight settings

        Returns:
            energy              - The respective energy to travel to the next pass
            dpath               - The dubins path required to link the two passes
        """
        d_path = None        
        # Add spiral?
        end = self.getEnd(config)
        start = other_pass.getStart(other_pass_config)
        q0 = (end[0],end[1],end[2],self.getHeading(config))
        q1 = (start[0],start[1],start[2],other_pass.getHeading(other_pass_config))

        d_path = dubins_shortest_path(q0,q1,routemanager.min_turn)

        dz = end[2] - start[2]
        if dz > 0:
            # Check if too steep
            if math.atan2(dz, d_path.length_3d()) > routemanager.max_grad:
                print("DUBINS IS TOO STEEP!")
            altEnergy = routemanager.uav_mass*G* dz
        else:
            altEnergy = 0 # If the next point is below the current

        if d_path.length() == 0:
            d_path = None
            energy = 0
        else:
            energy = d_path.length() + altEnergy

        return energy,d_path
