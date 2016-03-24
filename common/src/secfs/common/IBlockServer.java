package secfs.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public interface IBlockServer extends Remote {
	public static final String SERVICE_NAME = "BlockServer";
	
	void storePubKey(EncodedPublicKey publickKey) throws RemoteException;
	ArrayList<EncodedPublicKey> readPubKeys() throws RemoteException;
  	FileBlock get(BlockId blockId) throws BlockNotFoundException, TamperedBlockException, RemoteException;
  	BlockId put_k(KeyBlock data, EncodedSignature signature, EncodedPublicKey public_key) throws RemoteException, TamperedBlockException, InvalidKeyException, InvalidKeySpecException, SignatureException;
  	BlockId put_h(HashBlock data) throws RemoteException;
}