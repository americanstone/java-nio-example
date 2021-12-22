package e4;

import java.net.*;
import java.io.IOException;

/*
This program scans the UDP ports on the local machine to see if they are being used
 */
public class UDPPortScanner {

    public static void main(String[] args) {

        // first test to see whether or not we can bind to ports
        // below 1024
        boolean rootaccess = false;
        for (int port = 1; port < 1024; port += 50) {
            try {
                ServerSocket ss = new ServerSocket(port);
                // if successful
                rootaccess = true;
                ss.close();
                break;
            }
            catch (IOException ex) {
            }
        }

        int startport = 1;
        if (!rootaccess) startport = 1024;
        int stopport = 65535;

        System.out.println ("Checking UDP ports " + startport +
                " to " + stopport );

        for (int port = startport; port <= stopport; port++) {
            try {
                DatagramSocket ds = new DatagramSocket(port);
                ds.close();
            }
            catch (IOException ex) {
                System.out.println("UDP Port " + port + " is occupied.");
            }
        }
    }
}
