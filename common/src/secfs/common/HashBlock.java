package secfs.common;

import java.io.Serializable;

public final class HashBlock extends FileBlock implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6660886462638825356L;

	protected HashBlock(byte[] bytes) {
		super(bytes);
	}
}
