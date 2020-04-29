package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloClientFrontend {

    SiloGrpc.SiloBlockingStub stub;
    ManagedChannel channel;

    public SiloClientFrontend(String host, int port, String path) {
        try {
            ZKNaming zkNaming = new ZKNaming(host, Integer.toString(port));
            ZKRecord record = zkNaming.lookup(path);
            final String target = record.getURI();
            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            stub = SiloGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            throw new RuntimeException("failed");
        }
    }

    public InitResponse init() {
        return stub.ctrlInit(InitRequest.getDefaultInstance());
    }

    public PingResponse ping() {
        return stub.ctrlPing(PingRequest.getDefaultInstance());
    }

    public ClearResponse clear() {
        return stub.ctrlClear(ClearRequest.getDefaultInstance());
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

    public TrackResponse track(TrackRequest trackRequest) {
        return stub.track(trackRequest);
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest trackMatchRequest) {
        return stub.trackMatch(trackMatchRequest);
    }

    public TraceResponse trace(TraceRequest traceRequest) {
        return stub.trace(traceRequest);
    }

    public void end() {
        channel.shutdown();
    }

}
