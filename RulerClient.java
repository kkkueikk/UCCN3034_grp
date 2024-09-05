import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class RulerClient {
    
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2014; // Example for Group ID 20
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
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
            do {
                displayMenu();
                menuOption = scanner.nextLine(); // Encrypt user input
                // send(menuOption); // Send encrypted option to server
                System.out.println("You enter: "+menuOption);
                // int optionNumber;
                // response = receive();
                // string to integer if not integer will "Invalid option"
                // optionNumber = Integer.parseInt(menuOption);
                // if(optionNumber >=1 && optionNumber<1)
                String message;
                switch (menuOption) {
                    case "1":
                    case "2":
                        // For options "1" and "2", just send the option and receive a response
                        send(menuOption); // Send the selected menu option to the server
                        response = receive(); // Receive the response from the server
                        System.out.println("Server Response: " + response);
                        break;
                    case "3":
                    case "4":
                        // For options "3" and "4", send the option, then send the message
                        send(menuOption); // Send the selected menu option to the server
                        System.out.println("Enter the message:");
                        message = scanner.nextLine(); // Get the message input from the user
                        send(message); // Send the message to the server
                        response = receive(); // Receive the response from the server
                        System.out.println("Server Response: " + response);
                        break;
                    case "5":
                    case "6":
                        send(menuOption); // Send the "6" option to start the game on the server
                        System.out.println("Guess the number between 1 and 100: ");

                        boolean gameOver = false;
                        while (!gameOver) {
                            String guess = scanner.nextLine(); // Get the user's guess
                            send(guess); // Send the guess to the server

                            response = receive(); // Receive the server's response
                            System.out.println("Server Response: " + response);

                            // Check if the game is over
                            if (response.contains("Congrats") || response.contains("failing")) {
                                gameOver = true;
                            }
                        }
                        break;
                    case "7":
                        send(menuOption);
                        break;
                    default:
                        System.out.println("Invalid response");
                        break;
                }
                // if (response == "Invalid option") { // Check the received hash
                //     System.out.println(response);
                // }
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
        System.out.println("Welcome to the Ruler Program");
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
        byte[] hash = digest.digest(message.getBytes()); // computes the hash of the input string
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
            String localHash = generateHash(decryptedMessage);
            return localHash.equals(receivedHash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Hash verification error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String receive() throws IOException {

        try {
            String encryptedResponse = in.readLine(); // Read encrypted response from client
            String responseHash = in.readLine(); // Read hash from client
            String decryptedResponse = decrypt(encryptedResponse);
            
            // System.out.println("decrypted message: " + decryptedResponse);
            // System.out.println("encrypted message: " + encryptedResponse);
            // System.out.println("response message: " + responseHash);

            if (verifyHash(decryptedResponse, responseHash)) { // Check the received hash
                System.out.println("Server message: " + decryptedResponse);
            } else {
                System.out.println("Error: Message integrity check failed.");
            }
            return decryptedResponse;
        } catch (IOException e) {
            throw e;
        }
    }

    private int send(String message) {
        try {
            String encryptedString = encrypt(message);
            String hashedString = generateHash(message);
            // System.out.println("decrypted message: " + decryptedResponse);
            System.out.println("encrypted message: " + encryptedString);
            System.out.println("response message: " + hashedString);
            out.println(encryptedString);
            out.println(hashedString);
            out.flush();
            return 1;
        } catch (Exception e) {
            return -1;
        }

    }
}
