package pt.ulisboa.tecnico.sec.filesystem.common;

import java.io.Serializable;

public final class EncodedSignature extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7411543246873408494L;

	public EncodedSignature(byte[] bytes) {
		super(bytes);
	}
}
