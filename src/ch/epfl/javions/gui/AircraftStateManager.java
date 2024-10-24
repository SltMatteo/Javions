package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class is the manager that handles all the visible aircrafts states
 */
public final class AircraftStateManager {

    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> recentAircraftState;
    private final ObservableSet<ObservableAircraftState> modifiableStates;
    private final ObservableSet<ObservableAircraftState> unmodifiableStates;
    private final AircraftDatabase aircraftDatabase;
    private long timeStampNsLastMessage;
    private static final long MINUTE = 60_000_000_000L; // One minute expressed in nanoseconds

    /**
     * Instantiate the state manager
     * @param aircraftDatabase the aircraft database
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftDatabase = aircraftDatabase;
        recentAircraftState = new HashMap<>();
        modifiableStates = FXCollections.observableSet();
        this.unmodifiableStates = FXCollections.unmodifiableObservableSet(modifiableStates);
    }

    /**
     * Returns the non-modifiable view of the set of observable aircraft states with known position
     *
     * @return the non-modifiable view of the set of observable aircraft states
     */
    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableStates;
    }

    /**
     * Updates the state associated with the airplane that sent the message or create a new state if
     * it is the first message sent by an aircraft
     *
     * @param message the message sent by an aircraft
     */
    public void updateWithMessage(Message message) {
        if (Objects.isNull(message)) return;
        IcaoAddress icao = message.icaoAddress();
        if (!recentAircraftState.containsKey(icao)) {
            ObservableAircraftState state = new ObservableAircraftState(icao, aircraftDatabase.get(icao));
            recentAircraftState.put(icao, new AircraftStateAccumulator<>(state));
        }
        recentAircraftState.get(icao).update(message);
        ObservableAircraftState state = recentAircraftState.get(icao).stateSetter();
        if (state.getPosition() != null) {
            modifiableStates.add(state);
        }
        timeStampNsLastMessage = message.timeStampNs();
    }

    /**
     * Remove from the state manager all aircraft that have not sent a message in the last minute
     */
    public void purge() {
        modifiableStates.removeIf(state -> {
            boolean shouldRemove = timeStampNsLastMessage - state.getLastMessageTimeStampNs() > MINUTE;
            if (shouldRemove) {
                recentAircraftState.remove(state.getAddress());
            }
            return shouldRemove;
        });
    }
}