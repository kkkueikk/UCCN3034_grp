import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 2020)) {
            System.out.println("Connected to the server.");

            // Initialize input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);
            boolean gameOver = false;

            for (int i = 0; i < 5; i++) {
                System.out.print("Enter your guess (1-100): ");
                int guess = scanner.nextInt();
                out.println(guess);

                String response = in.readLine();
                System.out.println(response);

                if (response.equals("Success")) {
                    gameOver = true;
                    break;
                }
            }

            // Check for Fail after loop
            if (!gameOver) {
                String response = in.readLine();
                if (response.equals("Fail")) {
                    System.out.println("You have used all your attempts. Game over.");
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

