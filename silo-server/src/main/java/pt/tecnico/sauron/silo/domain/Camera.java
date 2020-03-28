package pt.tecnico.sauron.silo.domain;

public class Camera {
    public String name;
    public float latitude;
    public float longitude;

    public Camera(String name, float latitude, float longitude){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

}
