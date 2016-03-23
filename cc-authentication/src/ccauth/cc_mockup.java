package ccauth;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

public class cc_mockup implements IAuth {

	private PublicKey _publicKey = null;
	private PrivateKey _privateKey = null;

	public cc_mockup() {
		if (_publicKey == null || _privateKey == null) {
			// Generate key pair for RSA encryption
			KeyPairGenerator keyGen;
			try {
				keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(1024);
				KeyPair keys = keyGen.generateKeyPair();

				// Set key pair state
				_publicKey = keys.getPublic();
				_privateKey = keys.getPrivate();
			} catch (NoSuchAlgorithmException e) {
				System.out
						.println("wrong algorithm to generate public and private keys");
				e.printStackTrace();
			}
		}
	}

	@Override
	public PublicKey getPublickKey() {
		return _publicKey;
	}

	@Override
	public byte[] signData(byte[] data) {
		byte[] output = null;

		if (_privateKey != null) {
			SecureRandom secureRandom = new SecureRandom(data);
			Signature signature;

			try {
				signature = Signature.getInstance("SHA512withRSA");
				signature.initSign(_privateKey, secureRandom);
				signature.update(data);
				output = signature.sign();
			} catch (NoSuchAlgorithmException | InvalidKeyException
					| SignatureException e) {
				System.out.println("Failled to sign the data");
				e.printStackTrace();
			}
		} else {
			System.out.println("Private key was not successful generated!");
		}

		return output;
	}

	@Override
	public boolean verifySignature(byte[] in_signature, byte[] data) {
		// Verify signature
		try {
			Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initVerify(_publicKey);
			signature.update(data);
			return signature.verify(in_signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException
				| SignatureException e) {
			System.out.println("Failled to sign the data");
			e.printStackTrace();
		}
		
		return false;
	}

}
