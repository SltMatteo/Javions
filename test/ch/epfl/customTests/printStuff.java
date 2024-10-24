package ch.epfl.customTests;


import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.gui.AircraftStateManager;
import ch.epfl.javions.gui.ObservableAircraftState;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class printStuff {

    public static void main(String[] args) throws IOException {
        long i = 0;
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("/Users/matteo/Desktop/Javions/resources/messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];

            AircraftDatabase aircraftDatabase = new AircraftDatabase("/Users/matteo/Desktop/Javions/resources/messages_20230318_0915.bin");
            AircraftStateManager a = new AircraftStateManager(aircraftDatabase);

            while (true) {

                /*
                String CSI = "\u001B[";
                String CLEAR_SCREEN = CSI + "2J";
                System.out.print(CLEAR_SCREEN);
                 */

                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                ++i;

                Message m = MessageParser.parse(new RawMessage(timeStampNs, message));
                //check si c pas nul
                a.updateWithMessage(m);
                a.purge();
                //System.out.printf(i + ": " + m.timeStampNs() + ": " + "%13d: %s\n", timeStampNs, message);
                List<ObservableAircraftState> l = new ArrayList<>(a.states());
                l.sort(new AddressComparator());

                long startTime = System.nanoTime();

                AircraftData d;
                for (ObservableAircraftState o : l) {

                    d = o.getData();
                    //if (startTime < timeStampNs) {
                    //    Thread.sleep(timeStampNs - startTime);
                    //}
                    if (d != null) {
                        CallSign c = o.getCallSign();
                        System.out.println(i + ": " + o.getAddress().string() + " - " + (c != null ? c.string() : "       ") + " - " + d.registration().string() + " - " +
                                d.model() + " - " + o.getPosition() + " - " + o.getAltitude() + " - " + o.getVelocity()*3.6);
                    }
                }
            }
        } catch (EOFException e) { /* nothing to do */ }
    }

    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.getAddress().string();
            String s2 = o2.getAddress().string();
            return s1.compareTo(s2);
        }
    }
}
