package main;

public class Camera {
    private String name;
    private String sensorX;
    private String sensorY;
    private String focalLength;
    private String resolution;
    private String aspectRatio;

    public Camera(){
        this.name = "";
        this.sensorX = "";
        this.sensorY = "";
        this.focalLength = "";
        this.resolution = "";
        this.aspectRatio = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSensorX() {
        return sensorX;
    }

    public void setSensorX(String sensorX) {
        this.sensorX = sensorX;
    }

    public String getSensorY() {
        return sensorY;
    }

    public void setSensorY(String sensorY) {
        this.sensorY = sensorY;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
}
