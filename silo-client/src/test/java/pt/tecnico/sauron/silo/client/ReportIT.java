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
    

    @Test
    public void reportShouldFailWithUnknownCameraName() {
        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.report(
                    ReportRequest.newBuilder()
                            .setCameraName("notACamera")
                            .addObservations(observationCar.build())
                            .build()
            );
        });
    }

    @Test
    public void reportShouldSucceed() {
        Assertions.assertDoesNotThrow(() -> {
            client.report(
                    ReportRequest.newBuilder()
                            .setCameraName(cameraName)
                            .addAllObservations(new ArrayList<>())
                            .build()
            );
        });
    }

    @Test
    public void reportCarShouldSucceed() {
        Assertions.assertDoesNotThrow(() -> {
            client.report(
                    ReportRequest.newBuilder()
                            .setCameraName(cameraName)
                            .addObservations(observationCar.build())
                            .build()
            );
        });
    }

    @Test
    public void reportCarShouldFailWithInvalidPlate() {
        final String plate = "a";

        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.report(
                    ReportRequest
                            .newBuilder()
                            .setCameraName(cameraName)
                            .addObservations(
                                    observationCar.setId(plate).build()
                            )
                            .build()
            );
        });
    }

    @Test
    public void reportPersonShouldFailWithInvalidId() {
        final String[] invalidIds = {
                "x",
                "-1",
                "",
        };

        for (String id : invalidIds) {
            Assertions.assertThrows(StatusRuntimeException.class, () -> {
                client.report(
                        ReportRequest.newBuilder()
                                .setCameraName(cameraName)
                                .addObservations(observationPerson.setId(id).build())
                                .build()
                );
            });
        }
    }

	@Test
    public void reportPersonShouldSucceed() {
        final String[] validIds = {
                "0",
                "1",
                "999999999999999999",
        };

        for (String id : validIds) {
            Assertions.assertDoesNotThrow(() -> {
                client.report(
                        ReportRequest.newBuilder()
                                .setCameraName(cameraName)
                                .addObservations(observationPerson.setId(id).build())
                                .build()
                );
            });
        }
    }
}

