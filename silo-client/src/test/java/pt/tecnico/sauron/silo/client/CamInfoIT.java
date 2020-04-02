package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

import static org.junit.Assert.*;

public class CamInfoIT extends BaseIT {

    private final static String cameraName = "camera";
    private final static float latitude = 55;
    private final static float longitude = 43;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        client.camJoin(
                CamJoinRequest.newBuilder()
                        .setCameraName(cameraName)
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .build()
        );
    }

    @Test
    public void camInfoShouldReturnCorrectCoordinates() {       

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
                            .setCameraName("notACamera")
                            .build()
            );
        });
    }
}