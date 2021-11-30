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


public class JavaSecureChannel {

    private static int port = 0;

    private JavaSecureChannel(int port) {
        this.port = port;
    }
    public static void main(String[] args) {
        try {
            initChannel();
        } catch (Exception e) {
            System.out.println("Welp");
            e.printStackTrace();
        }
    }

    /* Look at the below library to transform the channel into a secure channel
     * https://docs.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html*/

    /* Function to Read the data in the pipeline into the cache using socketChannel's read method */

    public static void initChannel() throws IOException {
        // Server side, create Server Socket Channel through open method
        // Note that at this point, the server side has not yet bound the port.

        System.out.println("About to Init Channel");

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // Binding port number
        serverSocketChannel.bind(new InetSocketAddress(8081));


        // Create a byte buffer The size of the buffer is 1024 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (true) {
            System.out.println("-------Server side-----Start receiving-----Client Connection---------");
            // On the server side, receive a link from the client and return a link if there is a client.

            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                while (true) {
                    // Clear cache data, new data can be received
                    byteBuffer.clear();

                    // Read the data from the pipe socketChannel into the cache byteBuffer
                    // ReadSize denotes the number of bytes read
                    int readSize = socketChannel.read(byteBuffer);
                    if (readSize == -1) {
                        break;
                    }

                    // Note that the byte type used . Therefore, we need to convert
                    String message = new String(byteBuffer.array());
//                    System.out.println(new String(byteBuffer.array()));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void connectServer() throws IOException{
        // Create a SocketChannel object,
        // Please note that there is no link to the server side.
        SocketChannel socketChannel = SocketChannel.open();

        //Start linking the server side
        socketChannel.connect(new InetSocketAddress("localhost", port));

        //Create a byte buffer on the client side
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        String msg = ""; // TODO Get the message

        //To the byte buffer, add data
        byteBuffer.put(msg.getBytes());
        // For updating the limit value, the value is updated to position for subsequent reads
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            //Write the data in the byte cache into the pipeline
            socketChannel.write(byteBuffer);
        }

        socketChannel.close();
    }
    
    public static void writeFileToSocket (String fileName) {
        int sockPort = 400;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        ServerSocket servsock = null;
        Socket sock = null;
        try {
            servsock = new ServerSocket(sockPort);
            while (true) {
                System.out.println("Waiting...");
                try {
                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);
                    // send file
                    File myFile = new File(fileName);
                    byte[] mybytearray = new byte[(int) myFile.length()];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    bis.read(mybytearray, 0, mybytearray.length);
                    os = sock.getOutputStream();
                    System.out.println("Sending " + fileName + "(" + mybytearray.length + " bytes)");
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();
                    System.out.println("Done.");
                } catch (IOException ex) {
                    System.out.println(ex.getMessage() + ": An Inbound Connection Was Not Resolved");
                }
                finally{
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (sock != null) sock.close();
                }
            }

        }
        catch (IOException ex){

        }
        finally {
            if (servsock != null)
                try {
                    servsock.close();
                }
            catch (Exception e){
                System.out.println("Nothing");
            }
        }
    }

}
