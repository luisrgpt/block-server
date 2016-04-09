package pt.ulisboa.tecnico.sec.filesystem;

import java.util.List;

import pt.ulisboa.tecnico.sec.filesystem.exception.FileSystemException;

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
	
	private static void checkContentSize(int size, byte[] contents)
			throws FileSystemException {
		//Check content size
		if (contents.length != size) {
			throw new FileSystemException("Size doesnt't match contents' size.");
		}
	}
	
	public static void FS_init()
			throws FileSystemException {
		//Check parameters
		CheckFileSystemClientNullability();

		//Initialise file system client
		_fileSystemClient = new FileSystemClient();
	}

	public static void FS_write(int pos, int size, byte[] contents)
			throws FileSystemException {
		//Check parameters
		checkArgumentsNonNullability(pos, size, contents);
		checkContentSize(size, contents);
		checkFileSystemClientNonNullability();
		
		_fileSystemClient.writeIntoFileSystemServer(pos, contents);
	}

	public static byte[] FS_read(PublicKey pk, int pos, int size, Integer nRead)
			throws FileSystemException {
		//Check parameters
		checkArgumentsNonNullability(pk, pos, size, nRead);
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