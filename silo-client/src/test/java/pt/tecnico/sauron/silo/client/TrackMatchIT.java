package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.grpc.*;

import static org.junit.Assert.assertEquals;

public class TrackMatchIT extends BaseIT {

    private final static String cameraName = "camera";
    private final static String personId = "777";

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
    public void trackMatchShouldNotReturnUnaskedObservations() {
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
