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
import java.util.ArrayList;
import java.util.Arrays;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockServerClient extends RmiNode {

	private byte[] _fileId;
	private IBlockServer _blockServer = null;

	private PublicKey _publicKey;
	private PrivateKey _privateKey;
	
	private List<BlockId> _blockSet;

	private IBlockServer getBlockServer()
			throws RemoteException, NotBoundException {
		if (_blockServer == null) {
			Registry registry;
			registry = LocateRegistry.getRegistry();
			_blockServer = (IBlockServer)registry.lookup(IBlockServer.SERVICE_NAME);
		}
		return _blockServer;
	}

	public byte[] FS_init()
			throws FileSystemException {
		System.out.println("Invoking FS_init");

		try {
			_blockSet = new ArrayList<>();
			
			//Generate key pair for RSA encryption
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair keys = keyGen.generateKeyPair();
	
			//Set key pair state
			_publicKey = keys.getPublic();
			_privateKey = keys.getPrivate();
	
			//Return public key's sha-1 hash
			MessageDigest messageDigest;
			messageDigest = MessageDigest.getInstance("sha1");
			messageDigest.update(_publicKey.getEncoded());
			_fileId = messageDigest.digest();
	
			return _fileId;
		} catch (NoSuchAlgorithmException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

	private int copyBlock(int pos, int size, byte[] newBlock, byte[] oldBlock) {
		int limit = Math.min(pos + size, oldBlock.length);
		for(int index = pos; index < limit; index++) {
			newBlock[index] = oldBlock[index];
		}

		return limit;
	}
	
	private void addZeroes(int limit, int pos, byte[] newBlock) {
		for(int index = limit; index < pos; index++) {
			newBlock[index] = 0;
		}
	}
	
	private void addZeroes(int pos, byte[] newBlock) {
		addZeroes(0, pos, newBlock);
	}
	
	private void copyContent(int pos, int size, byte[] newBlock, byte[] content) {
		for(int indexA = 0, indexB = pos; indexA < size; indexA++, indexB++) {
			newBlock[indexB] = content[indexA];
		}
	}
	
	private void checkSize(int size, byte[] contents)
			throws FileSystemException {
		if (contents.length != size) {
			throw new FileSystemException("FS_write: Size doesnt't match contents' size.");
		}
	}
	
	public void FS_write(int pos, int size, byte[] contents)
			throws FileSystemException {
		//(this phase) if block exists
		//               then get block
		//               else create block
		//create block from max(pos + write, block.length) size
		//get pos position:
		//  while pointer smaller than pos and pointer smaller than block
		//    copy block
		//  while pointer smaller than pos
		//    add zeros
		//  while pointer smaller than pos + size
		//    copy contents
		//  while pointer smaller than block
		//    copy block
		
		//Throws FileSystemException
		checkSize(size, contents);

		byte[] newBlock;
		IBlockServer blockServer;

		try {
			blockServer = getBlockServer();

			if(_blockSet.isEmpty()) {
				newBlock = new byte[pos + size];
				addZeroes(pos, newBlock);
				copyContent(pos, size, newBlock, contents);
			} else {
				byte[] oldBlock;
				int limit;
	
				try {
					oldBlock = blockServer.get(_blockSet.remove(0)).getBytes();
				} catch (RemoteException e)  {
					throw new FileSystemException("FS_write: " + e.getMessage());
				}
				newBlock = new byte[Math.max(pos + size, oldBlock.length)];
				
				limit = copyBlock(0, pos, newBlock, oldBlock);
				addZeroes(limit, pos, newBlock);
				copyContent(pos, size, newBlock, contents);
				copyBlock(pos + size, oldBlock.length, newBlock, oldBlock);
			}
		
			//Create key block
			KeyBlock keyBlock = new KeyBlock(newBlock);
		
			//Create encoded signature
        	SecureRandom secureRandom = new SecureRandom(newBlock);
        	Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initSign(_privateKey, secureRandom);
			signature.update(_publicKey.getEncoded());
			EncodedSignature encodedSignature = new EncodedSignature(signature.sign());
		
			//Create encoded public key
			EncodedPublicKey encodedPublicKey = new EncodedPublicKey(_publicKey.getEncoded());
			_blockSet.add(blockServer.put_k(keyBlock, encodedSignature, encodedPublicKey));
		} catch (RemoteException |
				 NotBoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException e)  {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
		
		System.out.println(Arrays.toString(newBlock));
	}

	public int FS_read(byte[] id, int pos, int size, byte[] contents)
			throws FileSystemException {
		//Throws FileSystemException
		checkSize(size, contents);

		IBlockServer blockServer;
		byte[] block;
		int min;
		try {
			blockServer = getBlockServer();
			block = blockServer.get(_blockSet.get(0)).getBytes();

			min = Math.min(size, block.length - pos);
			for(int indexA = 0, indexB = pos;
				indexA < min;
				indexA++, indexB++) {
				contents[indexA] = block[indexB];
			}

			System.out.println(Arrays.toString(contents));
			return min;
		} catch (RemoteException |
				 NotBoundException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

	public void test() {
    	setSecurityParameters(); 		

        try {	      	
        	System.out.println("Found server");
            byte[] aux = new byte[2];
            aux[0] = (byte) 0;
            aux[1] = (byte) 0;
            
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keys = keyGen.generateKeyPair();
            
            KeyBlock keyBlock = new KeyBlock(aux);
            
            SecureRandom secureRandom = new SecureRandom(aux);
            Signature signature = Signature.getInstance("NONEwithRSA");
            signature.initSign(keys.getPrivate(), secureRandom); //TODO: add random value
    		signature.update(aux);
    		EncodedSignature encodedSignature = new EncodedSignature(signature.sign());
    		
    		EncodedPublicKey encodedPublicKey = new EncodedPublicKey(keys.getPublic().getEncoded());
    		
    		HashBlock hashBlock = new HashBlock(aux);
    		
    		BlockId id;
            id = getBlockServer().put_k(keyBlock, encodedSignature, encodedPublicKey);
            getBlockServer().get(id);
            
            id = getBlockServer().put_h(hashBlock);
            getBlockServer().get(id);
        } catch(RemoteException e) {
            System.out.println("BlockServer: " + e.getMessage());
	    } catch(Exception e) {
            System.out.println("Lookup: " + e.getMessage());
        }
    }
}
