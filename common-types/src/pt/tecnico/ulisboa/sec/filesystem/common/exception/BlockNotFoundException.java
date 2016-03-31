package pt.tecnico.ulisboa.sec.filesystem.common.exception;

public final class BlockNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3497298698143236123L;

	public BlockNotFoundException() {
		super("Block not found");
	}
}
