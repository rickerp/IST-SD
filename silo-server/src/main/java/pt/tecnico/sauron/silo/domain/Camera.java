package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Camera {

    private List<ObservationDomain> observations = new ArrayList<>();
    private String name;

    private float latitude;
    private float longitude;

    public Camera(String name, float latitude, float longitude) throws SiloException {
        setName(name);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) throws SiloException {
        if (latitude < -90 || latitude > 90) {
            throw new SiloException("Latitude must be between -90 and 90");
        }
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws SiloException {
        if (!name.matches("^[a-zA-Z0-9]{3,15}$")) {
            throw new SiloException("Camera name should be alphanumeric and its length between 3 and 5.");
        }
        this.name = name;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) throws SiloException {
        if (longitude < -180 || longitude > 180) {
            throw new SiloException("Longitude must be between -180 and 180");
        }
        this.longitude = longitude;
    }

    public List<ObservationDomain> getObservations() {
        return observations;
    }

    public List<ObservationDomain> getObjectObservations(Object object) {
        return this.observations.stream()
                .filter(obs -> obs.getObject().equals(object))
                .collect(Collectors.toList());
    }

}
