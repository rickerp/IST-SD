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

    
}