package pt.ulisboa.tecnico.sec.filesystem.common.exception;

public final class NullArgumentException extends FileSystemException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public NullArgumentException() {
		super("Argument is null");
	}
}
