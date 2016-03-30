package secfs.library;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import secfs.common.exception.BlockNotFoundException;
import secfs.common.exception.InvalidRemoteArgumentException;
import secfs.common.exception.TamperedBlockException;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

final public class FileSystem {
	private static FileSystemClient _fileSystemClient = null;

	private static void checkArgumentsNonNullability(Object ... objects)
			throws FileSystemException {
		for(Object object : objects) {
			if(object == null) {
				throw new FileSystemException("Identified null pointer argument.");
			}
		}
	}
	
	private static void checkContentSize(int size, byte[] contents)
			throws FileSystemException {
		if (contents.length != size) {
			throw new FileSystemException("Size doesnt't match contents' size.");
		}
	}
	
	private static void checkFileSystemClient()
			throws FileSystemException {
		//Check instance initialization
		if(_fileSystemClient == null) {
			throw new FileSystemException("[FS_init]: File system isn't initialized.");
		}
	}

	@SuppressWarnings("unused")
	private static void checkBlocksLength()
			throws FileSystemException {
		//Check block length
		if(FileSystemClient.BLOCK_LENGTH < 140) {
			throw new FileSystemException("[FS_init]: Block size is too small (< 140).");
		}
	}
	
	public static void FS_init()
			throws FileSystemException {
		checkBlocksLength();

		//Check instance initialization
		if(_fileSystemClient != null) {
			throw new FileSystemException("[FS_init]: File system already initialized.");
		}

		try {
			//Initialize file system client
			_fileSystemClient = new FileSystemClient();
			_fileSystemClient.initializeFileSystemClient();
		} catch (Exception exception) {
			//Finish file system client
			_fileSystemClient = null;
			throw new FileSystemException("[FS_init]: " + exception.getMessage());
		}
	}


	public static void FS_write(int pos, int size, byte[] contents)
			throws FileSystemException {
		//Throws NullPointerException
		checkArgumentsNonNullability(pos, size, contents);
		checkContentSize(size, contents);
		checkFileSystemClient();
		
		try {
			//Write into file system
			_fileSystemClient.writeIntoFileSystemServer(pos, size, contents);
		} catch (RemoteException |
				 NotBoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException |
				 BlockNotFoundException |
				 TamperedBlockException |
				 InvalidKeySpecException | InvalidRemoteArgumentException e)  {
			throw new FileSystemException("[FS_write]: " + e.getMessage());
		}
	}

	public static int FS_read(byte[] id, int pos, int size, byte[] contents)
			throws FileSystemException {
  		//Check arguments non-nullability
		//Throws NullPointerException
		checkArgumentsNonNullability(id, pos, size, contents);
		
		//Throws FileSystemException
		checkContentSize(size, contents);
		checkFileSystemClient();

		
		try {
			return _fileSystemClient.readFromFileSystemServer(id, pos, size, contents);
		} catch (RemoteException |
				 NotBoundException |
				 BlockNotFoundException |
				 NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException |
				 TamperedBlockException | InvalidRemoteArgumentException e) {
			throw new FileSystemException("FS_write: " + e.getMessage());
		}
	}

  	
  	public static List<PublicKey> FS_list()
  			throws FileSystemException{
  		//Throws FileSystemException
  		checkFileSystemClient();
  		
  		List<PublicKey> = _fileSystemClient.getPublicKeysFromFileSystemServer();
  		return _fileSystemClient.getPublicKeysFromFileSystemServer();
  	}

  	
  	public static void exit()
  			throws FileSystemException{
  		//Throws FileSystemException
  		checkFileSystemClient();
  		
  		_fileSystemClient.exitCc_Auth();
  	}
  	
  	
  	//Testing methods
	public static void tamperAttack(){
		AttackFlag.activateTamperingFlag();
	}
	
	public static void impersonationAttack(){
		AttackFlag.activateImpersonationFlag();
	}
}