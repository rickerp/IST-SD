package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.domain.exception.CamAlreadyExistsException;
import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;
import pt.tecnico.sauron.silo.domain.exception.CamNotFoundException;
import pt.tecnico.sauron.silo.domain.exception.CoordinateException;
import pt.tecnico.sauron.silo.domain.exception.SiloArgumentException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SiloServerBackend {

    private final List<ObservationDomain> observations = new ArrayList<>();
    private final List<Camera> cameras = new ArrayList<>();

    public synchronized void clear() {
        observations.clear();
        cameras.clear();
    }

    public void report(List<ObservationDomain> newObservations) {
        synchronized (observations) {
            observations.addAll(newObservations);
        }
    }

    public Camera getCamera(String cameraName) throws CamNotFoundException {
        synchronized (cameras) {
            return cameras.stream()
                    .filter(x -> x.getName().equals(cameraName))
                    .findFirst()
                    .orElseThrow(() -> new CamNotFoundException(cameraName));
        }
    }

    public void camJoin(String name, float latitude, float longitude) throws CamAlreadyExistsException, CoordinateException, SiloArgumentException {
        synchronized (cameras) {
            try {
                Camera camera = getCamera(name);
                if (camera.getLatitude() != latitude || camera.getLongitude() != longitude) {
                    throw new CamAlreadyExistsException(name);
                }
            } catch (CamNotFoundException e) {
                cameras.add(new Camera(name, latitude, longitude));
            }
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
