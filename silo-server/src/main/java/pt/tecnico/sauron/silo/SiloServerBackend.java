package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationDomain;

import java.util.ArrayList;
import java.util.List;


public class SiloServerBackend {
    private List<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

    }

    public void clear() {
        cameras.clear();
    }

    public boolean report(String cameraName, List<ObservationDomain> newObservations) {
        Camera camera = getCamera(cameraName);
        if (camera == null) {
            return false;
        }
        camera.getObservations().addAll(newObservations);
        return true;
    }

    public Camera getCamera(String cameraName) {
        return cameras.stream().filter(x -> x.getName().equals(cameraName)).findFirst().orElse(null);
    }

    public boolean camJoin(String name, float latitude, float longitude) {
        System.out.println("joining " + name);
        if (getCamera(name) != null) {
            return false;
        }
        cameras.add(new Camera(name, latitude, longitude));
        return true;
    }
}
