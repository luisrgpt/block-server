package pt.ulisboa.tecnico.sec.filesystem.common;

import java.io.Serializable;

public final class HashBlock extends FileBlock implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6660886462638825356L;

	public HashBlock(byte[] bytes) {
		super(bytes);
	}
}
