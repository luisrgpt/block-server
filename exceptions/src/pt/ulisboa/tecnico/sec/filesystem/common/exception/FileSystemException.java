package pt.ulisboa.tecnico.sec.filesystem.common.exception;

public class FileSystemException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7111471454956121744L;

	public FileSystemException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FileSystemException(String message) {
		super(message);
	}
}
