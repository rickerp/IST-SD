package pt.tecnico.sauron.silo.domain.ObservationObject;

import pt.tecnico.sauron.silo.domain.SiloException;

public class Car extends ObservationObject {

    private String plate;

    public Car(String plate) throws SiloException {
        setPlate(plate);
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) throws SiloException {
        if (plate.length() != 6) {
            throw new SiloException("Plate should have 6 characters.");
        }

        int letterGroups = 0;
        int numberGroups = 0;

        for (int i = 0; i < plate.length(); ++i) {
            String group = plate.substring(i, i + 1);
            if (group.matches("[A-Z][A-Z]")) {
                letterGroups++;
            } else if (group.matches("[0-9][0-9]")) {
                numberGroups++;
            } else {
                throw new SiloException("Plate has invalid formatting.");
            }
        }

        if (letterGroups > 2 || numberGroups > 2) {
            throw new SiloException("Plate has invalid formatting.");
        }

        this.plate = plate;
    }

    @Override
    public boolean equals(java.lang.Object object) {
        return object instanceof Car &&
                ((Car) object).plate.equals(plate);
    }

}
