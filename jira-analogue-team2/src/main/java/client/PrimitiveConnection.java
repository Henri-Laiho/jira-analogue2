package messages;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PrimitiveConnection {
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public PrimitiveConnection(DataOutputStream outputStream, DataInputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    private Object handleMessage(@NotNull MessageType type, byte[] data) throws IOException {
        System.out.print("MessageType ||| ");
        System.out.println(type);
        System.out.print("Message     ||| ");
        System.out.println(new String(data, "UTF-8"));
        return null;
    }

    public void sendMessage(@NotNull byte[] message, @NotNull MessageType type) throws IOException {
        // Send the type of the message
        outputStream.writeInt(type.getAsInt());

        // Send the length of the message
        outputStream.writeInt(message.length);

        String s = new String(message, StandardCharsets.UTF_8);

        // Send the data of the message
        outputStream.write(message);
    }


    public MessageType readMessage() throws IOException {
        // Get the type of message
        MessageType messageType = MessageType.getMessageType(inputStream.readInt());

        // Get the length of the message
        int messageLen = inputStream.readInt();

        // Get the data
        byte[] data = new byte[messageLen];
        inputStream.readFully(data);
        handleMessage(messageType, data);
        return messageType;
    }


}
