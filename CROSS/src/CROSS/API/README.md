The idea is that all these classes convert the Java objects to the JSON format described in the assignment's text. 

In this way I can use arbitrary Java objects and decide what exponse and in which format through the API.

So these classes are interfaces between Java objects to create the JSON strings to be used for requests/responses by using the Google gson lib.

Each response has a code.
Each code has a response type and a response content.
1 response type could have more response content avaible.
