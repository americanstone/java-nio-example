import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SocketServerExample {
	private Selector selector;
    private Map<SocketChannel,List<byte[]>> dataMapper;
    private InetSocketAddress listenAddress;
    
    public static void main(String[] args) throws Exception {
    	Runnable server = () -> {
             try {
                new SocketServerExample("127.0.0.1", 8090).startServer();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };
		
		Runnable client = () -> {
             try {
                 new SocketClientExample().startClient();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        };
       new Thread(server, Thread.currentThread().getName()).start();
       new Thread(client, "client-A").start();
       new Thread(client, "client-B").start();
    }

    public SocketServerExample(String address, int port) throws IOException {
    	listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<SocketChannel,List<byte[]>>();
    }

    // create server channel	
    private void startServer() throws IOException, InterruptedException {
        System.out.println("Server thread: " + Thread.currentThread().getName() + " Server started...");

        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        //associates the selector to the socket channel.
        //we use OP_ACCEPT, which means the selector merely reports that a client attempts a connection to the server.
        // Other possible options are: OP_CONNECT, which will be used by the client; OP_READ; and OP_WRITE.
        /*
            Acceptable: the associated client requests a connection.
            Connectable: the server accepted the connection.
            Readable: the server can read.
            Writeable: the server can write.
         */
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        System.out.println("server interest OP_ACCEPT");


        // no cpu burn
        while (true) {
            // wait for events
            System.out.println("server blocking/wait on client connect event");
            this.selector.select();
            //work on selected keys
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key =  keys.next();

                // this is necessary to prevent the same key from coming up 
                // again the next time around.
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.accept(key);
                } else if (key.isReadable()) {
                    this.read(key);
                }else if(key.isWritable()){
                    //not working
                    this.write(key);
                }
            }
        }
    }

    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        //this is server socker channel
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Server connected to client: " + remoteAddr);

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList<byte[]>());
        System.out.println("Selector interest OP_READ");
        channel.register(this.selector, SelectionKey.OP_READ);
    }
    
    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        //
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            this.dataMapper.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));
    }

    private void write(SelectionKey key) throws IOException, InterruptedException {
        SocketChannel channel = (SocketChannel) key.channel();
        int numRead = -1;
        String [] messages = new String []{ ": server msg test1", ":server msg test2", ": server msg test3"};
        System.out.println("Server send msg to client: " );

        for (String s : messages) {
            byte[] message = s.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            numRead = channel.write(buffer);
            System.out.println("Sent: "  + s);
            buffer.clear();
        }
    }
}