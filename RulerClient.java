import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.InputMismatchException;


public class RulerClient {
    
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2014; // Example for Group ID 20
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket socket;
    public static void main(String[] args) {
        // Add shutdown hook to handle Ctrl + C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down... Cleaning up resources.");
            try {
                
                if (socket != null && !socket.isClosed()) {
                    System.out.println("Closing socket...");
                    socket.close(); // Close socket on shutdown
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Goodbye!");
        }));
        RulerClient client = new RulerClient();
        client.run();
    }

    public void run() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)) {

            this.out = out; // Assign local out to the instance variable
            this.in = in; // Assign local in to the instance variable

            String menuOption;
            String response;
            System.out.println("Welcome to the Ruler Program");

            do {
                displayMenu();
                menuOption = scanner.nextLine(); // Encrypt user input
                // send(menuOption); // Send encrypted option to server
                System.out.println("You enter: "+menuOption);

                String message;
                switch (menuOption) {
                    case "1":
                    case "2":
                        // For options "1" and "2", just send the option and receive a response
                        send(menuOption); // Send the selected menu option to the server
                        System.out.println(receive()); // Receive the response from the server
                        break;
                    case "3":
                    case "4":
                        // For options "3" and "4", send the option, then send the message
                        send(menuOption); // Send the selected menu option to the server
                        System.out.println("Enter the message:");
                        message = scanner.nextLine(); // Get the message input from the user
                        send(message); // Send the message to the server
                        System.out.println(receive()); // Receive the response from the server
                        break;
                    case "5":
                        send(menuOption);
                        System.out.println(receive());
                        String operation = scanner.nextLine();
                        send(operation); // send operation to server

                        if ("1".equals(operation)) {
                            int num1 = getIntFromUser("Enter first number:");
                            send(String.valueOf(num1)); 
                            int num2 = getIntFromUser("Enter second number:");
                            send(String.valueOf(num2)); 

                        } else if ("2".equals(operation)) {
                            System.out.println(receive());
                            String password = scanner.nextLine();
                            send(password); // send password to server
                        }
                        System.out.println(receive());
                        break;
                    case "6":
                        send(menuOption); // Send the "6" option to start the game on the server
                        System.out.println(receive());

                        boolean gameOver = false;
                        do {
                            int guess = getIntFromUser("Number?"); // Get the user's guess
                            send(String.valueOf(guess)); // Send the guess to the server

                            response = receive(); // Receive the server's response
                            System.out.println(response);
                            // Check if the game is over
                            if (response.contains("Congrats") || response.contains("failing")) {
                                gameOver = true;
                            }
                        }while (!gameOver);
                        break;
                    case "7":
                        send(menuOption);
                        break;
                    default:
                        System.out.println("Invalid response");
                        break;
                }
            } while (!menuOption.equals("7")); // Exit when option 7 is selected

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e){
            System.out.println("Error converting string to number: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void displayMenu() {
        System.out.println("1. Print out server time (GMT+8)");
        System.out.println("2. Retrieve the Ruler's port number");
        System.out.println("3. War Alert Channel");
        System.out.println("4. Festival Channel");
        System.out.println("5. Perform RMI");
        System.out.println("6. Play a game");
        System.out.println("7. Exit the system");
        System.out.print("Select an option: ");
    }

    private static String encrypt(String message) {
        // Caesar Cipher with shift 3
        StringBuilder encrypted = new StringBuilder();
        for (char c : message.toCharArray()) {
            encrypted.append((char) (c + 3));
        }
        return encrypted.toString();
    }

    private static String decrypt(String message) {
        // Caesar Cipher with shift -3
        StringBuilder decrypted = new StringBuilder();
        for (char c : message.toCharArray()) {
            decrypted.append((char) (c - 3));
        }
        return decrypted.toString();
    }

    private static String generateHash(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");    //creates a MessageDigest instance for the SHA-256 algorithm
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8)); // computes the hash of the input string
        StringBuilder hexString = new StringBuilder();  //terates over each byte in the hash byte array
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        
        return hexString.toString();
    }

    private static boolean verifyHash(String decryptedMessage, String receivedHash) {
        try {
            String localHash = generateHash(decryptedMessage.trim());
            return localHash.equals(receivedHash); 
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Hash verification error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String receive() {

        try {
            String encryptedResponse = in.readLine(); // Read encrypted response from client
            String responseHash = in.readLine(); // Read hash from client
            String decryptedResponse = decrypt(encryptedResponse);

            if (verifyHash(decryptedResponse, responseHash)) { // Check the received hash
                return "Server reply: " + decryptedResponse;
            } else {
                return "Error detected! Message integrity check failed.";
            }
        } catch (Exception e) {
            return "Error in receiving server message.";
        }
    }

    private int getIntFromUser(String prompt) {
        Scanner scanner = new Scanner(System.in);
        int result = 0;
        boolean valid = false;
        
        while (!valid) {
            System.out.println(prompt);
            try {
                result = scanner.nextInt();
                valid = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.next(); // Clear the invalid input
            }
        }
        return result;
    }

    private int send(String message) {
        try {
            String encryptedString = encrypt(message);
            String hashedString = generateHash(message);

            out.println(encryptedString);
            out.println(hashedString);
            out.flush();
            return 1;
        } catch (Exception e) {
            return -1;
        }

    }
}
