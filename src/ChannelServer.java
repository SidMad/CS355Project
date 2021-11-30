import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class ChannelServer {

    private static int uid = 0;
    private int port;

    private ChannelServer(int port){
        this.port = port;
    }

    private void start(){
        ServerSocketChannel serverSocketChannel = null;
        SocketChannel socketChannel = null;
        try {
            // Server side, create Server Socket Channel
            serverSocketChannel = ServerSocketChannel.open();
            // Binding port number
            serverSocketChannel.bind(new InetSocketAddress(port));
        }
        catch(Exception e){
            System.out.println("Failed to create socket");
            e.printStackTrace();
        }
        // On the server side, receive a link from the client
        try {
            socketChannel = serverSocketChannel.accept();
        } catch (Exception e) {
            System.out.println("Failed to connect");
            e.printStackTrace();
        }
        if (socketChannel != null) {
            // TODO
        }

    }
}
