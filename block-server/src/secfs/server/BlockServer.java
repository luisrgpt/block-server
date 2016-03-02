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

  	private BlockId createBlockId(byte[] bytes) throws NoSuchAlgorithmException{
    	//Create block id using block's hash
    	MessageDigest messageDigest;
		messageDigest = MessageDigest.getInstance("sha1");
    	messageDigest.update(bytes);
    	byte[] hash = messageDigest.digest();
    	return new BlockId(hash);
  	}

  	public FileBlock get(BlockId blockId) throws RemoteException {
  		System.out.println("Invoking get");				//TODO: Erase print
  		
  		//Check if block exists
    	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
    		if(entry.getKey().hashCode() == blockId.hashCode()) {
    			return entry.getValue();
    		}
    	}
  		
  		throw new RemoteException("EmptySlotException");
     }
  	
   	public BlockId put_k(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey) throws RemoteException {
        System.out.println("Invoking put_k");
        try {
        	//Put key block into database using public key's hash
        	BlockId blockId = createBlockId(encodedPublicKey.getBytes());
        	if(_dataBase.putIfAbsent(blockId, keyBlock) != null) {
        		throw new RemoteException("SlotAlreadyFilledException");
        	}
			
        	//Return block id
			return blockId;
		} catch (NoSuchAlgorithmException e) {
			throw new RemoteException("NoSuchAlgorithmException");
		}
    }

    public BlockId put_h(HashBlock hashBlock) throws RemoteException {
        System.out.println("Invoking put_h");
        try {
        	//Put hash block into database using hash block's hash
        	BlockId blockId = createBlockId(hashBlock.getBytes());
        	if(_dataBase.putIfAbsent(blockId, hashBlock) != null) {
        		throw new RemoteException("SlotAlreadyFilledException");
        	}
        	
        	//Return block id
			return blockId;
		} catch (NoSuchAlgorithmException e) {
			throw new RemoteException("NoSuchAlgorithmException");
		}
    }
}