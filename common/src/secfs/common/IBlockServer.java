package secfs.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBlockServer extends Remote {
	public static final String SERVICE_NAME = "BlockServer";
	
  	FileBlock get(BlockId blockId) throws BlockNotFoundException, TamperedBlockException, RemoteException;
  	BlockId put_k(KeyBlock data, EncodedSignature signature, EncodedPublicKey public_key) throws RemoteException;
  	BlockId put_h(HashBlock data) throws RemoteException;
}