package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.grpc.*;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

    private SiloServerBackend serverBackend = new SiloServerBackend();

    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        PingResponse response = PingResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(ClearRequest request, StreamObserver<ClearResponse> responseObserver) {
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
                .setLatitude(camera.latitude)
                .setLongitude(camera.longitude)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        serverBackend.report(request.getCameraName(), request.getObservationsList());
        ReportResponse response = ReportResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
