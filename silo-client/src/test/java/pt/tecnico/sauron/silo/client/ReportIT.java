package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.ArrayList;

public class ReportIT extends BaseIT {

    private Observation.Builder observationCar;
    private Observation.Builder observationPerson;
    private final static String cameraName = "camera";

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        observationCar = Observation.newBuilder()
                            .setTarget(Target.CAR)
                            .setId("AA55BB");

        observationPerson = Observation.newBuilder()
                                .setTarget(Target.PERSON)
                                .setId("1");
    }
    

}

