package CROSS.Utils;

public abstract class Separator {

    private final static Integer DEFAULT_LENGTH = 42;
    
    // Just a utility method to create a separator string.
    // To format a little bit the output.
    public static String getSeparator(String character, Integer length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be greater than 0.");
        }
        String separator = "";
        for (int i = 0; i < length; i++) {
            separator += character;
        }
        separator += "\n";
        return separator;
    }
    public static String getSeparator(String character) {
        return getSeparator(character, DEFAULT_LENGTH);
    }

    public static Integer getDefaultLength() {
        return DEFAULT_LENGTH;
    }

}
