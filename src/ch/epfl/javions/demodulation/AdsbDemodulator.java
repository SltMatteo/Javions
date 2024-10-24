package ch.epfl.javions.demodulation;

import ch.epfl.javions.Bits;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Représente un démodulateur de messages ADS-B.
 *
 * @author Eleonora Kazda (347477)
 * @author Gwenaëlle Queloz (356106)
 */
public final class AdsbDemodulator {

    private static final int WINDOW_SIZE = 1200;
    private final PowerWindow window;
    private final byte[] byteTab = new byte[14];
    private long timeStamp;
    private int pics;

    /**
     * Retourne un démodulateur obtenant les octets contenant les échantillons du flot passé en argument.
     *
     * @param samplesStream flot passé en argument.
     * @throws IOException si une erreur d'entrée/sortie se produit lors de la création de l'objet de type PowerWindow.
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        window = new PowerWindow(samplesStream, WINDOW_SIZE);
    }

    /**
     * Cherche les messages ADS-B valide.
     * Elle passe en revue chaque élément du flot en avançant la fenêtre d'un élément à un élément.
     *
     * @return le message suivant trouvé
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public RawMessage nextMessage() throws IOException {

       if (timeStamp != 0) window.advanceBy(window.size());

        while (window.isFull()) {
            int previousPics = pics;
            pics = window.get(0) + window.get(10) + window.get(35) + window.get(45);

            if (previousPics < pics) {
                int nextPics = window.get(1) + window.get(11) + window.get(36) + window.get(46);

                if (pics > nextPics) {
                    int vallees = window.get(5) + window.get(15) + window.get(20)
                            + window.get(25) + window.get(30) + window.get(40);

                    if (pics >= 2 * vallees) {
                        byteDecoder(byteTab, 0);
                        if ((Bits.extractUInt(byteTab[0], 3, 5)) == 17) {
                            for (int i = 1; i < byteTab.length; ++i) {
                                byteDecoder(byteTab, i);
                            }
                            timeStamp = window.position() * 100;
                            RawMessage rawMessage = RawMessage.of(timeStamp, byteTab);

                            if (rawMessage != null) {
                                return rawMessage;
                            }
                        }
                    }
                }
            }
            window.advance();
        }
        return null;
    }

    /**
     * Décode l'octet qui correspond à l'index donné qui est mis dans le tableau passé en argument
     *
     * @param byteTab le tableau
     * @param index   l'index
     */
    private void byteDecoder(byte[] byteTab, int index) {
        byte[] b = new byte[8];
        for (int j = 0; j < 8; ++j) {
            if (this.window.get(80 + 10 * (index * 8 + j)) >= this.window.get(85 + 10 * (index * 8 + j))) b[j] = 1;
            else b[j] = 0;
        }
        byteTab[index] = (byte) (b[0] << 7 | b[1] << 6 | b[2] << 5 | b[3] << 4
                | b[4] << 3 | b[5] << 2 | b[6] << 1 | b[7]);
    }
}