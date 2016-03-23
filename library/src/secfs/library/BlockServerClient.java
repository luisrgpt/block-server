package secfs.library;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import secfs.common.RmiNode;
import secfs.common.TamperedBlockException;
import secfs.common.BlockId;
import secfs.common.BlockNotFoundException;
import secfs.common.KeyBlock;
import secfs.common.HashBlock;
import secfs.common.IBlockServer;
import secfs.common.EncodedPublicKey;
import secfs.common.EncodedSignature;
import secfs.common.FileBlock;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ccauth.IAuth;
import ccauth.cc_mockup;

public class BlockServerClient extends RmiNode {
	
	private boolean tamperAttack=false;
	private boolean impersonate=false;

	//Block minimal size ->  4 (64 + 4)*2 = 140 bytes
	private static final int BLOCK_LENGTH = 300;
	
	private BlockId _fileId = null;
	private IBlockServer _blockServer = null;

	private Map<Integer, BlockId> _blockTable = new HashMap<>();
	private Map<Integer, BlockId> _previousBlockTable = null; //Check integrity in the client side
	
	private int _bytesRead = 0;
	private IAuth cc_Auth= new cc_mockup();

	private IBlockServer getBlockServer()
			throws RemoteException, NotBoundException {
		if (_blockServer == null) {
			//Bind a RMI connection to BlockServer
			Registry registry;
			registry = LocateRegistry.getRegistry();
			_blockServer = (IBlockServer)registry.lookup(IBlockServer.SERVICE_NAME);
		}
		return _blockServer;
	}

