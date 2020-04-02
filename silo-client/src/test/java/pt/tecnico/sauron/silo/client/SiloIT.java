package pt.tecnico.sauron.silo.client;

import com.google.common.collect.Comparators;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SiloIT extends BaseIT {

    private final static String cameraName = "camera";
    private final static String personId = "777";


    @BeforeAll
    public static void oneTimeSetUp() {

    }

    @AfterAll
    public static void oneTimeTearDown() {

    }

    @BeforeEach
    public void setUp() {
        client.clear();
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void testPing() {
        Assertions.assertDoesNotThrow(() -> {
            client.ping();
        });
    }

    @Test
    public void camJoinShouldSucceed() {
        Assertions.assertDoesNotThrow(() -> {
            client.camJoin(
                    CamJoinRequest.newBuilder()
                            .setCameraName(cameraName)
                            .setLongitude(0)
                            .setLatitude(0)
                            .build()
            );
        });
    }

    @Test
    public void camJoinShouldNotAcceptInvalidCoordinates() {
        final float[][] invalidCoordinates = new float[][]{
                {-91, 0},
                {0, -181},
                {0, 181},
                {91, 0},
        };

        for (float[] coords : invalidCoordinates) {
            Assertions.assertThrows(StatusRuntimeException.class, () -> {
                client.camJoin(
                        CamJoinRequest.newBuilder()
                                .setLatitude(coords[0])
                                .setLongitude(coords[1])
                                .setCameraName(cameraName)
                                .build()
                );
            });
        }

    }

    @Test
    public void camJoinShouldNotAcceptInvalidName() {
        String[] invalidNames = new String[]{
                "ab",
                "abcdef%",
                "fasokfsdfpaskdpfkspdkfskdfksdk"
        };

        for (String name : invalidNames) {
            Assertions.assertThrows(StatusRuntimeException.class, () -> {
                client.camJoin(
                        CamJoinRequest.newBuilder()
                                .setCameraName(name)
                                .setLongitude(0)
                                .setLatitude(0)
                                .build()
                );
            });
        }
    }

    @Test
    public void camJoinShouldBeIdempotent() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        Assertions.assertDoesNotThrow(() -> {
            client.camJoin(CamJoinRequest
                    .newBuilder()
                    .setCameraName(cameraName)
                    .setLongitude(0)
                    .setLatitude(0)
                    .build()
            );
        });
    }

    @Test
    public void camJoinShouldNotAcceptRepeatedNameDifferentCoordinates() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.camJoin(CamJoinRequest
                    .newBuilder()
                    .setCameraName(cameraName)
                    .setLongitude(1)
                    .setLatitude(1)
                    .build()
            );
        });
    }

    @Test
    public void camInfoShouldReturnCorrectCoordinates() {
        final float latitude = 55;
        final float longitude = 43;

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .build()
        );

        CamInfoResponse camInfoResponse = client.camInfo(
                CamInfoRequest.newBuilder()
                        .setCameraName(cameraName)
                        .build()
        );

        assertEquals(camInfoResponse.getLatitude(), latitude, 0);
        assertEquals(camInfoResponse.getLongitude(), longitude, 0);
    }

    @Test
    public void camInfoShouldFailWithUnknownCameraName() {
        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.camInfo(
                    CamInfoRequest.newBuilder()
                            .setCameraName(cameraName)
                            .build()
            );
        });
    }

    @Test
    public void reportShouldFailWithUnknownCameraName() {
        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.report(
                    ReportRequest.newBuilder()
                            .setCameraName(cameraName)
                            .addObservations(
                                    Observation.getDefaultInstance()
                            )
                            .build()
            );
        });
    }

    @Test
    public void reportShouldSucceed() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(55)
                        .setLongitude(43)
                        .build()
        );

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
        final String plate = "AA55BB";

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        Assertions.assertDoesNotThrow(() -> {
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
        });
    }

    @Test
    public void reportCarShouldFailWithInvalidPlate() {
        final String plate = "a";

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.report(
                    ReportRequest
                            .newBuilder()
                            .setCameraName(cameraName)
                            .addObservations(
                                    Observation.newBuilder()
                                            .setTarget(Target.CAR)
                                            .setId(plate).build()
                            )
                            .build()
            );
        });
    }

    @Test
    public void trackShouldReturnMostRecentObservation() {
        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLongitude(0)
                        .setLatitude(0)
                        .build()
        );

        String personId = "123";
        for (int i = 0; i < 3; ++i) {
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

        TrackResponse response = client.track(
                TrackRequest.newBuilder()
                        .setId(personId)
                        .setTarget(Target.PERSON)
                        .build()
        );

        Assertions.assertTrue(response.hasObservation());
        Assertions.assertEquals(response.getObservation().getId(), personId);
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
    public void trackMatchShouldReturnCorrectObservations() {
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
    public void trackReturnsMostRecent() {

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
                        .setLongitude(0)
                        .setLongitude(0)
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


        TrackResponse response = client.track(
                TrackRequest.newBuilder()
                        .setTarget(Target.PERSON)
                        .setId(personId)
                        .build()
        );

        Assertions.assertEquals(camera2, response.getObservation().getCameraName());
    }

    @Test
	public void traceReturnsTimeOrderedObservations() {
		final int nObservations = 5;

		client.camJoin(
				CamJoinRequest.newBuilder()
						.setCameraName(cameraName)
						.setLatitude(0)
						.setLongitude(0)
						.build()
		);

		final Observation observation = Observation.newBuilder()
				.setTarget(Target.PERSON)
				.setId(personId)
				.build();

		for (int i = 0; i < nObservations; i++) {
			client.report(ReportRequest.newBuilder()
					.setCameraName(cameraName)
					.addObservations(observation)
					.build()
			);
		}

		TraceResponse response = client.trace(
				TraceRequest.newBuilder()
						.setTarget(Target.PERSON)
						.setId(personId)
						.build()
		);

		Timestamp[] ts = response.getObservationsList()
                .stream()
                .map(Observation::getTs)
                .toArray(Timestamp[]::new);

		for (int i = 0; i < ts.length - 1; ++i) {
		    Assertions.assertTrue(Timestamps.compare(ts[i], ts[i + 1]) >= 0);
        }
	}

	@Test
    public void reportPersonShouldSucceed() {
        final String[] validIds = {
                "0",
                "1",
                "999999999999999999",
        };

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        for (String id : validIds) {
            Assertions.assertDoesNotThrow(() -> {
                client.report(
                        ReportRequest.newBuilder()
                                .setCameraName(cameraName)
                                .addObservations(
                                        Observation.newBuilder()
                                                .setTarget(Target.PERSON)
                                                .setId(id)
                                                .build()
                                )
                                .build()
                );
            });
        }
    }

	@Test
    public void reportPersonShouldFailWithInvalidId() {
        final String[] invalidIds = {
                "x",
                "-1",
                "",
        };

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        for (String id : invalidIds) {
            Assertions.assertThrows(StatusRuntimeException.class, () -> {
                client.report(
                        ReportRequest.newBuilder()
                                .setCameraName(cameraName)
                                .addObservations(
                                        Observation.newBuilder()
                                                .setTarget(Target.PERSON)
                                                .setId(id)
                                                .build()
                                )
                                .build()
                );
            });
        }
    }

    @Test
    public void traceReturnsCorrectSortedObservations() {
        final int nObservations = 5;

        for (int i = 0; i < nObservations; i++) {
            client.camJoin(
                    CamJoinRequest.newBuilder()
							.setCameraName(cameraName + i)
							.setLatitude(0)
							.setLongitude(0)
							.build()
			);
        }

        final Observation observation = Observation.newBuilder()
				.setTarget(Target.PERSON)
				.setId(personId)
				.build();

        for (int i = 0; i < nObservations; i++) {
            client.report(ReportRequest.newBuilder()
					.setCameraName(cameraName + i)
					.addObservations(observation)
					.build()
			);
        }

        TraceResponse response = client.trace(
        		TraceRequest.newBuilder()
						.setTarget(Target.PERSON)
						.setId(personId)
						.build()
		);

        Assertions.assertEquals(nObservations, response.getObservationsCount());

        for (int i = 0; i < nObservations; i++) {
        	String actualCameraName = response.getObservationsList().get(nObservations - 1 - i).getCameraName();
            Assertions.assertEquals(cameraName + i, actualCameraName);
        }
    }
}
