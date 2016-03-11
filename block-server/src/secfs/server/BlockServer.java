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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import secfs.common.BlockId;
import secfs.common.BlockNotFoundException;
import secfs.common.EncodedPublicKey;
import secfs.common.EncodedSignature;
import secfs.common.FileBlock;
import secfs.common.HashBlock;
import secfs.common.IBlockServer;
import secfs.common.KeyBlock;
import secfs.common.TamperedBlockException;

import java.lang.System;

public class BlockServer extends UnicastRemoteObject implements IBlockServer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7081463307932455315L;

	private boolean serverUnderAttack= false;
	private boolean canAttack=true;
	
	Map<BlockId, FileBlock> _dataBase;
	Map<BlockId, BlockId> _keyBlockIdTable;
	
	public BlockServer() throws RemoteException {
		//Initialise tables
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

  	public FileBlock get(BlockId blockId)
  			throws BlockNotFoundException, TamperedBlockException, RemoteException {
  		System.out.println("Invoking get");				//TODO: Erase print
  		
  		//Check if block exists
  		BlockId currentBlockId;
    	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
    		currentBlockId = entry.getKey();
    		if(currentBlockId.hashCode() == blockId.hashCode()) {
    			try {
    				BlockId newBlockId = createBlockId(entry.getValue().getBytes());
    				System.out.println("Value: " + Arrays.toString(newBlockId.getBytes()));
    				System.out.println("Key: " + Arrays.toString(entry.getKey().getBytes()));
    				if (_keyBlockIdTable.get(currentBlockId) != null)
    				System.out.println("Table: " + Arrays.toString(_keyBlockIdTable.get(currentBlockId).getBytes()));
    				System.out.println("Returns: " + Arrays.toString(entry.getValue().getBytes()));
    				if(!entry.getKey().equals(newBlockId) &&
					   (_keyBlockIdTable.get(currentBlockId) == null ||
					    !_keyBlockIdTable.get(currentBlockId).equals(newBlockId))) {
						throw new TamperedBlockException();
					}
    				
    				if(serverUnderAttack && canAttack){
    					serverUnderAttack=false;
    					return new FileBlock("U were attacked".getBytes());
    				}
					return entry.getValue();
				} catch (NoSuchAlgorithmException e) {
					//Not suppose to happen, even as an invalid state
					System.out.println(e.getMessage());
					System.exit(1);
					throw new RemoteException("get: " + e.getMessage());
				}
    		}
    	}
  		
  		throw new BlockNotFoundException();
    }

   	public BlockId put_k(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey)
   			throws TamperedBlockException, InvalidKeyException, InvalidKeySpecException, RemoteException, SignatureException {
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
        	signature.update(keyBlock.getBytes());
        	if(!signature.verify(encodedSignature.getBytes())){
        		System.out.println("Verification failled!");
        		throw new TamperedBlockException();
        	}
        	
        	byte[] content = new byte[keyBlock.getBytes().length + encodedSignature.getBytes().length];
        	System.arraycopy(keyBlock.getBytes(), 0, content, 0, keyBlock.getBytes().length);
        	System.arraycopy(encodedSignature.getBytes(), 0, content, keyBlock.getBytes().length, encodedSignature.getBytes().length);
        	KeyBlock signedKeyBlock = new KeyBlock(content);

        	//Put key block into database using public key's hash
        	BlockId blockId = createBlockId(encodedPublicKey.getBytes());
        	boolean blockExists = false;

      		//Replace block if block exists
        	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
        		if(entry.getKey().hashCode() == blockId.hashCode()) {
        			_dataBase.put(entry.getKey(), signedKeyBlock);
        			_keyBlockIdTable.put(entry.getKey(), createBlockId(signedKeyBlock.getBytes()));
        			blockExists = true;
        			break;
        		}
        	}
        	//Else put block into new slot
        	if(!blockExists) {
        		_dataBase.put(blockId, signedKeyBlock);
        		_keyBlockIdTable.put(blockId, createBlockId(signedKeyBlock.getBytes()));
        	}

        	System.out.println(Arrays.toString(signedKeyBlock.getBytes()));

        	//Return block id
			return blockId;
		} catch (NoSuchAlgorithmException e) {
			//Not suppose to happen, even as an invalid state
			System.out.println(e.getMessage());
			System.exit(1);
			throw new RemoteException("put_k: " + e.getMessage());
		}
    }

    public BlockId put_h(HashBlock hashBlock) throws RemoteException {
        System.out.println("Invoking put_h");
        try {
        	//Put hash block into database using hash block's hash
        	BlockId blockId = createBlockId(hashBlock.getBytes());
        	boolean blockExists = false;
        	
      		//Replace block if block exists
        	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
        		if(entry.getKey().hashCode() == blockId.hashCode()) {
        			blockExists = true;
        			break;
        		}
        	}
        	//Else put block into new slot
        	if(!blockExists) {
        		_dataBase.put(blockId, hashBlock);
        	}
        	
        	//Return block id
			return blockId;
		} catch (NoSuchAlgorithmException e) {
			//Not suppose to happen, even as an invalid state
			System.out.println(e.getMessage());
			System.exit(1);
			throw new RemoteException("put_h: " + e.getMessage());
		}
    }
    
    public void serverAttack(){
    	serverUnderAttack=true;
    }
}