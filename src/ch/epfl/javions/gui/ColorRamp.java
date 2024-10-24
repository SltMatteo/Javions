package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

/**
 * Represents a gradient of colors
 */
public final class ColorRamp {

    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));

    private final Color[] colors;
    private final double segmentSize; // Size of the segments

    /**
     * @param colors the colors we create a gradient from
     */
    public ColorRamp(Color... colors) {
        Preconditions.checkArgument(colors.length >= 2);
        this.colors = colors.clone();
        this.segmentSize = 1d / (colors.length - 1);
    }

    /**
     * Returns the color corresponding to a given value on the color ramp
     * @param value the value
     * @return the color corresponding to value
     */
    public Color at(double value) {
        if (value < 0) return colors[0];
        if (value > 1) return colors[colors.length - 1];
        int segmentIndex = (int) (value / segmentSize);
        Color first = colors[segmentIndex];
        if (segmentIndex + 1 == colors.length) return first;
        Color second = colors[segmentIndex + 1];
        double positionInSegment = (value - segmentIndex * segmentSize) / segmentSize;
        return first.interpolate(second, positionInSegment);
    }
}