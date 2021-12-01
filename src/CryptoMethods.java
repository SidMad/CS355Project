import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class CryptoMethods {
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
            Signature verify = Signature.getInstance("SHA256withECDSA");
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
            sign = Signature.getInstance("SHA256withECDSA");
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
}
