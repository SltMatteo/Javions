package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * //TODO
 * @author Moyang Zhang (355928)
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private final T state;

    private final int EVEN_MESSAGE = 0;
    private final int ODD_MESSAGE = 1;
    private final double TIME_DIFFERENCE_LIMIT = 1e10;

    private AirbornePositionMessage lastEvenMessage;
    private AirbornePositionMessage lastOddMessage;
    private double lastPositionMessageTimeStampNs;


    public AircraftStateAccumulator(T stateSetter) {
        this.state = Objects.requireNonNull(stateSetter);
        this.lastOddMessage = null;
        this.lastEvenMessage = null;
        this.lastPositionMessageTimeStampNs = 0;
    }

    public T stateSetter() {
        return this.state;
    }

    public void update(Message message) {

        state.setLastMessageTimeStampNs(message.timeStampNs());

        switch (message) {

            case AircraftIdentificationMessage aim -> {
                state.setCategory(aim.category());
                state.setCallSign(aim.callSign());
            }

            case AirborneVelocityMessage avm -> {
                state.setVelocity(avm.speed());
                state.setTrackOrHeading(avm.trackOrHeading());
            }

            case AirbornePositionMessage apm  -> {
                state.setAltitude(apm.altitude());

                if (apm.parity() == EVEN_MESSAGE) this.lastEvenMessage = apm;
                else if (apm.parity() == ODD_MESSAGE) this.lastOddMessage = apm;

                double currentTimeStampNs = apm.timeStampNs();
                double tmpPositionMessageTimeStampsNs = lastPositionMessageTimeStampNs;
                lastPositionMessageTimeStampNs = currentTimeStampNs;

                if (Math.abs(tmpPositionMessageTimeStampsNs - currentTimeStampNs) > TIME_DIFFERENCE_LIMIT) break;
                if (Objects.isNull(lastOddMessage) || Objects.isNull(lastEvenMessage)) break;

                GeoPos position = CprDecoder.decodePosition(
                        lastEvenMessage.x(),
                        lastEvenMessage.y(),
                        lastOddMessage.x(),
                        lastOddMessage.y(),
                        apm.parity()
                );
                if (position != null) state.setPosition(position);
            }

            default -> {}
        }
    }
}
