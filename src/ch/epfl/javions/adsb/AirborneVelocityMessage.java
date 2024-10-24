package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;
import java.util.Objects;

/**
 * //TODO
 * @param timeStampNs
 * @param icaoAddress
 * @param speed
 * @param trackOrHeading
 * @author Matteo Pinto (326649)
 */

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) implements Message {

    public AirborneVelocityMessage {
        Preconditions.checkArgument(timeStampNs >= 0, "time stamp must be non negative: " + timeStampNs);
        Preconditions.checkArgument(speed >= 0, "speed mut be non negative: " + speed);
        Preconditions.checkArgument(trackOrHeading >= 0, "trackOrHeading must be non negative: " + trackOrHeading);
        Objects.requireNonNull(icaoAddress);
    }

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();
        int ST = Bits.extractUInt(payload, 48, 3);
        double trackOrHeading = Bits.extractUInt(payload, 21, 22);
        double speed = 0;
        boolean unknownSpeedOrInvalidST = false;
        double DewOrSH = Bits.extractUInt((int) trackOrHeading, 21, 1);
        double VewOrHDG = Bits.extractUInt((int )trackOrHeading, 11, 10);
        double DnsOrT = Bits.extractUInt((int) trackOrHeading, 10, 1);
        double VnsOrAS = Bits.extractUInt((int) trackOrHeading, 0, 10);
        switch (ST) {
            case (1) : case (2) : {
                if (VnsOrAS == 0 || VewOrHDG == 0) unknownSpeedOrInvalidST = true;
                VnsOrAS--; VewOrHDG--;
                speed = Math.hypot(VnsOrAS, VewOrHDG);
                speed = (ST == 1) ? speed : speed*4; //expressed in knots (sub/super)
                if (DewOrSH == 0) DewOrSH = -1;
                if (DnsOrT == 0) DnsOrT = -1;
                trackOrHeading = Math.atan2(DewOrSH*VewOrHDG, DnsOrT*VnsOrAS); //expressed in rads
                //trackOrHeading = (trackOrHeading < 0) ? trackOrHeading + Math.PI : trackOrHeading + Math.PI; //angle with north (rads)
                trackOrHeading += Math.PI; //north angle rads
                break;
            }
            case (3) : case (4) : {
                if (DewOrSH == 0) unknownSpeedOrInvalidST = true;
                trackOrHeading = VewOrHDG / (1 << 10); //heading expressed in turns
                trackOrHeading = Units.convertFrom(trackOrHeading, Units.Angle.TURN);
                if (VnsOrAS == 0) unknownSpeedOrInvalidST = true;
                VnsOrAS--;
                speed = (ST == 3) ? VnsOrAS : VnsOrAS*4; //expressed in knots (sub/super)
                break;
            }
            default : unknownSpeedOrInvalidST = true;
        }
        if (unknownSpeedOrInvalidST) return null;
        long timeStampNs = rawMessage.timeStampNs();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        speed = Units.convertFrom(speed, Units.Speed.KNOT);
        return new AirborneVelocityMessage(timeStampNs, icaoAddress, speed, trackOrHeading);
    }
}
