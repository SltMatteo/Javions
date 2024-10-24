package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * Represents the observable state of an aircraft with its current information
 */
public final class ObservableAircraftState implements AircraftStateSetter {

    private final LongProperty lastMessageTimeStampNs;
    private final IntegerProperty category;
    private final ObjectProperty<CallSign> callSign;
    private final ObjectProperty<GeoPos> position;
    private final ObservableList<AirbornePos> modifiableTrajectory;
    private final ObservableList<AirbornePos> unmodifiableTrajectory;
    private double lastTrajectoryChangeTimeStampNs;
    private final DoubleProperty altitude;
    private final DoubleProperty velocity;
    private final DoubleProperty trackOrHeading;
    private final IcaoAddress address;
    private final AircraftData data;

    /**
     * Used to represent a position and an altitude together
     *
     * @param geoPos   the position of the aircraft
     * @param altitude the altitude of the aircraft
     */
    public record AirbornePos(GeoPos geoPos, double altitude) { }

    /**
     * @param address the ICAO address of the aircraft
     * @param data    the data of the aircraft
     */
    public ObservableAircraftState(IcaoAddress address, AircraftData data) {
        this.lastMessageTimeStampNs = new SimpleLongProperty(0);
        this.category = new SimpleIntegerProperty(0);
        this.callSign = new SimpleObjectProperty<>();
        this.callSign.set(null);
        this.position = new SimpleObjectProperty<>();
        this.position.set(null);
        this.modifiableTrajectory = FXCollections.observableArrayList();
        this.unmodifiableTrajectory = FXCollections.unmodifiableObservableList(modifiableTrajectory);
        this.lastTrajectoryChangeTimeStampNs = 0;
        this.altitude = new SimpleDoubleProperty(Double.NaN);
        this.velocity = new SimpleDoubleProperty(Double.NaN);
        this.trackOrHeading = new SimpleDoubleProperty(0);
        this.address = Objects.requireNonNull(address); // ?
        this.data = data;

    }

    /**
     * Returns the ICAO address of the aircraft
     *
     * @return the ICAO address of the aircraft
     */
    public IcaoAddress getAddress() {
        return this.address;
    }

    /**
     * Returns the data of the aircraft
     *
     * @return the data of the aircraft
     */
    public AircraftData getData() {
        return this.data;
    }

    /**
     * Returns the property of the latest timestamp of the aircraft
     *
     * @return the property of the latest timestamp of the aircraft
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    /**
     * Returns the value of the latest timestamp of the aircraft
     *
     * @return the value of the latest timestamp of the aircraft
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    /**
     * Sets the latest timestamp value to its property
     *
     * @param timeStampNs the value of the timestamp
     */
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }

    /**
     * Returns the property of the category of the aircraft
     *
     * @return the property of the category of the aircraft
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    /**
     * Returns the value of the category of the aircraft
     *
     * @return the value of the category of the aircraft
     */
    public int getCategory() {
        return category.get();
    }

    /**
     * Sets the category of the aircraft
     *
     * @param category the category
     */
    public void setCategory(int category) {
        this.category.set(category);
    }

    /**
     * Returns the property of the call sign of the aircraft
     *
     * @return the property of the call sign of the aircraft
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    /**
     * Returns the value of the call sign of the aircraft
     *
     * @return the value of the call sign of the aircraft
     */
    public CallSign getCallSign() {
        return callSign.get();
    }

    /**
     * Sets the call sign of the aircraft
     *
     * @param callSign the call sign
     */
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    /**
     * Returns the property of the position of the aircraft
     *
     * @return the property of the position of the aircraft
     */
    public ReadOnlyObjectProperty<GeoPos> geoPositionProperty() {
        return position;
    }

    /**
     * Returns the value of the position of the aircraft
     *
     * @return the value of the position of the aircraft
     */
    public GeoPos getPosition() {
        return position.get();
    }

    /**
     * Sets the position of the aircraft
     *
     * @param geoPos the position
     */
    public void setPosition(GeoPos geoPos) {
        if (!Double.isNaN(getAltitude())) {
            modifiableTrajectory.add(new AirbornePos(geoPos, getAltitude()));
            lastTrajectoryChangeTimeStampNs = getLastMessageTimeStampNs();
        }
        position.set(geoPos);
    }

    /**
     * Returns the trajectory the aircraft
     *
     * @return the trajectory of the aircraft
     */
    public ObservableList<AirbornePos> getTrajectory() {
        return unmodifiableTrajectory;
    }


    /**
     * Returns the property of the altitude of the aircraft
     *
     * @return the property of the altitude of the aircraft
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    /**
     * Returns the value of the altitude of the aircraft
     *
     * @return the value of the altitude of the aircraft
     */
    public double getAltitude() {
        return altitude.get();
    }

    /**
     * Sets the altitude of the aircraft
     *
     * @param altitude the altitude
     */
    public void setAltitude(double altitude) {
        if (!Objects.isNull(getPosition())) {
            if (modifiableTrajectory.isEmpty()) {
                modifiableTrajectory.add(new AirbornePos(getPosition(), altitude));
            } else if (getLastMessageTimeStampNs() == lastTrajectoryChangeTimeStampNs) {
                modifiableTrajectory.set(modifiableTrajectory.size()-1, new AirbornePos(getPosition(), altitude));
            }
            lastTrajectoryChangeTimeStampNs = getLastMessageTimeStampNs();
        }
        this.altitude.set(altitude);

    }

    /**
     * Returns the property of the velocity of the aircraft
     *
     * @return the property of the velocity of the aircraft
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    /**
     * Returns the value of the velocity of the aircraft
     *
     * @return the value of the velocity of the aircraft
     */
    public double getVelocity() {
        return velocity.get();
    }

    /**
     * Sets the velocity of the aircraft
     *
     * @param velocity the velocity
     */
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    /**
     * Returns the property of the track and the heading of the aircraft
     *
     * @return the property of the track and the heading of the aircraft
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    /**
     * Returns the value of the track and the heading of the aircraft
     *
     * @return the value of the track and the heading of the aircraft
     */
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    /**
     * Sets the track and heading of the aircraft
     *
     * @param trackOrHeading the track and the heading
     */
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }
}