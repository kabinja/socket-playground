package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try(
                Socket socket = new Socket("localhost", 8085);
                PrintStream out = new PrintStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            out.println("Normally, a server runs on a specific computer and has a socket that is bound to a specific port number.");
            out.println("The server just waits, listening to the socket for a client to make a connection request.");
            out.println("The client knows the hostname of the machine on which the server is running and the port number on which the server is listening. ");

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
}
