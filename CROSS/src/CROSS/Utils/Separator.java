package CROSS.Utils;

/**
 * This class is used to create a separator string.
 * It is used to format the output of the program and make it more readable.
 * @version 1.0
 */
public class Separator {

    private final static Integer DEFAULT_LENGTH = 42;

    // Will contain the final created separator.
    private String separator;

    // A separator could be a sequence of characters, for that is String.
    private String characters;
    
    /**
     * Constructor with two parameters.
     * 
     * @param characters The sequence of characters to be repeated.
     * @param length The number of times the sequence of characters is repeated.
     * 
     * @throws NullPointerException If character or length are null.
     * @throws IllegalArgumentException If length is less than or equal to 0.
     */
    public Separator(String characters, Integer length) throws NullPointerException, IllegalArgumentException {
        // Null checks.
        if (characters == null) {
            throw new NullPointerException("Character cannot be null.");
        }
        if (length == null) {
            throw new NullPointerException("Length cannot be null.");
        }

        // Length check.
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0.");
        }

        // Create the separator.
        separator = "";
        for (int i = 0; i < length; i++) {
            separator += characters;
        }

        this.characters = characters;
    }
    /**
     * Constructor with one parameter. It uses the default length.
     * 
     * @param characters The characters to be repeated.
     */
    public Separator(String characters) {
        this(characters, DEFAULT_LENGTH);
    }

    /**
     * Returns the default length of the separator.
     * @return The default length of the separator.
     */
    public static Integer getDefaultLength() {
        return Integer.valueOf(DEFAULT_LENGTH);
    }
    /**
     * Returns the characters of the separator.
     * @return The characters of the separator.
     */
    public String getCharacters() {
        return String.format("%s", this.characters);
    }
    /**
     * Returns the separator.
     * @return The separator.
     */
    public String getSeparator() {
        return String.format("%s", this.separator);
    }

    @Override
    public String toString() {
        return this.getSeparator();
    }

}
