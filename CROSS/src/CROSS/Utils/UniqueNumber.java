package CROSS.Utils;

/**
 * Class to generate unique numbers.
 * Used to generate unique IDs for orders or users.
 * @version 1.0
 */
public class UniqueNumber {
    
    private static Long lastTime = Long.valueOf(0);
    private static Long lastNumber;
    
    private Long number;

    /**
     * Constructor to generate a unique number.
     * The number is generated by concatenating the current time in milliseconds and a number that increments if the time is the same.
     * It is synchronized to avoid conflicts between threads.
     */
    public UniqueNumber() {
        synchronized (UniqueNumber.class) {
            long currentTime = System.currentTimeMillis();
            if (currentTime == lastTime.longValue()) {
                lastNumber++;
            } else {
                lastTime = Long.valueOf(currentTime);
                lastNumber = Long.valueOf(0);
            }
            this.number = lastTime + lastNumber;
        }
    }

    /**
     * Get the unique number.
     * @return The unique number as Long.
     */
    public Long getNumber() {
        return number;
    }

}