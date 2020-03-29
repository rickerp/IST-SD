package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.util.stream.Collectors;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

    private SiloServerBackend serverBackend = new SiloServerBackend();

    private ObservationDomain.Target toTargetDomain(Target target) {
        ObservationDomain.Target domainTarget = null;
        switch (target) {
            case CAR:
                domainTarget = ObservationDomain.Target.CAR;
                break;
            case PERSON:
                domainTarget = ObservationDomain.Target.PERSON;
                break;
        }
        return domainTarget;
    }

    private Target toTarget(ObservationDomain.Target domainTarget) {
        Target target = null;
        switch (domainTarget) {
            case CAR:
                target = Target.CAR;
                break;
            case PERSON:
                target = Target.PERSON;
                break;
        }
        return target;
    }

    private ObservationDomain toObservationDomain(Observation observation) {
        return new ObservationDomain(
                observation.getId(),
                toTargetDomain(observation.getTarget()),
                new Timestamp(System.currentTimeMillis())
        );
    }

    private Observation toObservation(ObservationDomain observationDomain) {
        return Observation.newBuilder()
                    .setId(observationDomain.getId())
                    .setTarget(toTarget(observationDomain.getTarget()))
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
        boolean joined = serverBackend.camJoin(request.getCameraName(), request.getLatitude(), request.getLongitude());
        CamJoinResponse response = CamJoinResponse.newBuilder()
                .setSuccess(joined)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
        boolean success = serverBackend.report(request.getCameraName(), request.getObservationsList().stream().map(this::toObservationDomain).collect(Collectors.toList()));
        ReportResponse response = ReportResponse.newBuilder().setSuccess(success).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {
        Observation observation = toObservation(serverBackend.track(toTargetDomain(request.getTarget()) , request.getId()))
        TrackResponse response = TrackResponse.newBuilder().setObservation(observation).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
