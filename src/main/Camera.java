package main;

/**
 * Camera class used to store properties for a specific
 * camera after parsing.
 */
public class Camera {
    private String name;
    private String sensorX;             // Sensor width in mm
    private String sensorY;             // Sensor height in mm
    private String focalLength;         // Camera focal length in mm
    private String resolution;          // Camera resolution in Megapixels
    private String aspectRatio;         // Camera aspect ratio (X:Y)

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
