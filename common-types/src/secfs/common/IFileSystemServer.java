package secfs.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import secfs.common.exception.BlockNotFoundException;
import secfs.common.exception.InvalidRemoteArgumentException;
import secfs.common.exception.TamperedBlockException;

public interface IFileSystemServer extends Remote {
	public static final String SERVICE_NAME = "BlockServer";
	
	FileSystemServerReply storePubKey(EncodedCertificate certificate) throws RemoteException, InvalidRemoteArgumentException, CertificateException, KeyStoreException;
	List<EncodedPublicKey> readPubKeys() throws RemoteException, KeyStoreException;
  	FileBlock get(BlockId blockId) throws BlockNotFoundException, TamperedBlockException, RemoteException, InvalidRemoteArgumentException;
  	BlockId put_k(KeyBlock data, EncodedSignature signature, EncodedPublicKey public_key) throws RemoteException, TamperedBlockException, InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidRemoteArgumentException;
  	BlockId put_h(HashBlock data) throws RemoteException, InvalidRemoteArgumentException;
}