package secfs.common;

import java.io.Serializable;
import java.util.Map;

public class FileBlock extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 603541215955814954L;

	protected FileBlock(byte[] bytes) {
		super(bytes);
	}
}
