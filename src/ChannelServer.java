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
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
