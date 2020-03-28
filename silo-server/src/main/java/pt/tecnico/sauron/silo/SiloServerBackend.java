package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.grpc.Observation;
import pt.tecnico.sauron.silo.grpc.ReportRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiloServerBackend {
    private List<Observation> observations = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

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

    public void report(String camera, List<Observation> newObservations) {
        if (cameras.stream().map(x -> x.getName()).anyMatch(x -> x.equals(camera))){
            observations.addAll(newObservations);
        }
    }
}
