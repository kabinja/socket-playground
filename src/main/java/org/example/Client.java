package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) {
        try(
                Socket socket = new Socket("localhost", 8085);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            sendFrame("localhost:8090", out, "test\n");
            //sendFrame("localhost:8091", out, "Normally, a server runs on a specific computer and has a socket that is bound to a specific port number.");
            //sendFrame("localhost:8092", out, "The server just waits, listening to the socket for a client to make a connection request.");
            //sendFrame("localhost:8093", out, "The client knows the hostname of the machine on which the server is running and the port number on which the server is listening. ");

            String line = in.readLine();
            while( line != null )
            {
                System.out.println( line );
                line = in.readLine();
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    private static void sendFrame(String destination, DataOutputStream out, String message) throws IOException {
        final byte[] destinationBytes = destination.getBytes(StandardCharsets.UTF_8);
        final byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);

        out.writeInt(destinationBytes.length);
        out.write(destinationBytes);
        out.writeInt(payloadBytes.length);
        out.write(payloadBytes);
        out.flush();
    }
}
