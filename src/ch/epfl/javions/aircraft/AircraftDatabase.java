package ch.epfl.javions.aircraft;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipFile;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * //TODO
 * @author Matteo Pinto (326649)
 * @author Moyang Zhang (355928)
 */

public final class AircraftDatabase {

    private final String fileName;
    private static final int AIRCRAFT_NUMBER_OF_ATTRIBUTE = 5;
    private static final int REGISTRATION_POS = 1; //the next 5 attributes describe the order in which the characteristic are written in the database
    private static final int DESIGNATOR_POS = 2;
    private static final int MODEL_POS = 3;
    private static final int DESCRIPTION_POS = 4;
    private static final int CATEGORY_POS = 5;

    public AircraftDatabase(String fileName) {
        if (fileName == null) {
            throw new NullPointerException();
        }
        this.fileName = fileName;
    }

    public AircraftData get(IcaoAddress address) {
        String[] res = new String[AIRCRAFT_NUMBER_OF_ATTRIBUTE];
        int length = address.string().length();
        String zipFile = AircraftDatabase.class.getResource("/aircraft.zip").getFile();
        String last2Chars = address.string().substring(length - 2, length);
        String first2Chars = address.string().substring(0, 2);
        try (ZipFile z = new ZipFile(zipFile);
             InputStream stream = z.getInputStream(z.getEntry(last2Chars + ".csv"));
             Reader reader = new InputStreamReader(stream, UTF_8);
             BufferedReader buffer = new BufferedReader(reader)) {
            String line = "";
            while ((line = buffer.readLine()) != null)
                if (line.startsWith(first2Chars)) {
                    String[] lSplit = line.split(",", 2);
                    String firstParam = lSplit[0];
                    if (firstParam.compareTo(address.string()) == 0) {
                        res = line.split(",");
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Arrays.stream(res).anyMatch(Objects::isNull)) { //checks if any of the entries in 'res' is null
            return null;
        }

        AircraftRegistration registration = new AircraftRegistration(res[REGISTRATION_POS]);
        AircraftTypeDesignator typeDesignator = new AircraftTypeDesignator(res[DESIGNATOR_POS]);
        String model = res[MODEL_POS];
        AircraftDescription description = new AircraftDescription(res[DESCRIPTION_POS]);
        WakeTurbulenceCategory wakeTurbulenceCategory = WakeTurbulenceCategory.of(res[CATEGORY_POS]);

        return new AircraftData(registration, typeDesignator, model, description, wakeTurbulenceCategory);
    }


   //much cleaner eleonora version
//    public AircraftData get(IcaoAddress icaoAddress) throws IOException {
//        String file = icaoAddress.string().charAt(4) + "" + icaoAddress.string().charAt(5) + ".csv";
//        try (ZipFile z = new ZipFile(fileName);
//             InputStream s = z.getInputStream(z.getEntry(file));
//             Reader r = new InputStreamReader(s, UTF_8);
//             BufferedReader b = new BufferedReader(r)) {
//            String address;
//            String previousAddress = null;
//            while (((address = b.readLine())) != null) {
//                if (icaoAddress.string().equals(address.split(",", -1)[0])) {
//                    return new AircraftData(
//                            new AircraftRegistration(address.split(",", -1)[1]),
//                            new AircraftTypeDesignator(address.split(",", -1)[2]),
//                            address.split(",", -1)[3],
//                            new AircraftDescription(address.split(",", -1)[4]),
//                            WakeTurbulenceCategory.of(String.valueOf(address.split(",", -1)[5]))
//                    );
//                }
//                if (previousAddress != null && address.compareTo(previousAddress) < 0) {
//                    return null;
//                }
//                previousAddress = String.copyValueOf(address.toCharArray());
//            }
//            s.close();
//            return null;
//        }
//    }
}