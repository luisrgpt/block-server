package secfs.common.exception;

public final class PublicKeyNotStoredException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5048839466857224118L;

	public PublicKeyNotStoredException() {
		super("Key server hasn't stored public key.");
	}
}
