package secfs.common.exception;

public final class TamperedBlockException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public TamperedBlockException() {
		super("get: Block has been tampered");
	}
}
