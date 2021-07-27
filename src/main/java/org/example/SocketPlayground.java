package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class SocketPlayground {
    public static void main(String[] args) {
        String server = "www.javaworld.com";
        String path = "/";

        System.out.println( "Loading contents of URL: " + server );

        try(
                Socket socket = new Socket(server, 80);
                PrintStream out = new PrintStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        )
        {
            out.println( "GET " + path + " HTTP/1.0" );
            out.println();

            Thread.sleep(3000L);
            System.out.println("Test");

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
