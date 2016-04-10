package pt.tecnico.ulisboa.sec.filesystem.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import pt.tecnico.ulisboa.sec.filesystem.common.exception.BlockNotFoundException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.InvalidRemoteArgumentException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.NullArgumentException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.TamperedBlockException;

public interface IFileSystemServer extends Remote {
	FileSystemServerReply storePubKey(EncodedPublicKey encodedPk) throws RemoteException;
	List<EncodedPublicKey> readPubKeys() throws RemoteException;
  	FileBlock get(BlockId blockId) throws BlockNotFoundException, TamperedBlockException, RemoteException, InvalidRemoteArgumentException, NullArgumentException, NoSuchAlgorithmException;
  	BlockId put_k(KeyBlock data, EncodedSignature signature, EncodedPublicKey public_key) throws RemoteException, TamperedBlockException, InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidRemoteArgumentException, NullArgumentException, NoSuchAlgorithmException;
  	BlockId put_h(HashBlock data) throws RemoteException, InvalidRemoteArgumentException, NullArgumentException, NoSuchAlgorithmException;
}