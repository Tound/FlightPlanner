package main;

public class Coordinate {
    public int x;
    public int y;
    public double long_;
    public  double lat;
    public Coordinate(int x,int y,double long_,double lat){
        this.x = x;
        this.y = y;
        this.long_ = long_;
        this.lat = lat;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getLong_() {
        return long_;
    }

    public void setLong_(double long_) {
        this.long_ = long_;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
