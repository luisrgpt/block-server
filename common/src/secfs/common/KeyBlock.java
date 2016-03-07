package secfs.common;

import java.io.Serializable;
import java.util.Map;

public final class KeyBlock extends FileBlock implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2317974027660935620L;

	public KeyBlock(byte[] bytes) {
		super(bytes);
	}
	
	public KeyBlock(byte[] bytes, Map<Integer, BlockId> blockTable) {
		super(bytes, blockTable);
	}
}
