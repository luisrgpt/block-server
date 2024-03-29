package pt.ulisboa.tecnico.sec.filesystem.common;

import java.io.Serializable;
import java.util.Arrays;

public final class BlockId extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6660886462638825356L;

	public BlockId(byte[] bytes) {
		super(bytes);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(getBytes());
	}
	
	@Override
	public boolean equals(Object object) {
		return (object instanceof BlockId) && equals((BlockId) object);
	}
	
	public boolean equals(BlockId blockId) {
		return Arrays.equals(getBytes(), blockId.getBytes());
	}
}
