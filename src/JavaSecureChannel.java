import java.io.*;
import java.net.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.*;

public class JavaSecureChannel {

    private static int port = 8081;
    private static final int MAX_SIZE = 1024;

    private JavaSecureChannel(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify client or server");
            System.exit(0);
        }
        if (args[0].equals("client")) {
            JavaSecureChannel jsc = new JavaSecureChannel(port);
            try {
                jsc.connectServer("Hello Server!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                initChannel();
            } catch (Exception e) {
                System.out.println("Welp");
                e.printStackTrace();
            }
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
        serverSocketChannel.bind(new InetSocketAddress(port));


        // Create a byte buffer The size of the buffer is 1024 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);

        while (true) {
            System.out.println("-------Server side-----Start receiving-----Client Connection---------");
            // On the server side, receive a link from the client and return a link if there is a client.

            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println("we have accepted a client");
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
                    System.out.println(new String(byteBuffer.array()));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // client code
    public void connectServer(String initialMessage) throws IOException{
        // Create a SocketChannel object,
        // Please note that there is no link to the server side.
        SocketChannel socketChannel = SocketChannel.open();

        //Start linking the server side
        socketChannel.connect(new InetSocketAddress("localhost", port));

        //Create a byte buffer on the client side
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);

        String msg = initialMessage; // TODO Get the message

        // Send message across the socket
        sendMessage(msg, socketChannel);

        //To the byte buffer, add data
//        byteBuffer.put(msg.getBytes());
//        // For updating the limit value, the value is updated to position for subsequent reads
//        byteBuffer.flip();
//        while(byteBuffer.hasRemaining()) {
//            //Write the data in the byte cache into the pipeline
//            socketChannel.write(byteBuffer);
//        }

        // Close connection
        socketChannel.close();
    } /* connectServer() */

    /* public static void writeFileToSocket (String fileName) {
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
    } /* writeFileToSocket() */

    public static void sendMessage(String msg, SocketChannel socketChannel) throws IOException{
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);
        byteBuffer.put(msg.getBytes());
        // For updating the limit value, the value is updated to position for subsequent reads
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            //Write the data in the byte cache into the pipeline
            socketChannel.write(byteBuffer);
        }
    } /* sendMessage() */

    public static byte[] computeHash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input);
            MessageDigest tc1 = (MessageDigest) md.clone();
            byte[] toChapter1Digest = tc1.digest();
            return toChapter1Digest;
        }
        catch (Exception e){
            System.out.println("Problem generating digest");
            e.printStackTrace();
            return null;
        }
    } /* computeHash() */

    public static boolean verifyHash(PublicKey publicKey, byte[] message, byte[] signature){
        try {
            Signature verify = Signature.getInstance("SHA512withECDSA");
            verify.initVerify(publicKey);
            verify.update(message);
            boolean isVerified = verify.verify(signature);
            return isVerified;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    } /* verifyHash() - verifies whether signature is authentic given the signature, message hash, and public key */

    public static byte[] computeSignature(PrivateKey privateKey, byte[] message) {
        /* takes as input a private key sk and a message and outputs a signature */
        Signature sign;
        try {
            sign = Signature.getInstance("SHA512withECDSA");
            sign.initSign(privateKey);
            sign.update(message);
            byte[] signArray = sign.sign();
            return signArray;
        }
        catch (Exception e){
            System.out.println("Trouble computing Signature");
            e.printStackTrace();
            return null;
        }

    } /* computeSignature() */

    public static byte[] readFile(String path) throws IOException {

        File file = new File(path);
        if(!FileExists(file)) {
            System.out.println("Path to file incorrect");
            throw new FileNotFoundException();
        }
        // Creating a BufferedReader object to read the file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String content = "", line;

        // Iterate through each line of the file till we reach the last line
        while ((line = br.readLine()) != null) {
            content = content + line;
        }
        return content.getBytes();
    } /* readFile() */

    public static boolean verifyPath(String path) {
        File tempFile = new File(path);
        return tempFile.exists();
    } /* verifyFileExists() */

    public static boolean FileExists(File file) {;
        return file.exists();
    } /* verifyFileExists() */
}
