package mysecondproject_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class SimpleServer {

    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static boolean isRunning = true;


    public static void main(String[] args) {

        try {

            // Create a server socket that listens on port 1234
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server is running and waiting for clients...");

            // Start the command listener in a separate method
            CommandListener commandListener = new CommandListener();
            new Thread(commandListener).start();


            while (true) {

                // Wait for the client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected! " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);

                new Thread(clientHandler).start();

                serverSocket.close();

            }


        } 
        catch (IOException e) {

            e.printStackTrace();

        }
    }

    public static void removeClient(ClientHandler clientHandler) {

        clientHandlers.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getClientSocket().getRemoteSocketAddress());

    }

    // Command listener class
    private static class CommandListener implements Runnable {

        @Override
        public void run() {

            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            try {

                String command;
                while (isRunning && (command = consoleInput.readLine()) != null) {
                    if ("shutdown".equalsIgnoreCase(command.trim())) {
                        shutdownServer();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to shutdown the server
    public static void shutdownServer() {

        isRunning = false;
        System.out.println("Shutting down the server...");

        // Close all client connections
        for (ClientHandler clientHandler : clientHandlers) {

            clientHandler.interrupt(); 
            clientHandler.closeConnection();
        }
        System.out.println("All clients disconnected. Server shutting down.");

        System.exit(0);
    }



    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private volatile boolean running = true;
        private Thread thread;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        @Override
        public void run() {

            thread = Thread.currentThread(); // Store the reference to this thread
            try {

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;

                while (running && (message = in.readLine()) != null) {

                    System.out.println("Received: " + message);
                    out.println("Echo: " + message); // Echo the message back to the client
                }

            } 
            catch (IOException e) {

                System.out.println("Client connection lost: " + e.getMessage());

            } 
            finally {

                // Clean up resources
                closeConnection();

                // Notify server that client has disconnected
                removeClient(this);
            }

        }

        // Method to close client connection
        public void closeConnection() {

            running = false; // Stop the thread from running

            try {

                // Send a message to the client before closing
                if (out != null) {
                    out.println("Server is shutting down, disconnecting...");
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();

            } 
            catch (IOException e) {

                e.printStackTrace();

            }
        }

        // Method to interrupt the client handler thread
        public void interrupt() {
            if (thread != null) {
                thread.interrupt(); // Interrupt the thread
            }
        }

    }
    
}
