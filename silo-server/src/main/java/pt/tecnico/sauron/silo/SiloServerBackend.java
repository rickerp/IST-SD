package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.grpc.ReportRequest;

import java.util.ArrayList;

public class SiloServerBackend {
    private ArrayList<ReportRequest.Observation> observations = new ArrayList<>();
    private ArrayList<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

    }

    public void report(String camera, ArrayList<ReportRequest.Observation> newObservations){
        if(cameras.stream().map(x -> x.getName()).anyMatch(x -> x.equals(camera))){
            observations.addAll(newObservations);
        }
    }
}
