The idea is that all these classes convert the Java objects to the JSON format described in the assignment's text. 

In this way I can use arbitrary Java objects and decide what exponse and in which format through the API.
So the addition of these classes was done intentionally, to have more control, we could have more simply converted the main objects directly to JSON, but we would have lost control.

So these classes are interfaces between Java objects to create the JSON strings to be used for requests / responses by using the Google gson lib.

The requests / responses are sent / received through a socket.

The main objects to use to create requests / responses are:
    - Request.java
    - Response.java
    - Notification.java
All others classes are used to support to these 3 above.

All the getters, when possible (so when the data during the conversion from the Java object to the API one are not lost) wrap back the data in a new Java object returned.

