package secfs.common;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Encoder {
	public static BlockId getInstance(MessageDigest messageDigest, EncodedPublicKey encodedPublicKey) {
		messageDigest.update(encodedPublicKey.getBytes());
		return new BlockId(messageDigest.digest());
	}
	
	public static BlockId getInstance(MessageDigest messageDigest, HashBlock hashBlock) {
		messageDigest.update(hashBlock.getBytes());
		return new BlockId(messageDigest.digest());
	}
	
	public static HashBlock getInstance(MessageDigest messageDigest, byte[] block) {
		//TODO: Finish me!!!
        //messageDigest.update(block);
        //byte[] digest = messageDigest.digest();
		return new HashBlock(block);
	}

	public static KeyBlock getInstance(byte[] block) {
		//TODO: Finish me!!!
		return new KeyBlock(block);
	}

	public static EncodedPublicKey getInstance(PublicKey publicKey) {
		//TODO: Finish me!!!
		byte[] encodedPublicKey = publicKey.getEncoded();
		if (encodedPublicKey == null) {
			throw new NullPointerException();
		}
		return new EncodedPublicKey(encodedPublicKey);
	}

	public static EncodedSignature getInstance(Signature signature, PrivateKey privateKey, byte[] data)
			throws InvalidKeyException, SignatureException {
		signature.initSign(privateKey); //TODO: add random value
		signature.update(data);
		return new EncodedSignature(signature.sign());
	}
}
