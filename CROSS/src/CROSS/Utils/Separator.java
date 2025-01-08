package CROSS.Utils;

/**
 * This class is used to create a separator string.
 * It is used to format the output of the program and make it more readable.
 * @version 1.0
 */
public class Separator {

    private final static Integer DEFAULT_LENGTH = 42;

    // Will contain the final built separator.
    private String separator;

    // A separator could be a sequence of characters.
    private String character;
    private Integer length;
    
    /**
     * Constructor with two parameters.
     * 
     * @param character The sequence of characters to be repeated.
     * @param length The number of times the sequence of characters is repeated.
     * 
     * @throws NullPointerException If character or length are null.
     * @throws IllegalArgumentException If length is less than or equal to 0.
     */
    public Separator(String character, Integer length) throws NullPointerException, IllegalArgumentException {
        if (character == null) {
            throw new NullPointerException("Character cannot be null.");
        }
        if (length == null) {
            throw new NullPointerException("Length cannot be null.");
        }

        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0.");
        }

        separator = "";
        for (int i = 0; i < length; i++) {
            separator += character;
        }

        this.character = character;
        this.length = length;
    }
    /**
     * Constructor with one parameter. It uses the default length.
     * 
     * @param character The character to be repeated.
     */
    public Separator(String character) {
        this(character, DEFAULT_LENGTH);
    }

    /**
     * Returns the default length of the separator.
     * @return The default length of the separator.
     */
    public static Integer getDefaultLength() {
        return DEFAULT_LENGTH;
    }
    /**
     * Returns the character of the separator.
     * @return The character of the separator.
     */
    public String getCharacter() {
        return String.format("%s", this.character);
    }
    /**
     * Returns the length of the separator.
     * @return The length of the separator.
     */
    public Integer getLength() {
        return Integer.valueOf(this.length);
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
