package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;

import pt.tecnico.sauron.silo.domain.exception.CoordinateException;
import pt.tecnico.sauron.silo.domain.exception.SiloArgumentException;

public class Camera {

    private List<ObservationDomain> observations = new ArrayList<>();
    private String name;

    private float latitude;
    private float longitude;

    public Camera(String name, float latitude, float longitude) throws SiloArgumentException, CoordinateException {
        setName(name);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) throws CoordinateException {
        if (latitude < -90 || latitude > 90) {
            throw new CoordinateException("Latitude must be between -90 and 90");
        }
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws SiloArgumentException {
        if (!name.matches("^[a-zA-Z0-9]{3,15}$")) {
            throw new SiloArgumentException("Camera name should be alphanumeric and its length between 3 and 15.");
        }
        this.name = name;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) throws CoordinateException {
        if (longitude < -180 || longitude > 180) {
            throw new CoordinateException("Longitude must be between -180 and 180");
        }
        this.longitude = longitude;
    }
}
