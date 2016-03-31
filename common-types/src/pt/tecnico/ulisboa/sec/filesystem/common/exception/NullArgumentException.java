package pt.tecnico.ulisboa.sec.filesystem.common.exception;

public final class NullArgumentException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public NullArgumentException() {
		super("Argument is null");
	}
}
