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

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;
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

public class BlockServerClient extends RmiNode {

	//Block minimal size ->  4 (64 + 4)*2 = 140 bytes
	private static final int BLOCK_LENGTH = 300;
	
	private BlockId _fileId = null;
	private IBlockServer _blockServer = null;

	private PublicKey _publicKey;
	private PrivateKey _privateKey;

	private Map<Integer, BlockId> _blockTable = null;
	
	private int _bytesRead = 0;

	private IBlockServer getBlockServer()
			throws RemoteException, NotBoundException {
		if (_blockServer == null) {
			Registry registry;
			registry = LocateRegistry.getRegistry();
			_blockServer = (IBlockServer)registry.lookup(IBlockServer.SERVICE_NAME);
		}
		return _blockServer;
	}

	private Map<Integer, BlockId> getBlockTable()
			throws NotBoundException, TamperedBlockException, RemoteException {
		try {
			//Assuming that there is only one client per user
			if(_blockTable != null) {
				return _blockTable;
			}
			_blockTable = new HashMap<>();
			
			System.out.println("ID: " + Arrays.toString(_fileId.getBytes()));
			byte[] block = getBlockServer().get(_fileId).getBytes();
			
			System.out.println("Returned: " + Arrays.toString(block));
			
			BlockId nullPointer = new BlockId(new byte[64]);
			_blockTable.putAll(getBlockTable(block));
			
			while(!_blockTable.get(-1).equals(nullPointer)) {
				System.out.println("IdTable2");
				for (Map.Entry<Integer, BlockId> entry : _blockTable.entrySet()) {
					System.out.println(entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
				}
				System.out.println();
				block = getBlockServer().get(_blockTable.get(-1)).getBytes();
				_blockTable.remove(-1);
				System.out.println("Returned2: " + Arrays.toString(block));
				_blockTable.putAll(getBlockTable(block));
			}
			_blockTable.remove(-1);
			System.out.println("IdTable2");
			for (Map.Entry<Integer, BlockId> entry : _blockTable.entrySet()) {
				System.out.println(entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
			}
			System.out.println();
			
			return _blockTable;
		} catch (BlockNotFoundException e) {
			System.out.println("Creating new file");
			return new HashMap<>();
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
		System.out.println(Arrays.toString(block));
		
		return block;
  	}
  	
  	private Map<Integer, BlockId> getBlockTable(byte[] array) {
  		System.out.println("That's strange: " + Arrays.toString(array));
		Map<Integer, BlockId> blockTable = new HashMap<>();
		byte[] sizeArray = new byte[4];
		System.arraycopy(array, 0, sizeArray, 0, 4);
		_bytesRead += 4;
		System.out.println("alsjflasjfsdkf");

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
			System.out.println(entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
		}
		System.out.println();
		
		return blockTable;
  	}
  	
  	private void sendIndexBlocks(Map<Integer, BlockId> map)
  			throws RemoteException, NotBoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		IBlockServer blockServer = getBlockServer();
  		
  		int size = (int) Math.floor((double) (BLOCK_LENGTH - 4) / 68) - 1,
				mapSize = map.size();
		BlockId blockId = new BlockId(new byte[64]);
		if(size < mapSize) {
			int blockSize = (int) Math.ceil((double) mapSize / size),
				firstKey, lastKey;
			System.out.println("blockSize = " + blockSize +
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
					System.out.println(entry);
				}
				
				for (BlockId entry : valueSubList) {
					System.out.println(Arrays.toString(entry.getBytes()));
				}
				
				byte[] array = getBytes(keySubList, valueSubList);
				if(index > 0) {
					blockId = blockServer.put_h(new HashBlock(array));
				} else {
					//Create key block
					KeyBlock keyBlock = new KeyBlock(array);
				
					//Create encoded signature
		        	SecureRandom secureRandom = new SecureRandom(keyBlock.getBytes());
		        	Signature signature = Signature.getInstance("SHA512withRSA");
					signature.initSign(_privateKey, secureRandom);
					signature.update(keyBlock.getBytes());
					EncodedSignature encodedSignature = new EncodedSignature(signature.sign());
				
					//Create encoded public key
					EncodedPublicKey encodedPublicKey = new EncodedPublicKey(_publicKey.getEncoded());
					blockServer.put_k(keyBlock, encodedSignature, encodedPublicKey);
				}
				
				System.out.println();
			}
		}
  	}
	
	@SuppressWarnings("unused")
	public byte[] FS_init()
			throws FileSystemException {
		System.out.println("Invoking FS_init");
		
		if(BLOCK_LENGTH < 140) {
			throw new FileSystemException("FS_init: Block size is too small (< 140).");
		}

		try {
			//Generate key pair for RSA encryption
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair keys = keyGen.generateKeyPair();
	
			//Set key pair state
			_publicKey = keys.getPublic();
			_privateKey = keys.getPrivate();
	
			//Return public key's sha-1 hash
			MessageDigest messageDigest;
			messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(_publicKey.getEncoded());
			_fileId = new BlockId(messageDigest.digest());
	
			return _fileId.getBytes();
		} catch (NoSuchAlgorithmException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
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
			throw new FileSystemException("FS_write: Size doesnt't match contents' size.");
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

		try {
			IBlockServer blockServer = getBlockServer();
			Map<Integer, BlockId> blockTable = getBlockTable();

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

			System.out.println(Arrays.toString(currentBlock));
			blockList.add(currentBlock);

			System.out.println("FirstKey: " + firstKey + "; LastKey: " + lastKey);
			if(lastKey != firstKey + 1 || (pos + size) % BLOCK_LENGTH != 0) {
				for (int index = firstKey + 1; index <= lastKey; index++) {
					if(blockTable.containsKey((Integer) index)) {
						try {
							currentBlock = blockServer.get(blockTable.remove(index)).getBytes();
						} catch (BlockNotFoundException | TamperedBlockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						currentBlock = new byte[BLOCK_LENGTH];
					}
					
					System.out.println("Pos: " + posContent + "; index: " + index + "; size: " + size);
					posContent = copyContent(0, size, currentBlock, contents, posContent);
	
					System.out.println(Arrays.toString(currentBlock));
					blockList.add(currentBlock);
				}
			}
			
			BlockId blockId;
			int index = firstKey;
			for(byte[] block : blockList) {
				blockId = blockServer.put_h(new HashBlock(block));
				blockTable.put((Integer) index, blockId);
				index++;
			}

			sendIndexBlocks(blockTable);
		} catch (RemoteException |
				 NotBoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException |
				 BlockNotFoundException |
				 TamperedBlockException e)  {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

	public int FS_read(byte[] id, int pos, int size, byte[] contents)
			throws FileSystemException {
		//Throws FileSystemException
		checkSize(size, contents);

		_bytesRead = 0;
		try {
			IBlockServer blockServer = getBlockServer();
			Map<Integer, BlockId> blockTable = getBlockTable();
			
			byte[] currentBlock;
			int firstKey = (int) Math.floor((double) (pos / BLOCK_LENGTH)),
			    lastKey = (int) Math.ceil((double) ((pos + size) / BLOCK_LENGTH)),
			    posContent = 0;
			
			if(blockTable.containsKey((Integer) firstKey)) {
				currentBlock = blockServer.get(blockTable.remove(firstKey)).getBytes();
			} else {
				currentBlock = new byte[BLOCK_LENGTH];
			}
			
			posContent = extractContent(pos % BLOCK_LENGTH, size, currentBlock, contents, posContent);

			System.out.println(Arrays.toString(currentBlock));
			System.out.println(Arrays.toString(contents));
			
			if(lastKey != firstKey + 1 || (pos + size) % BLOCK_LENGTH != 0) {
				for (int index = firstKey + 1; index <= lastKey; index++) {
					if(blockTable.containsKey((Integer) index)) {
						currentBlock = blockServer.get(blockTable.remove(index)).getBytes();
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
				 TamperedBlockException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

  	
  	
  	
  	
  	
  	
  	
  	
  	
  	
  	
  	
	
  	private BlockId createBlockId() throws NoSuchAlgorithmException{
        byte[] aux = new byte[10];
         aux[0] = (byte) 8;
         aux[1] = (byte) 0;
         aux[2] = (byte) 0;
         aux[3] = (byte) 8;
         aux[4] = (byte) 5;
         aux[5] = (byte) 8;
         aux[6] = (byte) 0;
         aux[7] = (byte) 0;
         aux[8] = (byte) 8;
         aux[9] = (byte) 5;
  		
  		//Create block id using block's hash
    	MessageDigest messageDigest;
		messageDigest = MessageDigest.getInstance("SHA-512");
    	messageDigest.update(aux);
    	byte[] hash = messageDigest.digest();
    	return new BlockId(hash);
  	}
  	
	public void test () {
		try {
			Map<Integer, BlockId> test = new HashMap<>();
		
			test.put((Integer) 1, createBlockId());
			test.put((Integer) 2, createBlockId());
			test.put((Integer) 3, createBlockId());
			test.put((Integer) 4, createBlockId());
			test.put((Integer) 5, createBlockId());
			
			sendIndexBlocks(test);
			getBlockTable();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
