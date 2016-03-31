package pt.tecnico.ulisboa.sec.filesystem.common.exception;

public final class BlockTooSmallException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public BlockTooSmallException() {
		super("Block size is too small (< 140).");
	}
}
