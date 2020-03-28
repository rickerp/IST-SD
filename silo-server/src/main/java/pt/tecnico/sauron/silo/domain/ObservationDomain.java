package pt.tecnico.sauron.silo.domain;

import java.sql.Timestamp;

public class ObservationDomain {
    public enum Target {
        CAR,
        PERSON
    }

    private String id;
    private Timestamp timestamp;
    private Target target;

    public ObservationDomain(String id, Target target, Timestamp timestamp){
        this.id = id;
        this.target = target;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

}
