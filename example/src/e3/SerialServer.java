package e3;

import java.net.*;
import java.io.*;
/*
This example shows how to send an object across a TCP socket by serializing a class.
 */
public class SerialServer
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(10007);
        }
        catch (IOException e)
        {
            System.err.println("Could not listen on port: 10007.");
            System.exit(1);
        }

        Socket clientSocket = null;

        try {
            System.out.println ("Waiting for Client");
            clientSocket = serverSocket.accept();
        }
        catch (IOException e)
        {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        ObjectOutputStream out = new ObjectOutputStream(
                clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(
                clientSocket.getInputStream());

        Point3d pt3 = null;
        Point3d pt4 = null;

        try {
            pt3 = (Point3d) in.readObject();
        }
        catch (Exception ex)
        {
            System.out.println (ex.getMessage());
        }


        System.out.println ("Server recieved point: " + pt3 + " from Client");

        pt4 = new Point3d (-24, -23, -22);
        System.out.println ("Server sending point: " + pt4 + " to Client");
        out.writeObject(pt4);
        out.flush();


        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }
}