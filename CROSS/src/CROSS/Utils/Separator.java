package CROSS.Utils;

/**
 * This class is used to create a separator string.
 * It is used to format the output of the program and make it more readable.
 * @version 1.0
 */
public class Separator {

    private final static Integer DEFAULT_LENGTH = 42;
    private String separator;
    private String character;
    private Integer length;
    
    /**
     * Constructor with two parameters.
     * @param character The character to be repeated.
     * @param length The number of times the character is repeated.
     * @throws NullPointerException If character or length are null.
     * @throws IllegalArgumentException If length is less than or equal to 0.
     */
    public Separator(String character, Integer length) throws NullPointerException, IllegalArgumentException {
        if (character == null) {
            throw new NullPointerException("Character must not be null.");
        }
        if (length == null) {
            throw new NullPointerException("Length must not be null.");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0.");
        }
        separator = "";
        for (int i = 0; i < length; i++) {
            separator += character;
        }
        separator += "\n";
        this.character = character;
        this.length = length;
    }
    /**
     * Constructor with one parameter.
     * @param character The character to be repeated.
     * @throws NullPointerException If character is null.
     */
    public Separator(String character) throws NullPointerException {
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
        return character;
    }
    /**
     * Returns the length of the separator.
     * @return The length of the separator.
     */
    public Integer getLength() {
        return length;
    }
    /**
     * Returns the separator.
     * @return The separator.
     */
    public String getSeparator() {
        return separator;
    }

    @Override
    public String toString() {
        return this.getSeparator();
    }

}
