package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SiloServerBackend {

    private final List<ObservationDomain> observations = new ArrayList<>();
    private final List<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

    }

    public synchronized void clear() {
        observations.clear();
        cameras.clear();
    }

    public void report(List<ObservationDomain> newObservations) throws SiloException {
        synchronized (observations) {
            observations.addAll(newObservations);
        }
    }

    public Optional<Camera> getCamera(String cameraName) {
        synchronized (cameras) {
            return cameras.stream().filter(x -> x.getName().equals(cameraName)).findFirst();
        }
    }

    public void camJoin(String name, float latitude, float longitude) {
        synchronized (cameras) {
            getCamera(name).ifPresentOrElse(cam -> {
                if (cam.getLatitude() != latitude || cam.getLongitude() != longitude) {
                    throw new SiloException("Camera with name " + name + " already exists.");
                }
            }, () -> cameras.add(new Camera(name, latitude, longitude)));
        }
    }

    public List<ObservationDomain> trace(ObservationObject object) {
        synchronized (observations) {
            return observations.stream()
                    .filter(s -> s.getObservationObject().equals(object))
                    .sorted(Comparator.comparing(ObservationDomain::getTimestamp).reversed())
                    .collect(Collectors.toList());
        }
    }

    public Optional<ObservationDomain> track(ObservationObject object) {
        return trace(object).stream().findFirst();
    }

    public List<ObservationDomain> trackMatch(Class<? extends ObservationObject> targetType, String idLike) {
        synchronized (observations) {
            return observations.stream()
                    .map(ObservationDomain::getObservationObject)
                    .filter(s -> s.getClass().equals(targetType) && s.getStringId()
                    .matches(idLike.replace("*", ".*")))
                    .distinct()
                    .map(this::track)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }
}
