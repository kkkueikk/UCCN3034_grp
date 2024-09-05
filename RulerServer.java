import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;

public class RulerServer {

    private static final int UNICAST_SERVER_PORT = 2014; // Example port for Group ID 20
    private static final int MULTICAST_SERVER_PORT = 3014;
    private static final int BROADCAST_SERVER_PORT = 4014;
    private ServerSocket serverSocket;
    
    public RulerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
    }
    // main will create serverSocket and listen for connection, direct the request
    // to clientHandler (how to exit? error?)
    public void start() {
        while (true) {
            try {
                // Wait for a client connection
                Socket clientSocket = serverSocket.accept();
                // Handle each client connection in a new thread
                new ClientHandler(clientSocket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        try {
            RulerServer server = new RulerServer(UNICAST_SERVER_PORT);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }
    //I need a function that is a thread, keep listening on the input from keyboard if ctrl c then break the loop, or 'q'
    //I need a function that will break the loop after timeout
    // encrypt, hash communication between client and server
    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
        }

        // while having communication, decrypt data receive from user, encrypt response
        // to user, close when receiving 7 from client
        public void run() { 
            try {
                String option;
                while ((option = receive()) != null) {
                    if (option.equals("7")) {
                        break;
                    }

                    String response = handleOption(option);
                    send(response);
                }
            } catch (IOException e) {
                System.out.println("Server handler IOException: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Server handler exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (in != null) in.close();
                    if (out != null) out.close();
                } catch (IOException e) {
                    System.out.println("Error closing resources: " + e.getMessage());
                    e.printStackTrace();
                }
            }
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

        private String receive() throws IOException{
            
            try {
                String encryptedResponse = in.readLine(); // Read encrypted response from client
                String responseHash = in.readLine(); // Read hash from client
                String decryptedResponse = decrypt(encryptedResponse);
                System.out.println("decrypted message: " + decryptedResponse);
                System.out.println("encrypted message: " + encryptedResponse);
                System.out.println("response message: " + responseHash);
                if (verifyHash(decryptedResponse, responseHash)) { // Check the received hash
                    // decryptedResponse = decrypt(encryptedResponse); // Decrypt the response
                    // System.out.println("Client message: " + decryptedResponse);
                    return decryptedResponse;

                } else {
                    System.out.println("Error: Message integrity check failed.");
                    return "0";
                }

            } catch (IOException e) {
                throw e;
            }
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

        private String handleOption(String option) throws Exception {
            int optionNumber;

            try {// string to integer if not integer will "Invalid option"
                
                optionNumber = Integer.parseInt(option);
            } catch (NumberFormatException e) {
                return "Invalid option";
            }
            // System.out.println(optionNumber);
            String message;
            // try {
            System.out.println(option);
            switch (option) {
                case "1":
                    return "Server time (GMT+8): " + getCurrentTimeGMT8();
                case "2":
                    // System.out.println(optionNumber+"222222222");
                    return "Your port number is: " + socket.getPort();
                // Placeholder for other options; they would require additional implementation
                case "3":
                    message = receive();
                    return broadcast(message) ? "Broadcast successful" : "Broadcast failed";
                case "4":
                    message = receive();
                    return multicast(message) ? "Multicast successful" : "Multicast failed";
                case "6":
                    int result = playGame();
                    if(result == 1){
                        System.out.println("Champagne!");
                        return "What can I say? Congrats... ";
                    }else if(result == -1){
                        System.out.println("gugu");
                        return "You know... failing in this kind of game... Divide and Conquer";
                    }
                default:
                    // System.out.println("Invalid option is received ");
                    return "Invalid option";
            }
            // } catch (Exception e) {
            // throw e;
            // }

        }

        private String getCurrentTimeGMT8() {
            // Set the time zone to GMT+8
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return sdf.format(new Date());
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
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(message.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }

        private int playGame() throws IOException, NoSuchAlgorithmException {
            int max_attempts = 5;
            Random random = new Random();
            int targetNumber = random.nextInt(100) + 1;
            int attempts = 0;
            String received;

            while (attempts < max_attempts) {
                attempts++;
                received = receive();
                try {
                    int guess = Integer.parseInt(received);

                    if (guess < targetNumber) {
                        send("Try again - Low");
                    } else if (guess > targetNumber) {
                        send("Try again - High");
                    } else {
                        return 1;
                    }
                } catch (NumberFormatException e) {
                    send("Invalid number received, try again. ^o^\n Attemps: "+attempts);
                }
            }
            return -1;
        }

        // private static void unicast(String args) {
        //     try (ServerSocket serverSocket = new ServerSocket(UNICAST_SERVER_PORT)) {
        //         System.out.println("Server is listening on port " + UNICAST_SERVER_PORT);

        //         while (true) {
        //             Socket socket = serverSocket.accept();
        //             System.out.println("New client connected");

        //             new ClientHandler(socket).start();
        //         }

        //     } catch (IOException e) {
        //         System.out.println("Server exception: " + e.getMessage());
        //         e.printStackTrace();
        //     }
        // }

        private static boolean multicast(String message) throws UnknownHostException, SocketException, IOException {
            // create the socket with multicast port 3014
            DatagramSocket multicastSocket = new DatagramSocket();

            InetAddress multicastGroup = InetAddress.getByName("224.0.0.14");
            byte[] buf = message.getBytes();

            DatagramPacket multicastPacket = new DatagramPacket(buf, buf.length, multicastGroup, MULTICAST_SERVER_PORT);

            // send the packet using socket and close
            multicastSocket.send(multicastPacket);
            multicastSocket.close();
            return true;
        }

        private static boolean broadcast(String message) throws UnknownHostException, SocketException, IOException {
            DatagramSocket broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
            byte[] buf = message.getBytes();

            DatagramPacket broadcastPacket = new DatagramPacket(buf, buf.length, broadcastAddr, BROADCAST_SERVER_PORT);

            // send the packet using socket and close
            broadcastSocket.send(broadcastPacket);
            broadcastSocket.close();
            return true;
        }
    }
}
