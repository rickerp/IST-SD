package pt.tecnico.sauron.silo.domain.ObservationObject;

public abstract class ObservationObject {

    @Override
    public abstract boolean equals(Object object);

    public abstract String getStringId();

    @Override
    public int hashCode() {
        return this.getStringId().hashCode();
    }

}
