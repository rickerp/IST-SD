package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import jdk.jshell.Snippet;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SiloIT extends BaseIT {
	
	// static members
	// TODO
	private static int i = 0;
	private static String cameraName;
	
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){

	}

	@AfterAll
	public static void oneTimeTearDown() {
		
	}
	
	// initialization and clean-up for each test
	
	@BeforeEach
	public void setUp() {
		cameraName = "cameraN" + i;
		++i;
		client.clear();
	}
	
	@AfterEach
	public void tearDown() {
		
	}
		
	// tests 
	
	@Test
	public void testPing() {
		client.ping();
	}

	@Test
	public void testClear() {
		client.clear();
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
	public void camJoinShouldNotAcceptRepeatedName() {
		client.camJoin(
				CamJoinRequest.newBuilder()
						.setCameraName(cameraName)
						.setLatitude(0)
						.setLongitude(0).build()
		);

		Assertions.assertThrows(StatusRuntimeException.class, () -> {
			client.camJoin(CamJoinRequest
					.newBuilder()
					.setCameraName(cameraName)
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
	public void reportShouldFailWithUnknownCameraName() {
		Assertions.assertThrows(StatusRuntimeException.class, () -> {
			client.report(
					ReportRequest.newBuilder()
							.setCameraName(cameraName)
							.addAllObservations(new ArrayList<>())
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
											.setId(plate)
											.build()
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
				TrackRequest.newBuilder()
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
			TrackRequest.newBuilder()
				.setTarget(Target.PERSON)
				.setId("12345*")
				.build()
		);

		assertEquals(i, response.getObservationsCount());
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

		final String personId = "777";
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

}
