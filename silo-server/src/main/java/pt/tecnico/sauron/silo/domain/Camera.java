package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;

public class Camera {
    private String name;
    private int latitude;
    private int longitude;
    private List<ObservationDomain> observations = new ArrayList<>();

    public Camera(String name, int latitude, int longitude) {
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

    public List<ObservationDomain> getObservations() {
        return observations;
    }

}
