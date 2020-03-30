package pt.tecnico.sauron.silo;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.ObservationObject.Car;
import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;
import pt.tecnico.sauron.silo.domain.ObservationObject.Person;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.SiloException;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.util.stream.Collectors;
import java.util.List;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

    private SiloServerBackend serverBackend = new SiloServerBackend();

    private io.grpc.StatusRuntimeException getGRPCException(SiloException siloException) {
        return Status.INVALID_ARGUMENT.withDescription(siloException.getMessage()).asRuntimeException();
    }

    private Class<? extends ObservationObject> toDomainType(Target target) {
        switch (target) {
            case CAR: return Car.class;
            case PERSON: return Person.class;
            default: return ObservationObject.class;
        }

    }

    private ObservationObject parseObject(Target target, String id) {
        ObservationObject object = null;
        switch (target) {
            case CAR:
                object = new Car(id);
                break;
            case PERSON:
                try {
                    object = new Person(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    throw new SiloException("Person's id must be a number.");
                }
                break;
        }
        if (object == null) {
            throw new SiloException("Type of object not recognized.");
        }
        return object;
    }

    private ObservationDomain toObservationDomain(Observation observation) throws SiloException {
        return new ObservationDomain(
                parseObject(observation.getTarget(), observation.getId()),
                new Timestamp(System.currentTimeMillis()),
                serverBackend.getCamera(observation.getCameraName())
        );
    }

    private Observation toObservation(ObservationDomain observationDomain) {
        ObservationObject object = observationDomain.getObject();
        Target target = null;
        String id = null;
        if (object instanceof Car) {
            target = Target.CAR;
            id = ((Car) object).getPlate();
        } else if (object instanceof Person) {
            target = Target.PERSON;
            id = Long.toString(((Person) object).getId());
        }
        return Observation.newBuilder()
                    .setId(id)
                    .setTarget(target)
                    .setCameraName(observationDomain.getCamera().getName())
                    .setTs(com.google.protobuf.Timestamp.newBuilder().setSeconds(observationDomain.getTimestamp().getTime() / 1000))
                    .build();
    }

    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        PingResponse response = PingResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(ClearRequest request, StreamObserver<ClearResponse> responseObserver) {
        serverBackend.clear();
        ClearResponse response = ClearResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {
        try {
            serverBackend.camJoin(request.getCameraName(), request.getLatitude(), request.getLongitude());
            CamJoinResponse response = CamJoinResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SiloException siloException) {
            responseObserver.onError(getGRPCException(siloException));
        }
    }

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
        Camera camera = serverBackend.getCamera(request.getCameraName());
        CamInfoResponse response = CamInfoResponse.newBuilder()
                .setLatitude(camera.getLatitude())
                .setLongitude(camera.getLongitude())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        try {
            serverBackend.report(
                    request.getCameraName(),
                    request.getObservationsList()
                            .stream()
                            .map(o -> o.toBuilder().setCameraName(request.getCameraName()).build())
                            .map(this::toObservationDomain)
                            .collect(Collectors.toList())
            );
            ReportResponse response = ReportResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SiloException siloException) {
            responseObserver.onError(getGRPCException(siloException));
        }
    }

    @Override
    public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {
        try {
            Observation observation = toObservation(
                    serverBackend.track(parseObject(request.getTarget(), request.getId()))
            );
            TrackResponse response = TrackResponse.newBuilder().setObservation(observation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SiloException siloException) {
            responseObserver.onError(getGRPCException(siloException));
        }
    }

    @Override
    public void trackMatch(TrackRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
        try {
            List<Observation> observations = serverBackend
                                                .trackMatch(toDomainType(request.getTarget()), request.getId())
                                                .stream()
                                                .map(this::toObservation)
                                                .collect(Collectors.toList());

            TrackMatchResponse response = TrackMatchResponse.newBuilder().addAllObservations(observations).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SiloException siloException) {
            responseObserver.onError(getGRPCException(siloException));
        }
        
    }
}
