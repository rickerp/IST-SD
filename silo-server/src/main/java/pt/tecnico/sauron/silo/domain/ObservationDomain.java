package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;

import java.sql.Timestamp;

public class ObservationDomain {

    public ObservationObject object;
    private Timestamp timestamp;

    public ObservationDomain(ObservationObject object, Timestamp timestamp){
        setObject(object);
        setTimestamp(timestamp);
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ObservationObject getObject() {
        return object;
    }

    public void setObject(ObservationObject object) {
        this.object = object;
    }
}
