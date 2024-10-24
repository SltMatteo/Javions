package ch.epfl.customTests;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

public class CheckJavaFX {
    public static void main(String[] args) {
        Color c = Color.RED;
        System.out.println(c.getRed());
    }


    public static String fixedLengthString(String string, int length) {
        return String.format("%" + length + "s", string).replace(' ', '0');
    }

//    @Test
//    public void testFixedLengthString() {
//        int[] array = new int[]{1, 2, 3, 4};
//        int[] otherArray = new int[] {1,2,3,4};
//        int[] yetAnotherArray = new int[] {2,3,4,5};
//        double[] lastArray = new double[]{1.0, 2.0, 3.0, 4.0};
//        System.out.println(array);
//        System.out.println(otherArray);
//        System.out.println(yetAnotherArray);
//        System.out.println(lastArray);
//        StringJoiner Bob = new StringJoiner(",", "{", "}");
//        for(int i : array) Bob.add(Integer.toString(i));
//        System.out.println(Bob);
//        System.out.println(Bob.toString());
//    }


}
