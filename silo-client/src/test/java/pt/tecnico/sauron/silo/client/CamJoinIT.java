package pt.tecnico.sauron.silo.client;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.*;

public class CamJoinIT extends BaseIT {

    private final static String cameraName = "camera";
    private CamJoinRequest.Builder camRequest;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        camRequest = CamJoinRequest.newBuilder()
                            .setCameraName(cameraName)
                            .setLongitude(0)
                            .setLatitude(0);
    }

    @Test
    public void camJoinShouldSucceed() {
        Assertions.assertDoesNotThrow(() -> {
            client.camJoin(camRequest.build());
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
                client.camJoin(camRequest
                                    .setLatitude(coords[0])
                                    .setLongitude(coords[1])
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
                client.camJoin(camRequest
                                    .setCameraName(name)
                                    .build()
                );
            });
        }
    }

    @Test
    public void camJoinShouldBeIdempotent() {
        client.camJoin(camRequest.build());

        Assertions.assertDoesNotThrow(() -> {
            client.camJoin(camRequest.build());
        });
    }

    @Test
    public void camJoinShouldNotAcceptRepeatedNameDifferentCoordinates() {
        client.camJoin(camRequest.build());

        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            client.camJoin(camRequest
                                .setLongitude(1)
                                .setLatitude(1)
                                .build()
            );
        });
    }
}