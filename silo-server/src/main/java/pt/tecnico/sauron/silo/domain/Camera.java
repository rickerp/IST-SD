package pt.tecnico.sauron.silo.domain;

public class Camera {
    public String name;
    public int latitude;
    public int longitude;

    public Camera(String name, int latitude, int longitude){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

}
