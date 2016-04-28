package pt.ulisboa.tecnico.sec.filesystem.server;

import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

public final class FileSystemServerException extends FileSystemException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7111471454956121744L;

	public FileSystemServerException(String message, Throwable cause) {
		super(message, cause);
	}
}
