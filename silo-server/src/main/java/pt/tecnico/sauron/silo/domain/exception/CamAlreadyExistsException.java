package pt.tecnico.sauron.silo.domain.exception;

public class CamAlreadyExistsException extends SiloException {
    public CamAlreadyExistsException(String cameraName) {
        super("Camera with name " + cameraName + " already exists.");
    }
}
