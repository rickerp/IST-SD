package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.Object.Object;

import java.sql.Timestamp;

public class ObservationDomain {

    public Object object;
    private Timestamp timestamp;

    public ObservationDomain(Object object, Timestamp timestamp){
        setObject(object);
        setTimestamp(timestamp);
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
