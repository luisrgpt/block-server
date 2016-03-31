package pt.tecnico.ulisboa.sec.filesystem.common;

import java.io.Serializable;

public class FileBlock extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 603541215955814954L;

	public FileBlock(byte[] bytes) {
		super(bytes);
	}
}
