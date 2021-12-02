import java.io.*;
import java.net.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.nio.charset.StandardCharsets;


/*
`   Read in public key and secret key from their file.
    Read in the file with the passwords and generate its hash.

    Attempt to connect to the server at the host, if no such server exists skip to Alice's code,
    if it does exist, skip to Bob's code.

    Alice:
        Start server
        When a client connects, generate and send a signed SALT (counter).
        Wait for a message from the other party with their signature,
            Verify the public key and signature is indeed Bob's and continue
            If the connecting client is not Bob, let Alice know.
        Otherwise, sign and send our hashed (password.txt+salt)
            Wait for a signed hash of (password.txt+salt) from Bob.
            skip to Both:

    Bob:
        Wait for a signed salt message from Alice.
            If signature does not match Alice's, abort protocol.
        Wait for a signed hash of the file containing the passwords from Alice.
        Create a signed hash of (password.txt+salt) .
        skip to Both:

    Both:
        If the transmitted hash matches our hash, return "has file". Else, return "has different file" and terminate.


     Alice Method, Bob Method, Contractor class for shared methods and Constructor

 */


public class JavaSecureChannel {

    private static final int port = 8081;
    private static final int MAX_SIZE = 1024;
    private static final int HASHED_SIZE = 512;
    private static final int BUFFER_SIZE = 16384;
    private static final boolean Verify_keys = false;
    private final String passwordsFilePath;
    private PrivateKey mySecretKey;
    private PublicKey theirPublicKey;
    boolean useSalt = true;
// private JavaSecureChannel(int port, String passwordsFilePath, String theirPublicKeyFilepath, String myPrivateKeyFilepath ) {
    private JavaSecureChannel(String passwordsFilePath, String theirPublicKeyFilepath, String myPrivateKeyFilepath ) {

        // this.port = port; //TODO port should be a command line argument so if multiple published
        this.passwordsFilePath = passwordsFilePath;

        if(!verifyPath(passwordsFilePath) ) {
            System.out.println("Path to password file is Invalid");
            System.exit(0);
        }
        if(!verifyPath(theirPublicKeyFilepath)) {
            System.out.println("Path to Public Key file is Invalid");
            System.exit(0);
        }
        if(!verifyPath(myPrivateKeyFilepath) ) {
            System.out.println("Path to your Private Key file is Invalid");
            System.exit(0);
        }
        KeyFactory kf = null;

        /*
         * Code to convert byte[] to PrivateKey and PublicKey
         * Code from https://stackoverflow.com/questions/19353748/how-to-convert-byte-array-to-privatekey-or-publickey-type/22077915
         */
        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Unable to create RSA Key Factory");
            e.printStackTrace();
        }

        if (kf == null) {
            System.out.println("Key Factory is null - abort");
            System.exit(0);
        }
        if (!Verify_keys) {
            return;
        }
        try {
            mySecretKey = kf.generatePrivate(new PKCS8EncodedKeySpec(readFile(myPrivateKeyFilepath)));
            theirPublicKey = kf.generatePublic(new X509EncodedKeySpec(readFile(theirPublicKeyFilepath)));
        } catch (InvalidKeySpecException e) {
            System.out.println("Error Generating Key");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error Reading Key");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Null Pointer detected while creating/reading key");
        }
        if (mySecretKey == null) {
            System.out.println("Secret key could not be read");
            System.exit(0);
        }
        if (theirPublicKey == null) {
            System.out.println("Secret key could not be read");
            System.exit(0);
        }
    } /* JavaSecureChannel() */

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Please invoke with arguments [path to passwords.txt file] [filepath to other contractor's public key] [filepath to my secret key]");
            System.exit(0);
        }
