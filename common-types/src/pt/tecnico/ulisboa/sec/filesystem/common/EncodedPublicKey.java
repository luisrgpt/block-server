package pt.tecnico.ulisboa.sec.filesystem.common;

import java.io.Serializable;

public final class EncodedPublicKey extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4782283556942456090L;

	public EncodedPublicKey(byte[] bytes) {
		super(bytes);
	}
}
