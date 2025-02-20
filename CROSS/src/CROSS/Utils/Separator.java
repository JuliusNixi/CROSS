package CROSS.Utils;

/**
 * 
 * This class is used to create a very simple separator string.
 * It is used to format the output of the program and make it more readable (useful for debugging).
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public class Separator {

    private final static Integer DEFAULT_LENGTH = 42;

    // Will contain the final created separator.
    private final String separator;

    // A separator could also be a sequence of characters, for that is a String.
    private final String characters;
    
    // CONSTRUCTORS
    /**
     * 
     * Constructor with two parameters.
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

        this(characters, DEFAULT_LENGTH);

    }

    // GETTERS
    /**
     * 
     * Returns the default length of the separator.
     * 
     * @return The default length of the separator as an integer.
     * 
     */
    public static Integer getDefaultLength() {

        return Integer.valueOf(DEFAULT_LENGTH);

    }
    /**
     * 
     * Returns the characters of the separator.
     * 
     * @return The characters of the separator as a string.
     * 
     */
    public String getCharacters() {

        return String.format("%s", this.characters);

    }
    /**
     * 
     * Returns the separator as a string.
     * 
     * @return The separator as a string.
     * 
     */
    public String getSeparator() {

        return String.format("%s", this.separator);

    }

    @Override
    public String toString() {
        return this.getSeparator();
    }

}
