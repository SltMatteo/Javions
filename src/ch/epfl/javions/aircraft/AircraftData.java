package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * represents the data of an airplane; it's regfistration number, type designator, model, description and wake turbulence category
 * params are self-explanatory
 * @param registration
 * @param typeDesignator
 * @param model
 * @param description
 * @param wakeTurbulenceCategory
 * @author Moyang Zhang (355928)
 */

public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator,
                           String model, AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {


    /** creates an instance of an aircraft's data, and make sure all the attributes are non null
     * params are self-explanatory
     * @param registration
     * @param typeDesignator
     * @param model
     * @param description
     * @param wakeTurbulenceCategory
     * @throws NullPointerException if any of the aircraft's atribute is null 
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }

}
