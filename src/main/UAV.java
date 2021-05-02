package main;

/**
 * UAV class used to store properties for a specific
 * UAV after parsing.
 */

public class UAV {
    private String name;
    private String weight;          // Weight of the UAV in Kg
    private String turnRad;         // Minimum turn radius of the UAV in metres
    private String maxIncline;      // Max incline angle of the UAV in degrees
    private String battery;         // Type of battery
    private String capacity;        // Battery capacity in mAh


    public UAV(){
        this.name = "";
        this.weight = "";
        this.turnRad = "";
        this.maxIncline = "";
        this.battery = "";
        this.capacity = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getTurnRad() {
        return turnRad;
    }

    public void setTurnRad(String turnRad) {
        this.turnRad = turnRad;
    }

    public String getMaxIncline() {
        return maxIncline;
    }

    public void setMaxIncline(String maxIncline) {
        this.maxIncline = maxIncline;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}
