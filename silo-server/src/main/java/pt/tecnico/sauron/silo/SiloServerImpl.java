package pt.tecnico.sauron.silo;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.ObservationDomain;
import pt.tecnico.sauron.silo.grpc.*;

import java.sql.Timestamp;
import java.util.stream.Collectors;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

    private SiloServerBackend serverBackend = new SiloServerBackend();

    private ObservationDomain toObservationDomain(Observation observation) {
        ObservationDomain.Target domainTarget = null;
        switch (observation.getTarget()) {
            case CAR:
                domainTarget = ObservationDomain.Target.CAR;
                break;
            case PERSON:
                domainTarget = ObservationDomain.Target.PERSON;
                break;
        }

        return new ObservationDomain(
                observation.getId(),
                domainTarget,
                new Timestamp(System.currentTimeMillis())
        );
    }

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
        serverBackend.report(request.getCameraName(), request.getObservationsList().stream().map(this::toObservationDomain).collect(Collectors.toList()));
        ReportResponse response = ReportResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
