package main;

public class UAV {
    private String name;
    private String weight;
    private String turnRad;
    private String maxIncline;
    private String battery;
    private String capacity;

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
