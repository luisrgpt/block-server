package pt.ulisboa.tecnico.sec.filesystem.common.exception;

public final class BlockNotFoundException extends FileSystemException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public BlockNotFoundException() {
		super("Block not found");
	}
}
