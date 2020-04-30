package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.UUID;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloClientFrontend {

    SiloGrpc.SiloBlockingStub stub;
    ManagedChannel channel;
    TimestampVector timestamp = new TimestampVector(10);
    UpdateRequest clientLogin;
    ZKNaming zkNaming;
    boolean reconnect = false;
    final String prefix = "/grpc/sauron/silo";

    public SiloClientFrontend(String zHost, int zPort, int instance) {
        try {
            zkNaming = new ZKNaming(zHost, Integer.toString(zPort));
            String path = prefix;
            Random random = new Random();

            if (instance != -1)
                path += "/" + instance;
            else {
                ArrayList<ZKRecord> servers = new ArrayList<>(zkNaming.listRecords(path));
                int r = random.nextInt(servers.size());
                path = servers.get(r).getPath();
                reconnect = true;
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
        return stub.ctrlInit(InitRequest.getDefaultInstance());
    }

    public ClearResponse clear() {
        return stub.ctrlClear(ClearRequest.getDefaultInstance());
    }

    public PingResponse ping() {
        return stub.ctrlPing(PingRequest.getDefaultInstance());
    }

    private UpdateResponse update(UpdateRequest updateRequest) {
        while (true) {
            try {
                UpdateResponse updateResponse = stub.update(updateRequest);

                timestamp.merge(new TimestampVector(updateResponse.getTimestampList()));

                return UpdateResponse.getDefaultInstance();

            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != Status.Code.UNAVAILABLE || !reconnect)
                    throw e;
                else {
                    try {
                        channel.shutdownNow();
                        ArrayList<ZKRecord> servers = new ArrayList<>(zkNaming.listRecords(prefix));
                        int r = new Random().nextInt(servers.size());
                        String path = servers.get(r).getPath();

                        ZKRecord record = zkNaming.lookup(path);
                        final String target = record.getURI();
                        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                        stub = SiloGrpc.newBlockingStub(channel);

                        if (!updateRequest.hasCamJoinRequest())
                            stub.update(clientLogin);

                    } catch (ZKNamingException zke) {
                        zke.printStackTrace();
                    }
                }
            }
        }
    }

    private QueryResponse query(QueryRequest queryRequest) {
        while (true) {
            try {
                QueryResponse queryResponse = stub.query(queryRequest);

                timestamp.merge(new TimestampVector(queryResponse.getTimestampList()));

                return queryResponse;

            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() != Status.Code.UNAVAILABLE)
                    throw e;
                else {
                    try {
                        ArrayList<ZKRecord> servers = new ArrayList<>(zkNaming.listRecords(prefix));
                        int r = new Random().nextInt(servers.size());
                        String path = servers.get(r).getPath();

                        ZKRecord record = zkNaming.lookup(path);
                        final String target = record.getURI();
                        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                        stub = SiloGrpc.newBlockingStub(channel);
                    } catch (ZKNamingException zke) {
                        zke.printStackTrace();
                    }
                }
            }
        }
    }

    public ReportResponse report(ReportRequest reportRequest) {
        UUID uuid = UUID.randomUUID();

        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setReportRequest(reportRequest)
                .addAllTimestamp(timestamp.getValues())
                .setId(uuid.toString())
                .build();

        update(updateRequest);

        return ReportResponse.getDefaultInstance();
    }

    public CamJoinResponse camJoin(CamJoinRequest camJoinRequest) {
        UUID uuid = UUID.randomUUID();

        UpdateRequest updateRequest = UpdateRequest.newBuilder()
                .setCamJoinRequest(camJoinRequest)
                .addAllTimestamp(timestamp.getValues())
                .setId(uuid.toString())
                .build();

        clientLogin = updateRequest;

        update(updateRequest);

        return CamJoinResponse.getDefaultInstance();
    }

    public CamInfoResponse camInfo(CamInfoRequest camInfoRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setCamInfoRequest(camInfoRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        QueryResponse queryResponse = query(queryRequest);

        return queryResponse.getCamInfoResponse();
    }

    public TrackResponse track(TrackRequest trackRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setTrackRequest(trackRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        QueryResponse queryResponse = query(queryRequest);

        return queryResponse.getTrackResponse();
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest trackMatchRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setTrackMatchRequest(trackMatchRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        QueryResponse queryResponse = query(queryRequest);

        return queryResponse.getTrackMatchResponse();
    }

    public TraceResponse trace(TraceRequest traceRequest) {
        QueryRequest queryRequest = QueryRequest.newBuilder()
                .setTraceRequest(traceRequest)
                .addAllTimestamp(timestamp.getValues())
                .build();

        QueryResponse queryResponse = query(queryRequest);

        return queryResponse.getTraceResponse();
    }

    public void end() {
        channel.shutdown();
    }

}
