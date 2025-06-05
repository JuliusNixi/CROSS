package cross.utils;

/**
 *
 * This class is used to create a very simple separator string.
 * It is used to format the output of the program and make it more readable (useful for debugging).
 *
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see cross.api.notifications.Notification
 *
 */
public class Separator {

    private final static Integer DEFAULT_LENGTH = 42;

    // Will contain the final created separator.
    private final String separator;

    // A separator could also be a sequence of characters, for that is a String.
    // The characters used to create the separator.
    private final String characters;

    // The length of the separator.
    // How many times the sequence of characters is repeated.
    private final Integer length;

    // CONSTRUCTORS
    /**
     *
     * Constructor with two parameters.
     *
     * Synchronized on characters to avoid changing the string while using it to create the separator.
     *
     * @param characters The sequence of characters to be repeated.
     * @param length The number of times the sequence of characters is repeated.
     *
     * @throws NullPointerException If characters or length are null.
     * @throws IllegalArgumentException If length is less than or equal to 0.
     *
     */
    public Separator(String characters, Integer length) throws NullPointerException, IllegalArgumentException {

        // Null checks.
        if (characters == null) {
            throw new NullPointerException("Characters used in the separator cannot be null.");
        }
        if (length == null) {
            throw new NullPointerException("Length used in the separator cannot be null.");
        }

        // Integer discourages synchronization.
        synchronized (characters) {

            // Length check.
            if (length <= 0) {
                throw new IllegalArgumentException("Length used in the separator must be greater than 0.");
            }

            // Create the separator.
            String sep = "";
            for (int i = 0; i < length; i++) {
                sep += characters;
            }

            this.characters = characters;
            this.separator = sep;
            this.length = length;

        }

    }

    /**
     *
     * Constructor with one parameter. It uses the default length.
     *
     * @param characters The sequence of characters to be repeated.
     *
     * @throws NullPointerException If characters is null.
     *
     */
    public Separator(String characters) throws NullPointerException {

        this(characters, Separator.DEFAULT_LENGTH);

    }
    
    // GETTERS
    /**
     *
     * Returns the default length of the separator.
     *
     * @return The default length of the separator as an Integer.
     *
     */
    public static Integer getDefaultLength() {

        return DEFAULT_LENGTH;

    }
    /**
     *
     * Returns the characters of the separator.
     *
     * @return The characters of the separator as a String.
     *
     */
    public String getCharacters() {

        return String.format("%s", this.characters);

    }
    /**
     *
     * Returns the separator as a string.
     *
     * @return The separator as a String.
     *
     */
    public String getSeparator() {

        return String.format("%s", this.separator);

    }
    /**
     *
     * Returns the length of the separator.
     *
     * @return The length of the separator as an Integer.
     *
     */
    public Integer getLength() {

        return this.length;

    }

    @Override
    public String toString() {

        return this.getSeparator();

    }

}
