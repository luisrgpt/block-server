package pt.ulisboa.tecnico.sec.filesystem.logging;

import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

public class FileSystemLoggerException extends FileSystemException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1493098615172658262L;

	public FileSystemLoggerException(String message) {
		super("Cannot log message\"" + message + ".");
	}
	
	public FileSystemLoggerException(String message, Throwable cause) {
		super(message, cause);
	}
}
