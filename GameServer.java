import java.io.*;
import java.net.*;
import java.util.Random;

public class GameServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(2020)) {
            System.out.println("Server is listening on port 2020...");
            Socket socket = serverSocket.accept();
            System.out.println("Client connected.");

            // Initialize input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Generate a random number
            Random random = new Random();
            int targetNumber = random.nextInt(100) + 1;
            int attempts = 0;

            while (attempts < 5) {
                String received = in.readLine();
                int guess = Integer.parseInt(received);
                attempts++;

                if (guess < targetNumber) {
                    out.println("Try again – Low");
                } else if (guess > targetNumber) {
                    out.println("Try again – High");
                } else {
                    out.println("Success");
                    break;
                }
            }

            if (attempts == 5) {
                out.println("Fail");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
