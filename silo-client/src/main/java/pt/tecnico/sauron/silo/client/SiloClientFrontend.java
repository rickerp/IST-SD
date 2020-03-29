package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;

public class SiloClientFrontend {

    SiloGrpc.SiloBlockingStub stub;
    ManagedChannel channel;

    public SiloClientFrontend(String host, int port) {
        final String target = host + ":" + port;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SiloGrpc.newBlockingStub(channel);
    }

    public void ping() {
        // TODO: Calculate return time
        PingResponse response = stub.ctrlPing(PingRequest.getDefaultInstance());
    }

    public void clear() {
        ClearResponse response = stub.ctrlClear(ClearRequest.getDefaultInstance());
    }

    public ReportResponse report(ReportRequest reportRequest) {
        return stub.report(reportRequest);
    }

    public CamJoinResponse camJoin(CamJoinRequest camJoinRequest) {
        return stub.camJoin(camJoinRequest);
    }

    public CamInfoResponse camInfo(CamInfoRequest camInfoRequest) {
        return stub.camInfo(camInfoRequest);
    }

    public TrackResponse spot(TrackRequest trackRequest) {
        return stub.track(trackRequest);
    }

    public void end() {
        channel.shutdown();
    }

}

