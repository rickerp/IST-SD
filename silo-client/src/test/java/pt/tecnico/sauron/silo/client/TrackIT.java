package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import static org.junit.Assert.*;

public class TrackIT extends BaseIT {

    private final static String cameraName = "camera";
    private final static String personId = "777";
    private final static String plate = "00AA00";

    @Override
    @BeforeEach
    public void setUp() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

    }

    @Override
    @AfterEach
    public void tearDown() {
        client.clear();
    }

    @Test
    public void trackShouldReturnCorrectObservationUsingID(){
        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(cameraName)
                        .addObservations(
                                Observation.newBuilder()
                                        .setTarget(Target.PERSON)
                                        .setId(personId)
                                        .build()
                        )
                        .build()
        );

        TrackResponse response = client.track(
                TrackRequest.newBuilder()
                        .setId(personId)
                        .setTarget(Target.PERSON)
                        .build()
        );

        Assertions.assertTrue(response.hasObservation());
        Assertions.assertEquals(personId, response.getObservation().getId());
        Assertions.assertEquals(Target.PERSON, response.getObservation().getTarget());
        Assertions.assertEquals(cameraName, response.getObservation().getCameraName());
    }

    @Test
    public void trackShouldReturnCorrectObservationUsingPlate(){
        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(cameraName)
                        .addObservations(
                                Observation.newBuilder()
                                        .setTarget(Target.CAR)
                                        .setId(plate)
                                        .build()
                        )
                        .build()
        );

        TrackResponse response = client.track(
                TrackRequest.newBuilder()
                        .setId(plate)
                        .setTarget(Target.CAR)
                        .build()
        );

        Assertions.assertTrue(response.hasObservation());
        Assertions.assertEquals(plate, response.getObservation().getId());
        Assertions.assertEquals(Target.CAR, response.getObservation().getTarget());
        Assertions.assertEquals(cameraName, response.getObservation().getCameraName());
    }

    @Test
    public void trackShouldReturnMostRecentObservation() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName + "1")
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName + "2")
                        .setLongitude(10)
                        .setLatitude(10)
                        .build()
        );


        String personId = "123";
        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(cameraName + "1")
                        .addObservations(
                                Observation.newBuilder()
                                        .setTarget(Target.PERSON)
                                        .setId(personId)
                                        .build()
                        )
                        .build()
        );

        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(cameraName + "2")
                        .addObservations(
                                Observation.newBuilder()
                                        .setTarget(Target.PERSON)
                                        .setId(personId)
                                        .build()
                        )
                        .build()
        );

        TrackResponse response = client.track(
                TrackRequest.newBuilder()
                        .setId(personId)
                        .setTarget(Target.PERSON)
                        .build()
        );

        Assertions.assertTrue(response.hasObservation());
        Assertions.assertEquals(personId, response.getObservation().getId());
        Assertions.assertEquals(cameraName + "2", response.getObservation().getCameraName());
    }

    @Test
    public void trackShouldReturnEmptyOnNonExistentID() {
        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(cameraName)
                        .addObservations(
                                Observation.newBuilder()
                                        .setId("123")
                                        .setTarget(Target.PERSON)
                                        .build()
                        )
                        .build()
        );

        TrackResponse response = client.track(
                TrackRequest.newBuilder()
                        .setId("2")
                        .setTarget(Target.PERSON)
                        .build()
        );

        assertTrue(response.hasObservation());

    }

}

