The idea is that all these APIs classes convert the Java objects created for the project to the JSON format described in the assignment's text.

In this way I can use arbitrary Java objects and decide what expose and in which format through the APIs.
So the addition of these classes was done intentionally, to have more control, we could have more simply converted the main objects directly to JSON with Gson, but we would have lost some control.

So these classes are interfaces between project's Java objects to create the JSON strings to be used for requests / responses / notifications by using the Google Gson lib.

The requests / responses are sent / received through a TCP socket.

The requests / responses are exchanged between server and client.

The main objects to use to create requests / responses are:
    - Request.java
    - Response.java
    - Notification.java
All others classes are used to support to these 3 above.

All the APIs objects getters, when possible (so when the data during the conversion from the project's Java object to the API one are not lost), wrap back the data in a new project Java object before returning them.
