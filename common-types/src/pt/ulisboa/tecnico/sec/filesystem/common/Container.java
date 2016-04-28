package pt.ulisboa.tecnico.sec.filesystem.common;

import java.io.Serializable;
import java.util.Arrays;

public abstract class Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7889865214141979292L;

	private final byte[] _bytes;
	
	protected Container(byte[] bytes) {
		_bytes = bytes;
	}
	
	public byte[] getBytes() {
		return _bytes;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(_bytes);
	}
}