//        JavaSecureChannel jsc = new JavaSecureChannel(port, args[0],args[1],args[2]);
        JavaSecureChannel jsc = new JavaSecureChannel(args[0],args[1],args[2]);

        try {
            jsc.Bob();
        } catch (ConnectException e) {
            System.out.println("Alice isn't out there, SAD! You're Alice");
            try {
                jsc.Alice();
            } catch (Exception general) {
                System.out.println("Problem Initialising server");
                general.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Cannot connect to server");
            e.printStackTrace();
        }
    } /* main() */


    /*
     * Look at the below library to transform the channel into a secure channel
     * https://docs.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html
     * Function to Read the data in the pipeline into the cache using socketChannel's read method
     */
    public byte[] makeSalt()
    {
        byte[] salt = new byte[HASHED_SIZE];
        new Random().nextBytes(salt);
        return salt;
    }
    public void Alice() throws IOException {
        // Server side, create Server Socket Channel through open method
        // Note that at this point, the server side has not yet bound the port.

        System.out.println("Alice is in play");
        byte[] mySalt = makeSalt();
        String generatedSalt  = new String(mySalt, StandardCharsets.UTF_8);
        System.out.println("generated salt is " + generatedSalt);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        SocketChannel socketChannel = null;
        // Binding port number
        serverSocketChannel.bind(new InetSocketAddress(port));

        // Create a byte buffer The size of the buffer is 1024 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);

        while (true) {
            System.out.println("------- Alice is waiting---------");
            // On the server side, receive a link from the client and return a link if there is a client.
            try {
                socketChannel = serverSocketChannel.accept();
            } catch (Exception e){
                System.out.println("Exception occurred while trying to receive a link from the client ");
            }
            if (socketChannel != null) {
                System.out.println("we have accepted a client");
                try {
                   // byte[] salt = generatedSalt.getBytes(StandardCharsets.UTF_8);
                    sendMessage(mySalt, socketChannel);
                    System.out.println("Alice sent her salt");
                } catch (IOException e) {
                    System.out.println("Unable to send message");
                    e.printStackTrace();
                    // TODO have appropriate action
                }
                while (true) { //this is so we hang and wait for another client if someone malicious poses as bob
                    System.out.println("alice is waiting for bob to send his salt");
                    //ready to wait for bob to send his hash
                    byteBuffer.clear();
                    int readSize = socketChannel.read(byteBuffer);
                    String hisSalt = receiveMessage(byteBuffer.array());
                    byte[] myHash = hashFile(passwordsFilePath, hisSalt.getBytes(StandardCharsets.UTF_8));
                    // Send hash and signature to Bob
                    System.out.println("my hash is " + new String(myHash,StandardCharsets.UTF_8));
                    sendMessage(myHash,socketChannel);
                    byteBuffer = ByteBuffer.allocate(MAX_SIZE);
                    readSize = socketChannel.read(byteBuffer);
                    String hisHash = receiveMessage(byteBuffer.array());
                        // Hash the password file

                        // Compare the hashes and print appropriate message
                        finishProtocol(new String(hashFile(passwordsFilePath, mySalt), StandardCharsets.UTF_8), hisHash);
                }
            }
            // try {
                // Thread.sleep(1000);
            // } catch (InterruptedException e) {
                // e.printStackTrace();
            // }
        }
    } /* Alice() */


    public void finishProtocol(String myHash, String theirHash) {
        System.out.println("comparing the following hashes");
        System.out.println(myHash + " with length " + myHash.length());
        System.out.println(theirHash + " with lengh " + theirHash.length());
        if (theirHash.trim().equals(myHash.trim())) {
            System.out.println("The other contractor has the same file!");
        } else {
            System.out.println("The other contractor made contact but has a different file!");
        }
        System.exit(0);
    }

    // client code
    public void Bob() throws IOException, ConnectException{
//ThierSalt makes Myhash
        //Mysalt makes their hash
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", port));
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);
        System.out.println("you are bob");
        //wait for salt
        //
            // Clear cache data, new data can be received
            byteBuffer.clear();
            // Read the data from the pipe socketChannel into the cache byteBuffer
            // ReadSize denotes the number of bytes read
            System.out.println("bob is waiting for salt message");
            socketChannel.read(byteBuffer);
            byte[] mySalt = makeSalt();
            System.out.println("Bob's socket got a message");
            String theirsalt = receiveMessage(byteBuffer.array());
            System.out.println("sending my salt");
            sendMessage(mySalt, socketChannel);
            System.out.println("bob is hashing with their salt " + theirsalt);

            //Wait for final message to finish off protocol
            // Clear cache data, new data can be received
            byteBuffer = ByteBuffer.allocate(MAX_SIZE);
            int readSize = socketChannel.read(byteBuffer);
            String theirHash = receiveMessage(byteBuffer.array());
            byte[] myHash = hashFile(passwordsFilePath, theirsalt.getBytes());
            System.out.println("My hash is " + new String(myHash, StandardCharsets.UTF_8));
            sendMessage(myHash,socketChannel);
            finishProtocol(new String(hashFile(passwordsFilePath, mySalt), StandardCharsets.UTF_8), theirHash);
            socketChannel.close();
    } /* connectServer() */
    public byte[] catBuffers(byte[] a, byte[] b) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(a);
            outputStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
