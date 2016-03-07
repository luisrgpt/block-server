package secfs.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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
	Map<BlockId, BlockId> _keyBlockIdTable;
	
	public BlockServer() throws RemoteException {
		_dataBase = new HashMap<>();
		_keyBlockIdTable = new HashMap<>();
    }

  	private BlockId createBlockId(byte[] bytes) throws NoSuchAlgorithmException{
    	//Create block id using block's hash
    	MessageDigest messageDigest;
		messageDigest = MessageDigest.getInstance("SHA-512");
    	messageDigest.update(bytes);
    	byte[] hash = messageDigest.digest();
    	return new BlockId(hash);
  	}

  	public FileBlock get(BlockId blockId) throws RemoteException {
  		System.out.println("Invoking get");				//TODO: Erase print
  		
  		//Check if block exists
    	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
    		BlockId currentBlockId = entry.getKey();
    		if(currentBlockId.hashCode() == blockId.hashCode()) {
    			try {
    				BlockId newBlockId = createBlockId(entry.getValue().getBytes());
    				if(!entry.getKey().equals(newBlockId) &&
					   (_keyBlockIdTable.get(currentBlockId) == null ||
					    !_keyBlockIdTable.get(currentBlockId).equals(newBlockId))) {
						throw new RemoteException("get: Block has been tampered");
					}
					return entry.getValue();
				} catch (NoSuchAlgorithmException e) {
					throw new RemoteException("get: " + e.getMessage());
				}
    		}
    	}
  		
  		throw new RemoteException("get: EmptySlotException");
     }
  	
   	public BlockId put_k(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey)
   			throws RemoteException {
        System.out.println("Invoking put_k");
        try {
        	//Decode public key
        	byte[] encodedKey = encodedPublicKey.getBytes();
        	X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encodedKey);
        	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        	PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        	
        	//Verify signature
        	Signature signature = Signature.getInstance("SHA512withRSA");
        	signature.initVerify(pubKey);
        	signature.update(encodedKey);
        	signature.verify(encodedSignature.getBytes());
        	
        	//Put key block into database using public key's hash
        	BlockId blockId = createBlockId(encodedPublicKey.getBytes());
        	boolean blockExists = false;

      		//Replace block if block exists
        	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
        		if(entry.getKey().hashCode() == blockId.hashCode()) {
        			_dataBase.put(entry.getKey(), keyBlock);
        			_keyBlockIdTable.put(entry.getKey(), createBlockId(keyBlock.getBytes()));
        			blockExists = true;
        			break;
        		}
        	}
        	//Else put block into new slot
        	if(!blockExists) {
        		_dataBase.put(blockId, keyBlock);
        	}
			
        	//Return block id
			return blockId;
		} catch (NoSuchAlgorithmException |
				 InvalidKeySpecException |
				 InvalidKeyException |
				 SignatureException e) {
			throw new RemoteException("put_k: " + e.getMessage());
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