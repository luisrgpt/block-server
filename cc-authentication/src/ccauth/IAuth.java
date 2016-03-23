package ccauth;

import java.security.PublicKey;

public interface IAuth {

	public PublicKey getPublickKey();
	public byte[] signData(byte[] data);
	public boolean verifySignature(byte[] in_signature, byte[] data);
}
