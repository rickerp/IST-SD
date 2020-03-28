package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;

public class SiloClientFrontend {

    SiloGrpc.SiloBlockingStub stub;

    public SiloClientFrontend(String host, int port) {
        final String target = host + ":" + port;
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SiloGrpc.newBlockingStub(channel);
    }

    public void ping() {
        // TODO: Calculate return time
        PingResponse response = stub.ctrlPing(PingRequest.getDefaultInstance());
    }

    public void clear() {
        ClearResponse response = stub.ctrlClear(ClearRequest.getDefaultInstance());
    }

}

