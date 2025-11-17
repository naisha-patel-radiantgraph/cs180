package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 4242;

    public static void main(String[] args) {

        try {

            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connected to server.");


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            Thread listener = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverMsg;
                        while ((serverMsg = in.readLine()) != null) {
                            System.out.println("SERVER: " + serverMsg);
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected from server.");
                    }
                }
            });

            listener.start();

            while (true) {
                String input = scanner.nextLine();
                out.println(input);

                if (input.equalsIgnoreCase("DISCONNECT")) {
                    System.out.println("Closing client...");
                    socket.close();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Could not connect to server.");
        }
    }
}
