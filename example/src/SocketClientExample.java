import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketClientExample {
 
    public void startClient() throws IOException, InterruptedException {
        InetSocketAddress hostAddress = new InetSocketAddress( 8090);
        SocketChannel client = SocketChannel.open(hostAddress);
 
        System.out.println("Client... started");
        
        String threadName = Thread.currentThread().getName();
 
        // Send messages to server
        String [] messages = new String []{threadName + ": client msg test1", threadName + ":client msg test2", threadName + ": client msg test3"};

        for (String s : messages) {
            byte[] message = s.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            client.write(buffer);
            System.out.println(s);
            buffer.clear();
            Thread.sleep(5000);
        }
        client.close();            
    }
}

