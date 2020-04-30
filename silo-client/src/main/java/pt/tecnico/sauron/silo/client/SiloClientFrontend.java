package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.UUID;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloClientFrontend {

    SiloGrpc.SiloBlockingStub stub;
    ManagedChannel channel;
    TimestampVector timestamp = new TimestampVector(10);

    public SiloClientFrontend(String zHost, int zPort, int instance) {
        try {
            ZKNaming zkNaming = new ZKNaming(zHost, Integer.toString(zPort));
            String path = "/grpc/sauron/silo";
            Random random = new Random();

            if (instance != -1)
                path += "/" + instance;
            else {
                ArrayList<ZKRecord> servers = new ArrayList<>(zkNaming.listRecords(path));
                int r = random.nextInt(servers.size());
                String[] aux = servers.get(r).getPath().split("/");
                path += "/" + aux[aux.length - 1];
            }

            ZKRecord record = zkNaming.lookup(path);
            final String target = record.getURI();
            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            stub = SiloGrpc.newBlockingStub(channel);
        } catch (Exception e) {
            throw new RuntimeException("failed");
        }
    }

    public InitResponse init() {
        UUID uuid = UUID.randomUUID();

        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setInitRequest(InitRequest.getDefaultInstance())
                .addAllTimestamp(timestamp.getValues())
                .setId(uuid.toString())
                .build();

        UpdateResponse updateResponse = stub.update(updateRequest);

        timestamp.merge(new TimestampVector(updateResponse.getTimestampList()));

        return InitResponse.getDefaultInstance();
    }

    public ClearResponse clear() {
        UUID uuid = UUID.randomUUID();

        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setClearRequest(ClearRequest.getDefaultInstance())
                .addAllTimestamp(timestamp.getValues())
                .setId(uuid.toString())
                .build();

        UpdateResponse updateResponse = stub.update(updateRequest);

        timestamp.merge(new TimestampVector(updateResponse.getTimestampList()));

        return ClearResponse.getDefaultInstance();
    }

    public ReportResponse report(ReportRequest reportRequest) {
        UUID uuid = UUID.randomUUID();

        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setReportRequest(reportRequest)
                .addAllTimestamp(timestamp.getValues())
                .setId(uuid.toString())
                .build();

        UpdateResponse updateResponse = stub.update(updateRequest);

        timestamp.merge(new TimestampVector(updateResponse.getTimestampList()));

        return ReportResponse.getDefaultInstance();
    }

    public CamJoinResponse camJoin(CamJoinRequest camJoinRequest) {
        UUID uuid = UUID.randomUUID();

        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setCamJoinRequest(camJoinRequest)
                .addAllTimestamp(timestamp.getValues())
                .setId(uuid.toString())
                .build();

        UpdateResponse updateResponse = stub.update(updateRequest);

        timestamp.merge(new TimestampVector(updateResponse.getTimestampList()));

        return CamJoinResponse.getDefaultInstance();
    }

    public PingResponse ping() {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setPingRequest(PingRequest.getDefaultInstance())
                .addAllTimestamp(timestamp.getValues())
                .build();

        return stub.query(queryRequest).getPingResponse();
    }

    public CamInfoResponse camInfo(CamInfoRequest camInfoRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setCamInfoRequest(camInfoRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        return stub.query(queryRequest).getCamInfoResponse();
    }

    public TrackResponse track(TrackRequest trackRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setTrackRequest(trackRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        return stub.query(queryRequest).getTrackResponse();
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest trackMatchRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setTrackMatchRequest(trackMatchRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        return stub.query(queryRequest).getTrackMatchResponse();
    }

    public TraceResponse trace(TraceRequest traceRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setTraceRequest(traceRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        return stub.query(queryRequest).getTraceResponse();
    }

    public void end() {
        channel.shutdown();
    }

}
