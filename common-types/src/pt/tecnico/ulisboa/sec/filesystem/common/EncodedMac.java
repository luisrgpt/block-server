package pt.tecnico.ulisboa.sec.filesystem.common;

import java.io.Serializable;

public final class EncodedMac extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4782283556942456090L;

	public EncodedMac(byte[] bytes) {
		super(bytes);
	}
}
