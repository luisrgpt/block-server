package secfs.library;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import secfs.common.RmiNode;
import secfs.common.BlockId;
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

	private static final int BLOCK_LENGTH = 5;
	
	private BlockId _fileId;
	private IBlockServer _blockServer = null;

	private PublicKey _publicKey;
	private PrivateKey _privateKey;

	private Map<Integer, BlockId> _blockTable;

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
			throws RemoteException, NotBoundException {
		if (_blockTable == null) {
			IBlockServer blockServer = getBlockServer();
			_blockTable = blockServer.get(_fileId).getBlockTable();
		}
		return _blockTable;
	}
	
	public byte[] FS_init()
			throws FileSystemException {
		System.out.println("Invoking FS_init");

		try {
			//_blockSet = new ArrayList<>();
			_blockTable = new HashMap<>();
			
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
			int firstKey = (int) Math.floor((double) (pos / BLOCK_LENGTH)),
			    lastKey = (int) Math.ceil((double) ((pos + size) / BLOCK_LENGTH)),
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
						currentBlock = blockServer.get(blockTable.remove(index)).getBytes();
					} else {
						currentBlock = new byte[BLOCK_LENGTH];
					}
					
					System.out.println("Pos: " + posContent + "; index: " + index);
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

			//Create key block
			KeyBlock keyBlock = new KeyBlock(null, blockTable);
		
			//Create encoded signature
        	SecureRandom secureRandom = new SecureRandom(_publicKey.getEncoded());
        	Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initSign(_privateKey, secureRandom);
			signature.update(_publicKey.getEncoded());
			EncodedSignature encodedSignature = new EncodedSignature(signature.sign());
		
			//Create encoded public key
			EncodedPublicKey encodedPublicKey = new EncodedPublicKey(_publicKey.getEncoded());
			blockServer.put_k(keyBlock, encodedSignature, encodedPublicKey);
		} catch (RemoteException |
				 NotBoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException e)  {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

	public int FS_read(byte[] id, int pos, int size, byte[] contents)
			throws FileSystemException {
		//Throws FileSystemException
		checkSize(size, contents);

		int min = 0;
		try {
			IBlockServer blockServer = getBlockServer();
			Map<Integer, BlockId> blockTable = getBlockTable();
			
			byte[] currentBlock;
			int firstKey = (int) Math.floor((double) (pos / BLOCK_LENGTH)),
			    lastKey = (int) Math.ceil((double) ((pos + size) / BLOCK_LENGTH)),
			    posContent = 0;
			
			if(blockTable.containsKey((Integer) firstKey)) {
				currentBlock = blockServer.get(blockTable.remove(firstKey)).getBytes();
				min += BLOCK_LENGTH;
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
						min += BLOCK_LENGTH;
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

			return min;
		} catch (RemoteException |
				 NotBoundException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}
}
