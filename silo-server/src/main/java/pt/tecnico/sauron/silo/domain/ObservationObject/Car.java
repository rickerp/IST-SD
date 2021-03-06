package pt.tecnico.sauron.silo.domain.ObservationObject;

import pt.tecnico.sauron.silo.domain.exception.SiloArgumentException;

public class Car extends ObservationObject {

    private String plate;

    public Car(String plate) throws SiloArgumentException {
        setPlate(plate);
    }

    public String getPlate() {
        return plate;
    }

    @Override
    public String getStringId() {
        return plate;
    }

    public void setPlate(String plate) throws SiloArgumentException {
        if (plate.length() != 6) {
            throw new SiloArgumentException("Plate should have 6 characters.");
        }

        int letterGroups = 0;
        int numberGroups = 0;

        for (int i = 0; i < plate.length(); i += 2) {
            String group = plate.substring(i, i + 2);
            if (group.matches("[A-Z][A-Z]")) {
                letterGroups++;
            } else if (group.matches("[0-9][0-9]")) {
                numberGroups++;
            } else {
                throw new SiloArgumentException("Plate has invalid formatting.");
            }
        }

        if (letterGroups > 2 || numberGroups > 2) {
            throw new SiloArgumentException("Plate has invalid formatting.");
        }

        this.plate = plate;
    }

    @Override
    public boolean equals(java.lang.Object object) {
        return object instanceof Car &&
                ((Car) object).plate.equals(plate);
    }

}
