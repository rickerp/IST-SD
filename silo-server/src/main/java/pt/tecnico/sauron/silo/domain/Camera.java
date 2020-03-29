package pt.tecnico.sauron.silo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Camera {
    private List<ObservationDomain> observations = new ArrayList<>();
    private String name;
    private float latitude;
    private float longitude;

    public Camera(String name, float latitude, float longitude) {
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

    public List<ObservationDomain> getObservations() {
        return observations;
    }

    public List<ObservationDomain> getObjectObservations(ObservationDomain.Target target, String id) {
        return this.observations.stream()
                .filter(obs -> obs.getTarget() == target && obs.getId().equals(id))
                .collect(Collectors.toList());
    }

    public List<String> getIds(ObservationDomain.Target target, String idLike) {
        return this.observations.stream()
                        .filter(s -> s.getTarget() == target && s.getId().matches(idLike.replace("*", ".*")))
                        .map(obs -> obs.getId())
                        .distinct()
                        .collect(Collectors.toList());
    }
}
