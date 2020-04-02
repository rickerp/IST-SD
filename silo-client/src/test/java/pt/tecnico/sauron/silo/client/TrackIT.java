package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import static org.junit.Assert.*;

public class TrackIT extends BaseIT {

    private final static String cameraName = "camera";
    private final static String personId = "777";
    private final static String plate = "00AA00";

    @Test
    public void trackShouldReturnCorrectObservationUsingID(){
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

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
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

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
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

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

    @Test
    public void trackMatchShouldNotReturnUnaskedObservations() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

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

        TrackMatchResponse response = client.trackMatch(
                TrackMatchRequest.newBuilder()
                        .setId("2*")
                        .setTarget(Target.PERSON)
                        .build()
        );

        assertEquals(0, response.getObservationsCount());
    }

    @Test
    public void trackMatchShouldReturnCorrectObservationsUsingID() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

        String personId = null;
        int i;
        for (i = 0; i < 3; ++i) {
            personId = "12345" + i;
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
        }

        TrackMatchResponse response = client.trackMatch(
                TrackMatchRequest.newBuilder()
                        .setTarget(Target.PERSON)
                        .setId("12345*")
                        .build()
        );

        assertEquals(i, response.getObservationsCount());
    }

    @Test
    public void trackMatchShouldReturnCorrectObservationsUsingPlate(){
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

        String plate = "00AA0";
        int i;
        for (i = 0; i < 3; ++i) {
            String plateaux = plate + i;
            client.report(
                    ReportRequest.newBuilder()
                            .setCameraName(cameraName)
                            .addObservations(
                                    Observation.newBuilder()
                                            .setTarget(Target.CAR)
                                            .setId(plateaux)
                                            .build()
                            )
                            .build()
            );
        }

        TrackMatchResponse response = client.trackMatch(
                TrackMatchRequest.newBuilder()
                        .setTarget(Target.CAR)
                        .setId("00AA0*")
                        .build()
        );

        assertEquals(i, response.getObservationsCount());
    }

    @Test
    public void trackMatchShouldReturnOneMostRecent() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        final Observation observation = Observation.newBuilder()
                .setTarget(Target.PERSON)
                .setId("100")
                .build();


        for (int i = 0; i < 5; ++i) {
            client.report(
                    ReportRequest.newBuilder()
                            .setCameraName(cameraName)
                            .addObservations(observation)
                            .build()
            );
        }

        TrackMatchResponse response = client.trackMatch(
                TrackMatchRequest.newBuilder()
                        .setTarget(Target.PERSON)
                        .setId("1*")
                        .build()
        );

        Assertions.assertEquals(1, response.getObservationsCount());
    }

    @Test
    public void trackMatchReturnsMostRecent() {

        final String camera1 = cameraName + "1";
        final String camera2 = cameraName + "2";

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(camera1)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(camera2)
                        .setLongitude(10)
                        .setLongitude(10)
                        .build()
        );

        final Observation observation = Observation.newBuilder()
                .setTarget(Target.PERSON)
                .setId(personId)
                .build();

        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(camera1)
                        .addObservations(observation)
                        .build()
        );

        client.report(
                ReportRequest.newBuilder()
                        .setCameraName(camera2)
                        .addObservations(observation)
                        .build()
        );


        TrackMatchResponse response = client.trackMatch(
                TrackMatchRequest.newBuilder()
                        .setTarget(Target.PERSON)
                        .setId("7*")
                        .build()
        );

        Assertions.assertEquals(camera2, response.getObservationsList().get(0).getCameraName());
    }
}

