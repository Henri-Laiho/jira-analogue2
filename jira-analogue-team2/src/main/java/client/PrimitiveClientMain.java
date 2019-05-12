package client;

import messages.MessageType;
import messages.PrimitiveConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * For testing server without TUI.
 */
public class PrimitiveClientMain {

    /*public static void main(String[] args) {
        System.out.println("primitive client.");
        Scanner stdin = new Scanner(System.in);

        while (true) {

            Socket socket;
            DataOutputStream output;
            DataInputStream inputStream;

            try {
                socket = new Socket("localhost", 28015);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            try {
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            try {
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            var b = new PrimitiveConnection(output, inputStream);

            try {
                while (socket.isConnected()) {
                    try {
                        send(stdin, b);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        continue;
                    }
                    b.readMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Socket disconnected. Press enter to reconnect.");
            stdin.nextLine();


        }

    }
    */

    static void send(Scanner stdin, PrimitiveConnection b) throws IOException {
        System.out.print("MessageType >>> ");
        MessageType m = MessageType.valueOf(stdin.nextLine());
        System.out.print("Message     >>> ");
        b.sendMessage(stdin.nextLine().getBytes(StandardCharsets.UTF_8), m);
    }

}