	private Map<Integer, BlockId> getBlockTable(BlockId fileId)
			throws NotBoundException, TamperedBlockException, RemoteException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		try {
			_blockTable = new HashMap<>();
			
			System.out.println("[getBlockTable] ID: " + Arrays.toString(fileId.getBytes()));
			byte[] block = getAndVerifyKeyBlock(fileId).getBytes();
			
			System.out.println("[getBlockTable] Returned: " + Arrays.toString(block));
			
			BlockId nullPointer = new BlockId(new byte[64]);
			_blockTable.putAll(getBlockTable(block));
			
			while(!_blockTable.get(-1).equals(nullPointer)) {
				System.out.println("[getBlockTable] IdTable2");
				for (Map.Entry<Integer, BlockId> entry : _blockTable.entrySet()) {
					System.out.println("[getBlockTable] "+entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
				}
				System.out.println();
				block = getBlockServer().get(_blockTable.get(-1)).getBytes();
				_blockTable.remove(-1);
				System.out.println("[getBlockTable] Returned2: " + Arrays.toString(block));
				_blockTable.putAll(getBlockTable(block));
			}
			_blockTable.remove(-1);
			System.out.println("[getBlockTable] IdTable2");
			for (Map.Entry<Integer, BlockId> entry : _blockTable.entrySet()) {
				System.out.println("[getBlockTable] " +entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
			}
			System.out.println();
			
			return _blockTable;
		} catch (BlockNotFoundException e) {
			System.out.println("[getBlockTable] Block not found -> Creating new file");
			return new HashMap<>();
		}
	}
	
	//only applicable to hash blocks
  	private FileBlock getAndVerifyHashBlock(BlockId blockId, BlockId prevBlockId)
  			throws NoSuchAlgorithmException, TamperedBlockException, RemoteException, BlockNotFoundException, NotBoundException{
  		FileBlock block = getBlockServer().get(blockId);
  		
    	//Create block id using block's hash
    	MessageDigest messageDigest;
		messageDigest = MessageDigest.getInstance("SHA-512");
		try{
			messageDigest.update(block.getBytes());
		}catch( NullPointerException e){
			System.out.println("Could not get the block bytes");
		}
		
    	byte[] hash = messageDigest.digest();
    
    	if(Arrays.equals(hash, prevBlockId.getBytes()) || _previousBlockTable.isEmpty()){
    		//then return block
    		return block;
    	}else{
    		//else throw exception
    		throw new TamperedBlockException();
    	}
    	
  	}
  	
	//only applicable to hash blocks
  	private FileBlock getAndVerifyKeyBlock(BlockId fileId)
  			throws NoSuchAlgorithmException, TamperedBlockException, InvalidKeyException, SignatureException, RemoteException, BlockNotFoundException, NotBoundException{
  		FileBlock block = getBlockServer().get(fileId);
		
		byte[] data = new byte[BLOCK_LENGTH],
		signatureData = new byte[block.getBytes().length - BLOCK_LENGTH];
  		
		System.arraycopy(block.getBytes(), 0, data, 0, BLOCK_LENGTH);
		System.arraycopy(block.getBytes(), BLOCK_LENGTH, signatureData, 0, signatureData.length);
    
//    	//Verify signature
//    	Signature signature = Signature.getInstance("SHA512withRSA");
//    	signature.initVerify(_publicKey);
//    	signature.update(data);
		
    	if(cc_Auth.verifySignature(signatureData, data)){
    		//then return block
    		return block;
    	}else{
    		//else throw exception
    		throw new TamperedBlockException();
    	}
  	}
	
  	private byte[] getBytes(int i) {
		ByteBuffer dbuf = ByteBuffer.allocate(4);
		dbuf.putInt(i);
		return dbuf.array();
  	}
  	
  	private int getInt(byte[] array) {
		ByteBuffer buffer = ByteBuffer.wrap(array);
		return buffer.getInt();
  	}
  	
  	private byte[] getBytes(List<Integer> keySubList, List<BlockId> valueSubList) {
		byte[] block = new byte[BLOCK_LENGTH];
		int size = Math.min((int) Math.floor((double) (BLOCK_LENGTH - 4) / 68), keySubList.size());
		System.arraycopy(getBytes(size), 0, block, 0, 4);

		byte[] blockId;
		Integer key;
		for(int index = 0; index < size; index++) {
			key = keySubList.get(index);
			blockId = valueSubList.get(index).getBytes();

			System.arraycopy(getBytes(key), 0, block, 4 + 68*index, 4);
			System.arraycopy(blockId, 0, block, 4 + 68*index + 4, 64);
		}
		System.out.println("[getBytes] content: "+ Arrays.toString(block));
		
		return block;
  	}
  	
  	private Map<Integer, BlockId> getBlockTable(byte[] array) {
  		//System.out.println("That's strange: " + Arrays.toString(array));
		Map<Integer, BlockId> blockTable = new HashMap<>();
		byte[] sizeArray = new byte[4];
		System.arraycopy(array, 0, sizeArray, 0, 4);
		_bytesRead += 4;

		int size = getInt(sizeArray);
		byte[] keyArray = new byte[4], blockId = new byte[64];
		for(int index = 0; index < size; index++) {
			System.arraycopy(array, 4 + 68*index, keyArray, 0, 4);
			_bytesRead += 4;
			System.arraycopy(array, 4 + 68*index + 4, blockId, 0, 64);
			_bytesRead += 64;

			blockTable.put((Integer) getInt(keyArray), new BlockId(blockId.clone()));
		}
		
		System.out.println("IdTable2");
		for (Map.Entry<Integer, BlockId> entry : blockTable.entrySet()) {
			System.out.println("[getBlockTable] "+entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
		}
		System.out.println();
		
		return blockTable;
  	}
  	
  	private void sendIndexBlocks(Map<Integer, BlockId> map)
  			throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException, TamperedBlockException {
		IBlockServer blockServer = getBlockServer();
  		
  		int size = (int) Math.floor((double) (BLOCK_LENGTH - 4) / 68) - 1,
				mapSize = map.size();
		BlockId blockId = new BlockId(new byte[64]);
		//if(size < mapSize) {
			int blockSize = (int) Math.ceil((double) mapSize / size),
				firstKey, lastKey;
			System.out.println("[sendIndexBlocks] blockSize = " + blockSize +
					" size = " + size + 
					" mapSize = " + mapSize);
			List<Integer> keyList = new ArrayList<Integer>(map.keySet());
			List<BlockId> valueList = new ArrayList<BlockId>(map.values());
			for(int index = size - 1; index >= 0; index--) {
				firstKey = blockSize * index;
				lastKey = Math.min(firstKey + blockSize, mapSize);
				
				List<Integer> keySubList = new ArrayList<>(keyList.subList(firstKey, lastKey));
				List<BlockId> valueSubList = new ArrayList<>(valueList.subList(firstKey, lastKey));
				
				keySubList.add(-1);
				valueSubList.add(blockId);
				
				for (Integer entry : keySubList) {
					System.out.println("[sendindexBlocks] keySublist: "+entry);
				}
				
				for (BlockId entry : valueSubList) {
					System.out.println("[sendindexBlocks] valueSubList: "+Arrays.toString(entry.getBytes()));
				}
				
				byte[] array = getBytes(keySubList, valueSubList);
				if(index > 0) {
					blockId = blockServer.put_h(new HashBlock(array));
				} else {
					//Create key block
					KeyBlock keyBlock = new KeyBlock(array);
				
//					//Create encoded signature
//		        	SecureRandom secureRandom = new SecureRandom(keyBlock.getBytes());
//		        	Signature signature = Signature.getInstance("SHA512withRSA");
//					signature.initSign(_privateKey, secureRandom);
//					signature.update(keyBlock.getBytes());
					
					EncodedSignature encodedSignature = new EncodedSignature(cc_Auth.signData(keyBlock.getBytes()));
				
					//Create encoded public key
					EncodedPublicKey encodedPublicKey = new EncodedPublicKey(cc_Auth.getPublickKey().getEncoded());
					if(tamperAttack){
						//After generate the signature with the data we should change the block content
						System.out.println("Tampering data...");
						keyBlock = new KeyBlock("U were cracked!".getBytes());
						tamperAttack=false;
					}
					
					if(impersonate){
						System.out.println("Changing public key...");
						KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
						keyGen.initialize(1024);
						KeyPair keys = keyGen.generateKeyPair();
				
						//Set key pair state
						EncodedPublicKey fakeEncodedPublicKey = new EncodedPublicKey(keys.getPublic().getEncoded());
						encodedPublicKey = fakeEncodedPublicKey;
						impersonate=false;
					}
					
					blockServer.put_k(keyBlock, encodedSignature, encodedPublicKey);
				}
				
				System.out.println();
			}
		//}
  	}
	
	@SuppressWarnings("unused")
	public byte[] FS_init()
			throws FileSystemException {
		//System.out.println("Invoking FS_init");
		
		if(BLOCK_LENGTH < 140) {
			throw new FileSystemException("[FS_init]: Block size is too small (< 140).");
		}

		try {
			//Generate key pair for RSA encryption
//			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//			keyGen.initialize(1024);
//			KeyPair keys = keyGen.generateKeyPair();
//	
//			//Set key pair state
//			_publicKey = keys.getPublic();
//			_privateKey = keys.getPrivate();
	
			//System.out.println(">>>>>>>>>>>pk size: "+_publicKey.getEncoded().length);
			//Return public key's sha-1 hash
			MessageDigest messageDigest;
			messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(cc_Auth.getPublickKey().getEncoded());
			_fileId = new BlockId(messageDigest.digest());
	
			return _fileId.getBytes();
		} catch (NoSuchAlgorithmException e) {
			throw new FileSystemException("[FS_write]: " + e.getMessage());
		}
	}
	
	private int copyContent(int pos, int size, byte[] newBlock, byte[] content, int posContent) {
		int indexA = posContent;
		for(int indexB = pos,
				limitA = size,
				limitB = newBlock.length;
				indexA < limitA &&
				indexB < limitB;
				indexA++, indexB++) {
			newBlock[indexB] = content[indexA];
		}
		
		return indexA;
	}
	
	private int extractContent(int pos, int size, byte[] newBlock, byte[] content, int posContent) {
		int indexA = posContent;
		for(int indexB = pos,
				limitA = size,
				limitB = newBlock.length;
				indexA < limitA &&
				indexB < limitB;
				indexA++, indexB++) {
			content[indexA] = newBlock[indexB];
			_bytesRead++;
		}
		
		return indexA;
	}
	
	private void checkSize(int size, byte[] contents)
			throws FileSystemException {
		if (contents.length != size) {
			throw new FileSystemException("Size doesnt't match contents' size.");
		}
	}
	
	private void checkInit()
			throws FileSystemException {
		if (_fileId == null) {
			throw new FileSystemException("File system not initialized.");
		}
	}
	
	public void FS_write(int pos, int size, byte[] contents)
			throws FileSystemException {
		//foreach key from range(ceiling(pos/length), ceiling(pos+size/length))
		//    if map contains key
		//        then get block
		//        else create block
		
		//create block from max(pos + write, block.length) size
		//get pos position:
		//  while pointer smaller than pos and pointer smaller than block
		//    copy block
		//  while pointer smaller than pos + size
		//    copy contents
		//  while pointer smaller than block
		//    copy block
		
		//Throws FileSystemException
		
		checkSize(size, contents);
		checkInit();
		if(_previousBlockTable==null){
			_previousBlockTable = new HashMap<>();
		}
		try {
			IBlockServer blockServer = getBlockServer();
			Map<Integer, BlockId> blockTable = getBlockTable(_fileId);

			List<byte[]> blockList = new ArrayList<>();
			byte[] currentBlock;
			int firstKey = (int) Math.floor((double) pos / BLOCK_LENGTH),
			    lastKey = (int) Math.ceil(((double) pos + size) / BLOCK_LENGTH),
			    posContent = 0;

			if(blockTable.containsKey((Integer) firstKey)) {
				currentBlock = blockServer.get(blockTable.remove(firstKey)).getBytes(); 
			} else {
				currentBlock = new byte[BLOCK_LENGTH];
			}
			
			posContent = copyContent(pos % BLOCK_LENGTH, size, currentBlock, contents, posContent);

			System.out.println("[FS_Write] new content: "+Arrays.toString(currentBlock));
			blockList.add(currentBlock);

			System.out.println("[FS_Write] FirstKey: " + firstKey + "; LastKey: " + lastKey);
			if(lastKey != firstKey + 1 || (pos + size) % BLOCK_LENGTH != 0) {
				for (int index = firstKey + 1; index <= lastKey; index++) {
					if(blockTable.containsKey((Integer) index)) {
						currentBlock = getAndVerifyHashBlock(blockTable.remove(index),_previousBlockTable.remove(index)).getBytes();
					} else {
						currentBlock = new byte[BLOCK_LENGTH];
					}
					
					System.out.println("[FS_Write] Pos: " + posContent + "; index: " + index + "; size: " + size);
					posContent = copyContent(0, size, currentBlock, contents, posContent);
	
					System.out.println("[FS_Write] "+Arrays.toString(currentBlock));
					blockList.add(currentBlock);
				}
			}
			
			BlockId blockId;
			int index = firstKey;
			for(byte[] block : blockList) {
				blockId = blockServer.put_h(new HashBlock(block));
				blockTable.put((Integer) index, blockId);
				_previousBlockTable.put((Integer) index, blockId);
				index++;
			}

			sendIndexBlocks(blockTable);
		} catch (RemoteException |
				 NotBoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException |
				 BlockNotFoundException |
				 TamperedBlockException |
				 InvalidKeySpecException e)  {
			throw new FileSystemException("[FS_write]: " + e.getMessage());
		}
	}

	public int FS_read(byte[] id, int pos, int size, byte[] contents)
			throws FileSystemException {
		//Throws FileSystemException
		checkSize(size, contents);
		checkInit();

		_bytesRead = 0;
		try {
			IBlockServer blockServer = getBlockServer();
			Map<Integer, BlockId> blockTable = getBlockTable(new BlockId(id));
			
			byte[] currentBlock;
			int firstKey = (int) Math.floor((double) (pos / BLOCK_LENGTH)),
			    lastKey = (int) Math.ceil((double) ((pos + size) / BLOCK_LENGTH)),
			    posContent = 0;
			
			if(blockTable.containsKey((Integer) firstKey)) {
				try{
					currentBlock = blockServer.get(blockTable.remove(firstKey)).getBytes();
				}catch(BlockNotFoundException e){
					throw new FileSystemException(null);
				}
			} else {
				currentBlock = new byte[BLOCK_LENGTH];
			}
			
			posContent = extractContent(pos % BLOCK_LENGTH, size, currentBlock, contents, posContent);

			System.out.println("[FS_Read]: "+Arrays.toString(currentBlock));
			System.out.println("[FS_Read]: "+Arrays.toString(contents));
			
			if(lastKey != firstKey + 1 || (pos + size) % BLOCK_LENGTH != 0) {
				for (int index = firstKey + 1; index <= lastKey; index++) {
					if(blockTable.containsKey((Integer) index)) {
						currentBlock = getAndVerifyHashBlock(blockTable.remove(index), _previousBlockTable.get(index)).getBytes();
					} else {
						System.out.print("new block");
						currentBlock = new byte[BLOCK_LENGTH];
					}
					
					System.out.println("Pos: " + posContent);
					posContent = extractContent(0, size, currentBlock, contents, posContent);
	
					System.out.println(Arrays.toString(currentBlock));
					System.out.println(Arrays.toString(contents));
				}
			}

			return _bytesRead;
		} catch (RemoteException |
				 NotBoundException |
				 BlockNotFoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException |
				 TamperedBlockException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

  	
  	
  	
  	
  	
  	
  	
  	
  	
  	
  	
  	//Testing methods
	public void tamperAttack(){
		this.tamperAttack=true;
	}
	
	public void impersonationAttack(){
		this.impersonate = true;
	}
}
