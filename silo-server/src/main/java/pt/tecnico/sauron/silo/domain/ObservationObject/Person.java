package pt.tecnico.sauron.silo.domain.ObservationObject;

import pt.tecnico.sauron.silo.domain.exception.SiloArgumentException;

public class Person extends ObservationObject {

    private long id;

    public Person(long id) throws SiloArgumentException {
        setId(id);
    }

    public long getId() {
        return id;
    }

    @Override
    public String getStringId() {
        return Long.toString(id);
    }

    public void setId(long id) throws SiloArgumentException {
        if (id < 0) {
            throw new SiloArgumentException("Person id should be >= 0");
        }
        this.id = id;
    }

    @Override
    public boolean equals(java.lang.Object object) {
        return object instanceof Person &&
                ((Person) object).id == id;
    }
}
