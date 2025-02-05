package CROSS.API2;

public class Request extends JSON {
    
    private String operation = null;
    private Object values = null;

    public Request(String operation, Object values) throws NullPointerException {

        // Null check.
        if (operation == null)
            throw new NullPointerException("Operation cannot be null.");
        if (values == null)
            throw new NullPointerException("Values cannot be null.");

        this.operation = operation;
        this.values = values;
        
    }


}
