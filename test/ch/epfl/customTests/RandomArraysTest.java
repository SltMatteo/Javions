package ch.epfl.customTests;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RandomArraysTest {

    private static final Integer[] firstArray = {1,2,3,4,5};

//    @Test
//    public void shuffle() {
//        String[] array = {"un", "deux", "trois"};
//        List<String> a = List.of(array);
//        System.out.println(a);
//        System.out.println(a);
//    }

//    @Test
//    public void arrayContains() {
//        List<Integer> myList = new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//            myList.add(i);
//        }
//        int in = 3;
//        int out = 9;
//        System.out.println(Arrays.asList(firstArray).contains(in));
//        System.out.println(Arrays.asList(firstArray).contains(out));
//        System.out.println(myList.contains(in));
//        System.out.println(myList.contains(out));
//    }


//    @Test
//    public void loopCompare() {
//        int passes = 10;
//        int overallRegWins = 0;
//        int overallRecWins = 0;
//        for (int j = 0; j < passes; j++) {
//            int regularWin = 0;
//            int recursiveWin = 0;
//            long maxDiff = 0;
//            for (int i = 1; i < passes; i++) {
//                long startTime = System.nanoTime();
//                computeSquaresSum(passes);
//                long endTime = System.nanoTime();
//                long regTime = (endTime - startTime);
//                startTime = System.nanoTime();
//                recursiveSquaresSum(passes);
//                endTime = System.nanoTime();
//                long recTime = (endTime - startTime);
//                if (regTime < recTime) regularWin++;
//                else recursiveWin++;
//                long m = Math.abs(recTime - regTime);
//                if(m > maxDiff) maxDiff = m;
//            }
//            if (regularWin > recursiveWin) overallRegWins++;
//            else overallRecWins++;
//            System.out.println("\nregular wins: " + regularWin);
//            System.out.println("recursive wins: " + recursiveWin);
//            System.out.println("maxDiff: " + maxDiff);
//        }
//        System.out.println("\n\noverall regular wins: " + overallRegWins);
//        System.out.println("overall recursive wins: " + overallRecWins);
//
//    }

    private long computeSquaresSum(int i) {
        int r = 0;
        for (int j = 1; j <= i ; j++) {
            r += j*j;
        }
        return r;
    }

    private long recursiveSquaresSum(int i) {
        if (i == 1) return 1;
        else return ((i*i)+recursiveSquaresSum(i-1));
    }
}
