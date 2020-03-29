package pt.tecnico.sauron.silo.domain.Object;

import pt.tecnico.sauron.silo.domain.SiloException;

public class Person extends Object {

    private long id;

    public Person(int id) throws SiloException {
        setId(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) throws SiloException {
        if (id < 0) {
            throw new SiloException("Person id should be >= 0");
        }
        this.id = id;
    }

    @Override
    public boolean equals(java.lang.Object object) {
        return object instanceof Person &&
                ((Person) object).id == id;
    }
}
