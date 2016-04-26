package pt.ulisboa.tecnico.sec.filesystem.common.exception;

public final class PublicKeyNotStoredException extends FileSystemException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5048839466857224118L;

	public PublicKeyNotStoredException() {
		super("Key server hasn't stored public key.");
	}
}
