package mysecondproject_test;

import java.io.*;
import java.net.*;

public class SimpleClient {

    private static final String SERVER_ADDRESS = "localhost"; // Server address
    private static final int SERVER_PORT = 1234; // Server port

    public static void main(String[] args) {

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;
            System.out.println("Connected to server. Type messages to send:");

            while ((userInput = stdIn.readLine()) != null) {

                out.println(userInput); // Send message to the server
                System.out.println("Server reply: " + in.readLine()); // Read response from the server

            }
        } 
        catch (IOException e) {

            e.printStackTrace();

        }

    }
}

