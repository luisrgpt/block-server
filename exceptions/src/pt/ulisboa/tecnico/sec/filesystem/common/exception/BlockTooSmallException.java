package pt.ulisboa.tecnico.sec.filesystem.common.exception;

public final class BlockTooSmallException extends FileSystemException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public BlockTooSmallException() {
		super("Block size is too small (< 140).");
	}
}
