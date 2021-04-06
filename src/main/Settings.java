package main;

public class Settings {
    private String uavSpeed;
    private String windSpeed;
    private String windDirection;
    private String sideOverlap;
    private String gsd;
    private String altitude;
    public Settings(){
        this.uavSpeed = "";
        this.windSpeed = "";
        this.windSpeed = "";
        this.sideOverlap = "";
        this.gsd = "";
        this.altitude = "";

    }

    public String getUavSpeed() {
        return uavSpeed;
    }

    public void setUavSpeed(String uavSpeed) {
        this.uavSpeed = uavSpeed;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getSideOverlap() {
        return sideOverlap;
    }

    public void setSideOverlap(String sideOverlap) {
        this.sideOverlap = sideOverlap;
    }

    public String getGsd() {
        return gsd;
    }

    public void setGsd(String gsd) {
        this.gsd = gsd;
    }

    public String  getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }
}
