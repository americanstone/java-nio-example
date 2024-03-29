package e1;
import java.net.*;
import java.io.*;

/*

https://www.cs.uic.edu/~troy/spring05/cs450/sockets/

This client/server pair runs a simple TCP socket program as an Echo Server that only allows one client to connect to the server.

 */
public class EchoServer {
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
            System.out.println ("Waiting for connection.....");

            try {
                clientSocket = serverSocket.accept();
                System.out.println("client port:"  + clientSocket.getPort());
            }
            catch (IOException e)
            {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            System.out.println ("Connection successful");
            System.out.println ("Waiting for input.....");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                    true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader( clientSocket.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null)
            {
                System.out.println ("Server: " + inputLine);
                out.println(inputLine);

                if (inputLine.equals("Bye."))
                    break;
            }

            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        }
    }
