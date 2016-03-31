package pt.tecnico.ulisboa.sec.filesystem.common;

import java.io.Serializable;

public final class KeyBlock extends FileBlock implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2317974027660935620L;

	public KeyBlock(byte[] bytes) {
		super(bytes);
	}
}
