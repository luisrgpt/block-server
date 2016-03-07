package secfs.common;

import java.io.Serializable;
import java.util.Map;

public class FileBlock extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 603541215955814954L;

	
	private Map<Integer, BlockId> _blockTable;
	
	protected FileBlock(byte[] bytes) {
		super(bytes);
	}
	
	protected FileBlock(byte[] bytes, Map<Integer, BlockId> blockTable) {
		this(bytes);
		_blockTable = blockTable;
	}
	
	public Map<Integer, BlockId> getBlockTable() {
		return _blockTable;
	}
}
