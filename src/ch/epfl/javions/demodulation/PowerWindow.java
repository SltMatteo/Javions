package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Représente une fenêtre de taille fixe sur une séquence d'échantillons de puissance produits par un calculateur de puissance.
 *
 * @author Eleonora Kazda (347477)
 * @author Gwenaëlle Queloz (356106)
 */
public final class PowerWindow {

    private final static int BATCH_SIZE = 1 << 16;
    private final int windowSize;
    private final PowerComputer powerComputer;
    private final int[] tab1 = new int[BATCH_SIZE];
    private int[] tab2 = new int[BATCH_SIZE];
    private long position;
    private int startIndex;
    private int endIndex;
    private int elementsTab1;
    private int elementsTab2;

    /**
     * Crée une fenêtre de taille donnée sur la séquence d'échantillons de puissance calculés à partir des octets fournis par le flot d'entrée donné.
     *
     * @param stream     le flot d'entré
     * @param windowSize la taille de la fenêtre
     * @throws IllegalArgumentException si la taille de la fenêtre donnée n'est pas comprise entre 0 (exclu) et 216 (inclus).
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= BATCH_SIZE);
        powerComputer = new PowerComputer(stream, BATCH_SIZE);
        elementsTab1 = powerComputer.readBatch(tab1);
        elementsTab2 = powerComputer.readBatch(tab2);
        this.windowSize = windowSize;
        endIndex = windowSize;
    }

    /**
     * @return la taille de la fenêtre
     */
    public int size() {
        return windowSize;
    }


    /**
     * @return la position actuelle de la fenêtre par rapport au début du flot de valeurs de puissance
     */
    public long position() {
        return position;
    }

    /**
     * Test si la fenêtre possède autant d'élément que sa taille.
     *
     * @return vrai si la fenêtre est pleine, faux sinon.
     */
    public boolean isFull() {
        if (endIndex < tab1.length && endIndex < elementsTab1) {
            return true;
        } else
            return endIndex < tab2.length + tab1.length && endIndex >= tab1.length && endIndex <= elementsTab2 + elementsTab1;
    }

    /**
     * Recherche dans la fenêtre l'échantillon à l'index donné.
     *
     * @param i l'index
     * @return l'échantillon de puissance à l'index donné de la fenêtre
     */
    public int get(int i) {
        if (startIndex + i < tab1.length) {
            return tab1[startIndex + i];
        } else {
            return tab2[startIndex + i - tab1.length];
        }
    }

    /**
     * Fait avancer la fenêtre, elle la déplace d'un échantillon.
     * Lorsqu'elle arrive à la fin du premier tableau, elle remplie le deuxième tableau.
     * À la fin du deuxième tableau, elle remplace le premier par le deuxième puis rempli le deuxième.
     *
     * @throws IOException s'il y a des problèmes d'entrée/sortie
     */
    public void advance() throws IOException {
        position++;
        startIndex++;
        endIndex++;
        if (endIndex == 2*BATCH_SIZE) {
            System.arraycopy(tab2, 0, tab1, 0, tab2.length);
            elementsTab1 = elementsTab2;
            elementsTab2 = powerComputer.readBatch(tab2);
            startIndex -= tab1.length;
            endIndex -= tab1.length;
        }
    }

    /**
     * Avance la fenêtre du nombre d'échantillons donné.
     *
     * @param offset le nombre d'échantillons
     * @throws IOException si offset n'est pas positif.
     */
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset > 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}
