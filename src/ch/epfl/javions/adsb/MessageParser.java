package ch.epfl.javions.adsb;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * //TODO
 * @author Moyang Zhang (355928)
 */


public final class MessageParser {

    //private static final Integer[] IDENTIFICATION_MESSAGE_TYPE_CODES  = {1,2,3,4};
    private static final Set<Integer> IDENTIFICATION_MESSAGE_TYPE_CODE = Set.of(1,2,3,4);
    private static final Integer[] POSITION_MESSAGE_TYPE_CODES = {9,10,11,12,13,14,15,16,17,18,20,21,22};
    private static final int VELOCITY_MESSAGE_TYPE_CODE = 19;

    private MessageParser() {}

    public static Message parse(RawMessage rawMessage) {

        //TODO refaire ce bail

        int typeCode = rawMessage.typeCode();

        //if (Arrays.asList(IDENTIFICATION_MESSAGE_TYPE_CODES).contains(typeCode)) {
        if (IDENTIFICATION_MESSAGE_TYPE_CODE.contains(typeCode)) {
            AircraftIdentificationMessage message = AircraftIdentificationMessage.of(rawMessage);
            return (Objects.isNull(message)) ? null : message;
        }
        if (Arrays.asList(POSITION_MESSAGE_TYPE_CODES).contains(typeCode)) {
            AirbornePositionMessage message = AirbornePositionMessage.of(rawMessage);
            return (Objects.isNull(message)) ? null : message;
        }
        if (typeCode == VELOCITY_MESSAGE_TYPE_CODE) {
            AirborneVelocityMessage message = AirborneVelocityMessage.of(rawMessage);
            return (Objects.isNull(message)) ? null : message;
        }
        else return null;
    }
}

