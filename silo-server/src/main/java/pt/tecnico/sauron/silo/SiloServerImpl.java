package pt.tecnico.sauron.silo;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.ObservationObject.Car;
import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;
import pt.tecnico.sauron.silo.domain.ObservationObject.Person;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.exception.*;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

    private TimestampVector valueTS = new TimestampVector(10);

    private List<TimestampVector> table = new ArrayList<>();
    private List<LogRecord> executedLog = new ArrayList<LogRecord>();
    private Set<String> executed = new HashSet<String>();

    private SiloServerBackend serverBackend = new SiloServerBackend();

    final int replica;

    public SiloServerImpl(String zHost, String zPort, Integer instance) {
        super();

        for (int i = 0; i < 10; ++i)
            table.add(new TimestampVector(10));

        replica = instance; 
        final int gossipInterval = 30; // seconds

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ZKNaming zkNaming = new ZKNaming(zHost, zPort);

                    final String prefix = "/grpc/sauron/silo";
                    ArrayList<ZKRecord> servers = new ArrayList<ZKRecord>(zkNaming.listRecords(prefix));

                    for (ZKRecord record : servers) {
                        String[] aux = record.getPath().split("/");

                        final int sv = Integer.parseInt(aux[aux.length - 1]);
                        if (sv == replica) continue;

                        final String target = record.getURI();

                        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                        SiloGrpc.SiloBlockingStub stub = SiloGrpc.newBlockingStub(channel);
                        stub.gossip(GossipRequest.newBuilder()
                                                    .addAllTimestamp(valueTS.getValues())
                                                    .addAllLog(executedLog.stream()
                                                    .filter(e -> {
                                                        var ts = new TimestampVector(e.getTimestampList());
                                                        return ts.compareTo(table.get(sv)) > 0;
                                                    }).collect(Collectors.toList()))
                                                    .setReplica(replica)
                                                    .build());
                        channel.shutdownNow();
                    }
                } catch (Exception e) {
                    //throw new RuntimeException("failed");
                }
            }
        }, gossipInterval * 1000, gossipInterval * 1000);
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        try {
            List<LogRecord> gossipLog = request.getLogList();
            TimestampVector gossipTS = new TimestampVector(request.getTimestampList());

            table.set(request.getReplica(), gossipTS);

            for (LogRecord l : gossipLog) {
                var updateRequest = l.getUpdateRequest();
                log(String.format("Received update {%s} from gossip.", updateRequest.getId()));
                if (!executed.contains(updateRequest.getId())) {
                    try {
                        if (updateRequest.hasCamJoinRequest()) {
                            camJoin(updateRequest.getCamJoinRequest());
                        } else if (updateRequest.hasReportRequest()) {
                            report(updateRequest.getReportRequest());
                        }
                        executedLog.add(l);
                        executed.add(updateRequest.getId());
                        valueTS.merge(gossipTS);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log(String.format("Update {%s} failed", updateRequest.getId()));
                        responseObserver.onError(getGRPCException(e));
                        return;
                    }
                }
            }
            log(String.format("VectorTS after gossip: %s", valueTS));

            GossipResponse response = GossipResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
        
    }

    @Override
    public void query(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
        TimestampVector prev = new TimestampVector(request.getTimestampList());

        log(String.format("Received query with ts: %s", prev));

        try {
            QueryResponse.Builder response = QueryResponse
                    .newBuilder()
                    .addAllTimestamp(valueTS.getValues());

            if (request.hasCamInfoRequest()) {
                response.setCamInfoResponse(
                        camInfo(request.getCamInfoRequest())
                );
            } else if (request.hasTrackMatchRequest()) {
                response.setTrackMatchResponse(
                        trackMatch(request.getTrackMatchRequest())
                );
            } else if (request.hasTrackRequest()) {
                response.setTrackResponse(track(request.getTrackRequest()));
            } else if (request.hasTraceRequest()) {
                response.setTraceResponse(trace(request.getTraceRequest()));
            }
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(getGRPCException(e));
        }
    }

    public void log(String message) {
        System.out.println("Replica " + replica + ": " + message);
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {

        log(String.format("Received update {%s} ", request.getId()));

        UpdateResponse response;

        if (!executed.contains(request.getId())) {

            log(String.format("Update {%s} now executing", request.getId()));

            try {
                if (request.hasCamJoinRequest()) {
                    camJoin(request.getCamJoinRequest());
                } else if (request.hasReportRequest()) {
                    report(request.getReportRequest());
                }
            } catch (Exception e) {
                log(String.format("Update {%s} failed", request.getId()));
                responseObserver.onError(getGRPCException(e));
                return;
            }

            valueTS.set(replica, valueTS.get(replica) + 1);
            log(String.format("VectorTS updated, now: %s", valueTS));

            LogRecord logRecord = LogRecord.newBuilder()
                        .setReplica(replica)
                        .setUpdateRequest(request)
                        .addAllTimestamp(valueTS.getValues())
                        .build();

            executedLog.add(logRecord);
            executed.add(request.getId());

        } else {
            log(String.format("Update with id {%s} was already received", request.getId()));
        }

        response = UpdateResponse.newBuilder()
                .addAllTimestamp(valueTS.getValues())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private io.grpc.StatusRuntimeException getGRPCException(Exception exception) {
        Status status = Status.INTERNAL.withDescription("An unknown error occurred.");
        if (exception instanceof CamAlreadyExistsException) {
            status = Status.ALREADY_EXISTS.withDescription(
                    exception.getMessage()
            );
        } else if (exception instanceof CamNotFoundException) {
            status = Status.INVALID_ARGUMENT.withDescription(
                    exception.getMessage()
            );
        } else if (exception instanceof SiloArgumentException) {
            status = Status.INVALID_ARGUMENT.withDescription(
                    exception.getMessage()
            );
        } else if (exception instanceof CoordinateException) {
            status = Status.OUT_OF_RANGE.withDescription(
                    exception.getMessage()
            );
        }
        return status.asRuntimeException();
    }

    private Class<? extends ObservationObject> toDomainType(Target target) {
        switch (target) {
            case CAR:
                return Car.class;
            case PERSON:
                return Person.class;
            default:
                return ObservationObject.class;
        }

    }

    private ObservationObject parseObject(Target target, String id) throws SiloArgumentException {
        ObservationObject object = null;
        switch (target) {
            case CAR:
                object = new Car(id);
                break;
            case PERSON:
                try {
                    object = new Person(Long.parseUnsignedLong(id));
                } catch (NumberFormatException e) {
                    throw new SiloArgumentException("Person's id must be a number.");
                }
                break;
        }
        if (object == null) {
            throw new SiloArgumentException("Type of object not recognized.");
        }
        return object;
    }

    private ObservationDomain toObservationDomain(Observation observation) throws CamNotFoundException, SiloArgumentException {
        String cameraName = observation.getCameraName();
        Camera camera = serverBackend.getCamera(cameraName);
        return new ObservationDomain(
                parseObject(observation.getTarget(), observation.getId()),
                new Timestamp(System.currentTimeMillis()),
                camera
        );
    }

    private Observation toObservation(ObservationDomain observationDomain) {
        ObservationObject object = observationDomain.getObservationObject();
        Target target = null;
        String id = null;
        if (object instanceof Car) {
            target = Target.CAR;
            id = ((Car) object).getPlate();
        } else if (object instanceof Person) {
            target = Target.PERSON;
            id = Long.toUnsignedString(((Person) object).getId());
        }
        return Observation.newBuilder().setId(id).setTarget(target)
                .setCameraName(observationDomain.getCamera().getName())
                .setTs(com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(observationDomain.getTimestamp().getTime() / 1000))
                .setCamInfo(CamInfoResponse.newBuilder()
                        .setLatitude(observationDomain.getCamera().getLatitude())
                        .setLongitude(observationDomain.getCamera().getLongitude()))
                .build();
    }

    @Override
    public void ctrlInit(InitRequest request, StreamObserver<InitResponse> responseObserver) {
        try {
            InitResponse response = InitResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        try {
            PingResponse response = PingResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void ctrlClear(ClearRequest request, StreamObserver<ClearResponse> responseObserver) {
        try {
            serverBackend.clear();
            ClearResponse response = ClearResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    public void camJoin(CamJoinRequest request) throws CoordinateException, SiloArgumentException, CamAlreadyExistsException {
        serverBackend.camJoin(request.getCameraName(), request.getLatitude(), request.getLongitude());
    }

    public CamInfoResponse camInfo(CamInfoRequest request) throws CamNotFoundException {
        String cameraName = request.getCameraName();
        Camera camera = serverBackend.getCamera(cameraName);
        return CamInfoResponse.newBuilder()
                .setLatitude(camera.getLatitude())
                .setLongitude(camera.getLongitude())
                .build();
    }

    public void report(ReportRequest request) throws CamNotFoundException, SiloArgumentException {
        List<ObservationDomain> list = new ArrayList<>();
        for (Observation o : request.getObservationsList()) {
            Observation build = o.toBuilder().setCameraName(request.getCameraName()).build();
            ObservationDomain observationDomain = toObservationDomain(build);
            list.add(observationDomain);
        }
        serverBackend.report(list);
    }

    public TraceResponse trace(TraceRequest request) throws SiloArgumentException {
        List<Observation> observations = serverBackend
                .trace(parseObject(request.getTarget(), request.getId()))
                .stream()
                .map(this::toObservation)
                .collect(Collectors.toList());
        return TraceResponse.newBuilder().addAllObservations(observations).build();
    }

    public TrackResponse track(TrackRequest request) throws SiloArgumentException {
        Observation observation = serverBackend
                .track(parseObject(request.getTarget(), request.getId()))
                .map(this::toObservation)
                .orElse(Observation.getDefaultInstance());
        return TrackResponse.newBuilder().setObservation(observation).build();
    }

    public TrackMatchResponse trackMatch(TrackMatchRequest request) {
        List<Observation> observations = serverBackend
                .trackMatch(toDomainType(request.getTarget()), request.getId())
                .stream()
                .map(this::toObservation)
                .collect(Collectors.toList());
        return TrackMatchResponse.newBuilder().addAllObservations(observations).build();
    }

}
