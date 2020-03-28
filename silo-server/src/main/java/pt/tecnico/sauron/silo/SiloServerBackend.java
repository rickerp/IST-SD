package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationDomain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SiloServerBackend {
    private List<Camera> cameras = new ArrayList<>();

    public SiloServerBackend() {

    }

    public void report(String cameraName, List<ObservationDomain> newObservations) {
        List<Camera> filteredCameras = cameras.stream()
                .filter(x -> x.getName().equals(cameraName))
                .collect(Collectors.toList());

        if (filteredCameras.size() == 1)
            filteredCameras.get(0).getObservations().addAll(newObservations);

    }
}
