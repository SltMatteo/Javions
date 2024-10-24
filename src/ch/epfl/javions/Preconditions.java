package ch.epfl.javions;

/**
 * contains a method that checks the validity of statements user in later methods
 * this class can't be instanciated 
 * @author Matteo Pinto 
 */


public final class Preconditions {

    // prevent instanciation 
    private Preconditions() {}

    /**
     * checks the validity of a statement and throws an error if the statement is false
     * @param shouldBeTrue boolean expression that should be true
     * @throws IllegalArgumentException if ShouldBeTrue is false
     */
    
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }

    public static void checkArgument(boolean shouldBeTrue, String errorMessage) {
        if (!shouldBeTrue) throw new IllegalArgumentException(errorMessage);
    }
}
