package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.grpc.Observation;
import pt.tecnico.sauron.silo.grpc.ReportRequest;

import java.util.ArrayList;
import java.util.List;

public class SiloServerBackend {
    private List<Observation> observations = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

    }

    public void report(String camera, List<Observation> newObservations){
        if (cameras.stream().map(x -> x.getName()).anyMatch(x -> x.equals(camera))){
            observations.addAll(newObservations);
        }
    }
}
