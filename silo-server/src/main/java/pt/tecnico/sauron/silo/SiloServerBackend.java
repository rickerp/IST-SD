package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SiloServerBackend {
    private List<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

    }

    public void clear() {
        cameras.clear();
    }

    public boolean report(String cameraName, List<ObservationDomain> newObservations) throws SiloException {
        Camera camera = getCamera(cameraName);
        if (camera == null) {
            throw new SiloException("Camera does not exist with name " + cameraName + ".");
        }
        camera.getObservations().addAll(newObservations);
        return true;
    }

    public Camera getCamera(String cameraName) {
        return cameras.stream().filter(x -> x.getName().equals(cameraName)).findFirst().orElse(null);
    }

    public void camJoin(String name, float latitude, float longitude) throws SiloException {
        if (getCamera(name) != null) {
            throw new SiloException("Camera with name " + name + " already exists.");
        }
        cameras.add(new Camera(name, latitude, longitude));
    }

    public ObservationDomain track(Object object) throws SiloException {
        return cameras.stream()
                .map(cam -> cam.getObjectObservations(object))
                .flatMap(List::stream)
                .max(Comparator.comparing(ObservationDomain::getTimestamp))
                .orElseThrow(() -> new SiloException("No observations found."));
    }

    public List<ObservationDomain> trackMatch(Class<? extends ObservationObject> targetType, String idLike) {
        return cameras.stream()
                        .map(cam -> cam.getObjects(targetType, idLike))
                        .flatMap(List::stream)
                        .distinct()
                        .map(this::track)
                        .collect(Collectors.toList());
                    
    }
    
}
