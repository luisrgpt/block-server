package ccauth;

import java.security.PublicKey;

public interface IAuth {

	public byte[] getCitizenAuthCertInBytes();
	public PublicKey getPublicKey();
	public byte[] signData(byte[] data);
	public boolean verifySignature(byte[] in_signature, byte[] data);
	public void exit();
}
