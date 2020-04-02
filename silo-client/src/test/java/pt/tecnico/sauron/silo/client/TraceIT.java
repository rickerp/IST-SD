package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

public class TraceIT extends BaseIT {

    private final static String cameraName = "camera";
    private final static String personId = "777";
    private final static String plate = "00AA00";

    @Test
	public void traceReturnsTimeOrderedObservationsUsingID() {
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
    public void traceReturnsTimeOrderedObservationsUsingPlate() {
        final int nObservations = 5;

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        final Observation observation = Observation.newBuilder()
                .setTarget(Target.CAR)
                .setId(plate)
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
                        .setTarget(Target.CAR)
                        .setId(plate)
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
    public void traceReturnsCorrectObservationsMultipleCamera() {
        final int nObservations = 2;

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName + "1")
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName + "2")
                        .setLatitude(0)
                        .setLongitude(0)
                        .build()
        );

        final Observation observation = Observation.newBuilder()
                .setTarget(Target.PERSON)
                .setId(personId)
                .build();

        for (int i = 1; i <= nObservations;  i++) {
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
        Assertions.assertEquals(cameraName + "2", response.getObservationsList().get(0).getCameraName());
        Assertions.assertEquals(cameraName + "1", response.getObservationsList().get(1).getCameraName());
        Assertions.assertEquals(personId, response.getObservationsList().get(0).getId());
        Assertions.assertEquals(personId, response.getObservationsList().get(1).getId());
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
