package pt.ulisboa.tecnico.sec.filesystem.common;

import java.io.Serializable;

public final class AckFlag implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2018137700137900094L;
	
	private BlockId _blockId;
	
	public AckFlag(BlockId blockId) {
		_blockId = blockId;
	}
	
	public BlockId getBlockId() {
		return _blockId;
	}
}
