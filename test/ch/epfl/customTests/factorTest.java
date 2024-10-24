package ch.epfl.customTests;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class factorTest {

//    @Test
//    public void factorTime() {
//        ArrayList<Integer> primes = new ArrayList<>();
//        long start = System.nanoTime();
//        for(int i = 0; i < 1000; i++) {
//            if(factorsList(i).size() == 2) primes.add(i);
//        }
//        long end = System.nanoTime();
//        System.out.println("time: " + (end-start));
//        for(int i : primes) System.out.println(i);
//
//    }

//    @Test
//    public void prime() {
//        int max = 0;
//        int index = 0;
//        for(int i = 1; i < 500; i++) {
//            ArrayList<Integer> current = primeFactorization(i);
//            if (current.size() > max) {
//                max = current.size();
//                index = i;
//            }
//            System.out.println("step " + i + ": " + current);
//        }
//        System.out.println("max: " + max + ", at index " + index);
//    }

    private ArrayList<Integer> factorsList(int number) {
        ArrayList<Integer> res = new ArrayList<>();
        for(int i = 1; i <= number; i++)
            if(number%i == 0) {
                res.add(i);
            }
        return res;
    }

    private ArrayList<Integer> primeFactorization(int number) {
        int end = number;
        ArrayList<Integer> res = new ArrayList<>();
        for(int i = 2; i <= end/2; i++) {
            if(isPrime(i)) {
                while(number%i == 0) {
                    res.add(i);
                    number/=i;
                }
            }
        }
        return (res.size() == 0) ? new ArrayList<>(Arrays.asList(number)) : res;
    }
    
    private boolean isPrime(int i) {
        for (int j = 2; j < Math.floor(Math.sqrt(i)) + 1; j++) {
            if(i%j == 0) return false;
        }
        return true;
    }
}
