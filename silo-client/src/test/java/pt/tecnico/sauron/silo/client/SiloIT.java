package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.CamInfoRequest;
import pt.tecnico.sauron.silo.grpc.CamInfoResponse;
import pt.tecnico.sauron.silo.grpc.CamJoinRequest;
import pt.tecnico.sauron.silo.grpc.CamJoinResponse;

import static org.junit.Assert.*;

public class SiloIT extends BaseIT {
	
	// static members
	// TODO	
	
	
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
	public void camJoinShouldNotAcceptRepeatedName() {
		final String cameraName = "camJoinShouldNotAcceptRepeatedName";

		CamJoinResponse responseBefore = client.camJoin(
				CamJoinRequest.newBuilder()
						.setCameraName(cameraName)
						.setLatitude(0)
						.setLongitude(0).build()
		);

		CamJoinResponse responseAfter = client.camJoin(
				CamJoinRequest.newBuilder()
						.setCameraName(cameraName).build()
		);

		assertTrue(responseBefore.getSuccess());
		assertFalse(responseAfter.getSuccess());
	}

	@Test
	public void camInfoShouldReturnCorrectCoordinates() {
		final String cameraName = "camInfoShouldReturnCorrectCoordinates";
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

}
