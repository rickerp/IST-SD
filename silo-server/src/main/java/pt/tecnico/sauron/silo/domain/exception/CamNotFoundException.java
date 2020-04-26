package pt.tecnico.sauron.silo.domain.exception;

public class CamNotFoundException extends SiloException {
    public CamNotFoundException(String cameraName) {
        super("Camera with name " + cameraName + " does not exist.");
    }
}
