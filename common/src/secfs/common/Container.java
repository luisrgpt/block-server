package secfs.common;

import java.io.Serializable;

public abstract class Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7889865214141979292L;

	private byte[] _bytes;
	
	protected Container(byte[] bytes) {
		_bytes = bytes;
	}
	
	public byte[] getBytes() {
		return _bytes;
	}
}
