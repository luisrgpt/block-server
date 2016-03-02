package secfs.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import secfs.common.BlockId;
import secfs.common.EncodedPublicKey;
import secfs.common.EncodedSignature;
import secfs.common.Encoder;
import secfs.common.FileBlock;
import secfs.common.HashBlock;
import secfs.common.IBlockServer;
import secfs.common.KeyBlock;

import java.lang.System;

public class BlockServer extends UnicastRemoteObject implements IBlockServer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7081463307932455315L;

	Map<BlockId, FileBlock> _dataBase;
	
	public BlockServer() throws RemoteException {
		_dataBase = new HashMap<>();
    }

  	public FileBlock get(BlockId blockId) throws RemoteException {
  		System.out.println("Invoking get");				//TODO: Erase print
  		if(!_dataBase.containsKey(blockId)) {
  			System.out.println("Failed to get block");	//TODO: Erase print
  			throw new RemoteException();
  		}
  	    System.out.println("Success");					//TODO: Erase print
        return _dataBase.get(blockId);
     }

   	public BlockId put_k(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey) throws RemoteException {
        System.out.println("Invoking put_k");
        try {
			BlockId id = Encoder.getInstance(MessageDigest.getInstance("md5"), encodedPublicKey);
			_dataBase.put(id, keyBlock);
			return id;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RemoteException();
		}
    }

    public BlockId put_h(HashBlock hashBlock) throws RemoteException {
        System.out.println("Invoking put_h");
        try {
			BlockId id = Encoder.getInstance(MessageDigest.getInstance("md5"), hashBlock);
			_dataBase.put(id, hashBlock);
			return id;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RemoteException();
		}
    }
}