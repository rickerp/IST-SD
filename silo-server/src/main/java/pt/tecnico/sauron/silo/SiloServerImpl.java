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
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

    private SiloServerBackend serverBackend = new SiloServerBackend();

    private io.grpc.StatusRuntimeException getGRPCException(Exception exception) {
        Status status = Status.INTERNAL.withDescription("An unknown error occurred.");
        if (exception instanceof CamAlreadyExistsException) {
            status = Status.ALREADY_EXISTS.withDescription(
                    exception.getMessage()
            );
        } else if (exception instanceof CamNotFoundException) {
            status = Status.NOT_FOUND.withDescription(
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

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {
        try {
            serverBackend.camJoin(request.getCameraName(), request.getLatitude(), request.getLongitude());
            CamJoinResponse response = CamJoinResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
        try {
            String cameraName = request.getCameraName();
            Camera camera = serverBackend.getCamera(cameraName);
            CamInfoResponse response = CamInfoResponse.newBuilder()
                    .setLatitude(camera.getLatitude())
                    .setLongitude(camera.getLongitude())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        try {
            List<ObservationDomain> list = new ArrayList<>();
            for (Observation o : request.getObservationsList()) {
                Observation build = o.toBuilder().setCameraName(request.getCameraName()).build();
                ObservationDomain observationDomain = toObservationDomain(build);
                list.add(observationDomain);
            }
            serverBackend.report(list);
            ReportResponse response = ReportResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {
        try {
            List<Observation> observations = serverBackend
                    .trace(parseObject(request.getTarget(), request.getId()))
                    .stream()
                    .map(this::toObservation)
                    .collect(Collectors.toList());
            TraceResponse response = TraceResponse.newBuilder().addAllObservations(observations).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {
        try {
            Observation observation = serverBackend
                    .track(parseObject(request.getTarget(), request.getId()))
                    .map(this::toObservation)
                    .orElse(Observation.getDefaultInstance());
            TrackResponse response = TrackResponse.newBuilder().setObservation(observation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

    @Override
    public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
        try {
            List<Observation> observations = serverBackend
                    .trackMatch(toDomainType(request.getTarget()), request.getId())
                    .stream()
                    .map(this::toObservation)
                    .collect(Collectors.toList());
            TrackMatchResponse response = TrackMatchResponse.newBuilder().addAllObservations(observations).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(getGRPCException(exception));
        }
    }

}
