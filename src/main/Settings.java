package main;

/**
 * Settings class used to store properties for the flight settings
 */
public class Settings {
    private String uavSpeed;            // UAV speed in m/s
    private String windSpeed;           // Wind speed in m/s
    private String windDirection;       // Wind direction in radians
    private String sideOverlap;         // Side overlap as a percentage
    private String gsd;                 // Ground sample distance in cm/px
    private String altitude;            // Altitude above ground

    public Settings(){
        // Initialise strings
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
