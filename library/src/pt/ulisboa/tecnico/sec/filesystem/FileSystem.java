package pt.ulisboa.tecnico.sec.filesystem;

import java.util.List;

import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

import java.security.PublicKey;

public final class FileSystem {

	private static FileSystemClient _fileSystemClient;

	private static void CheckFileSystemClientNullability()
			throws FileSystemException {
		//Check file system client nullability
		if(_fileSystemClient != null) {
			throw new FileSystemException("File system already initialized.");
		}
	}

	private static void checkFileSystemClientNonNullability()
			throws FileSystemException {
		//Check file system client non-nullability
		if(_fileSystemClient == null) {
			throw new FileSystemException("File system isn't initialized.");
		}
	}

	private static void checkArgumentsNonNullability(Object ... objects)
			throws FileSystemException {
		//Check arguments non-nullability
		for(Object object : objects) {
			if(object == null) {
				throw new FileSystemException("Identified null pointer argument.");
			}
		}
	}
	
	private static void checkPositionValue(int pos)
			throws FileSystemException {
		//Check position value
		if(pos < 0) {
			throw new FileSystemException("Position value is negative.");
		}
	}
	
	private static void checkContentSize(int size, byte[] contents)
			throws FileSystemException {
		//Check content size
		if (contents.length != size) {
			throw new FileSystemException("Size doesnt't match contents' size.");
		}
	}
	
	public static PublicKey FS_init()
			throws FileSystemException {
		//Check parameters
		CheckFileSystemClientNullability();

		//Initialise file system client
		_fileSystemClient = new FileSystemClient();
		return _fileSystemClient.getClientPublicKey();
	}

	public static void FS_write(int pos, int size, byte[] contents)
			throws FileSystemException {
		//Check parameters
		checkArgumentsNonNullability(pos, size, contents);
		checkPositionValue(pos);
		checkContentSize(size, contents);
		checkFileSystemClientNonNullability();
		
		_fileSystemClient.writeIntoFileSystemServer(pos, contents);
	}

	public static byte[] FS_read(PublicKey pk, int pos, int size, Integer nRead)
			throws FileSystemException {
		//Check parameters
		checkArgumentsNonNullability(pk, pos, size, nRead);
		checkPositionValue(pos);
		checkFileSystemClientNonNullability();
		
		byte[] result = new byte[size];
		nRead = _fileSystemClient.readFromFileSystemServer(pk, pos, result);

		return result;
	}

  	public static List<PublicKey> FS_list()
  			throws FileSystemException{
  		//Check parameters
  		checkFileSystemClientNonNullability();
  		
  		return _fileSystemClient.getPublicKeysFromFileSystemServer();
  	}

  	public static void exit()
  			throws FileSystemException{
  		//Check parameters
  		checkFileSystemClientNonNullability();
  		
  		_fileSystemClient.exit();
  	}
  	
  	//Testing methods
	public static void tamperAttack(){
		AttackFlag.activateTamperingFlag();
	}
	
	public static void impersonationAttack(){
		AttackFlag.activateImpersonationFlag();
	}
}