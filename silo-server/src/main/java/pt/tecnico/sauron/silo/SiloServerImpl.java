package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
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
    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        ReportResponse response = ReportResponse.getDefaultInstance();
        System.out.print(request.getObservationsCount());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
