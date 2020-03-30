package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.ObservationObject.ObservationObject;

import java.sql.Timestamp;

public class ObservationDomain {

    private Camera camera;
    private ObservationObject object;
    private Timestamp timestamp;

    public ObservationDomain(ObservationObject object, Timestamp timestamp, Camera camera){
        setCamera(camera);
        setObject(object);
        setTimestamp(timestamp);
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ObservationObject getObservationObject() {
        return object;
    }

    public void setObject(ObservationObject object) {
        this.object = object;
    }
}