/*from https://stackoverflow.com/questions/1741545/java-calculate-sha-256-hash-of-large-file-efficiently*/
    public byte[] hashFile(String filepath, byte[] salt) throws IOException {
        byte[] hash = null;
        salt = new String(salt, StandardCharsets.UTF_8).trim().getBytes(StandardCharsets.UTF_8);
        System.out.println("my file is " + new String(readFile(filepath)));
        RandomAccessFile file = null;
        try {
            // RandomAccessFile file = new RandomAccessFile("T:\\someLargeFile.m2v", "r");
            file = new RandomAccessFile(filepath, "r");
            MessageDigest hashSum = null;
            try {
                hashSum = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Unable to Hash Message");
                e.printStackTrace();
            }
            if (hashSum == null) {
                // TODO Action to take if hash is null
                System.out.println("no hashsum algo available");
                System.exit(0);
            }
            long read = 0;

            // calculate the hash of the whole file for the test
            long offset = file.length();
            int unitsize;
            while (read < offset) {
                unitsize = (int) (((offset - read) >= BUFFER_SIZE) ? BUFFER_SIZE : (offset - read));
                byte[] buffer = new byte[BUFFER_SIZE];
                file.read(buffer, 0, unitsize);
                try {
                    if (useSalt) {
                        buffer = catBuffers(buffer, salt);
                    }
                    hashSum.update(CryptoMethods.computeHash(buffer), 0, unitsize);
                } catch (NullPointerException e) {
                    System.out.println("Null Pointer Detected - HashSum failed to update");
                    e.printStackTrace();
                }
                read += unitsize;
            }
            // file.close();
            hash = new byte[hashSum.getDigestLength()];
            hash = hashSum.digest();
        } catch (FileNotFoundException e) {
            System.out.println("The file to hash could not be found");
            e.printStackTrace();
        }
        finally{
            if (file != null)
                file.close();
        }
        return hash;
    } /* hashFile() */

    /* public static byte[][] genKeyPairBytes(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[][] keyPairBytes = new byte[2][];
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        gen.initialize(keySize, new SecureRandom());
        KeyPair pair = gen.generateKeyPair();
        keyPairBytes[0] = pair.getPrivate().getEncoded();
        keyPairBytes[1] = pair.getPublic().getEncoded();
        return keyPairBytes;
    } /* genKeyPairBytes() */

    //Verify signature and return without the tag
    public String receiveMessage(byte[] msg) { //TODO either abort with a "not the person you think" or return the plaintext of the message
        //return the message without the little trailing signature verifier
        System.out.println("receiving message");
        String combined = new String(msg, StandardCharsets.UTF_8);
        if (Verify_keys) {
            String[] both = combined.split("PADDING");
            byte[] hash = both[0].getBytes(StandardCharsets.UTF_8);
            byte[] signature = both[1].getBytes(StandardCharsets.UTF_8);
            boolean ver = CryptoMethods.verifyHash(theirPublicKey, hash, signature);
            String answer;
            if (ver) {
                answer = both[0];
                return answer;
            } else {
                answer = "not the person you think";
                System.out.println(answer);
                System.exit(0);
            }
        } else {
            System.out.println("message is " + new String(msg,StandardCharsets.UTF_8));
            return new String(msg,StandardCharsets.UTF_8);
        }
        return null;
    }

    //create the tag for the message and send it
    public void sendMessage(byte[] msg, SocketChannel socketChannel) throws IOException {
        /*
         * Code to convert byte buffer to string: String s = StandardCharsets.UTF_8.decode(byteBuffer).toString();
           derived from https://stackoverflow.com/questions/17354891/java-bytebuffer-to-string/17355227

         * Code to convert String to Byte[]: byte[] bytes = string.getBytes(StandardCharsets.UTF_8)
           https://stackoverflow.com/questions/1536054/how-to-convert-byte-array-to-string-and-vice-versa

         * Code to convert byte[] to String: String s = new String(byteArray, StandardCharsets.UTF_8)
         */

        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);
        //TODO sign the message before we send with the crypto primitive
        if (Verify_keys) {
            byte[] signature = CryptoMethods.computeSignature(mySecretKey, msg);

            if (signature == null) {
                System.exit(0); // TODO The action to take if signature fails
            }

            String messageCombined = new String(msg, StandardCharsets.UTF_8) + "PADDING" + new String(signature, StandardCharsets.UTF_8);
            // To split messageCombined : decode from byte buffer form. Then messageCombined.split("PADDING").
            byte[] messageToTransmit = messageCombined.getBytes();
            byteBuffer.put(messageToTransmit);
        } else {
            System.out.println("alice transmitting " + new String(msg,StandardCharsets.UTF_8));
            byteBuffer.put(msg);
        }
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            // Write the data in the byte cache into the pipeline
            socketChannel.write(byteBuffer);
        }
    } /* sendMessage() */


    public static byte[] readFile(String path) throws IOException {

        File file = new File(path);
        if(!FileExists(file)) {
            System.out.println("Path to file incorrect");
            throw new FileNotFoundException();
        }
        // Creating a BufferedReader object to read the file
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;

        // Iterate through each line of the file till we reach the last line
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        br.close();
        return content.toString().getBytes();
    } /* readFile() */

    public static boolean verifyPath(String path) {
        if (path == null)
            return false;
        File tempFile = new File(path);
        return FileExists(tempFile);
    } /* verifyPath() */

    public static boolean FileExists(File file) {
        if (file == null)
            return false;
        return file.exists();
    } /* verifyFileExists() */

}
