package pt.ulisboa.tecnico.sec.filesystem.authentication;

import java.security.PublicKey;

public interface IAuthenticator {

	public PublicKey getPublicKey();
	public byte[] signData(byte[] data);
	public boolean verifySignature(byte[] in_signature, byte[] data);
	public void exit();
}
