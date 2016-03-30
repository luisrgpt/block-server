package secfs.common;

import java.io.Serializable;

public final class EncodedCertificate extends Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8373968392381899345L;

	public EncodedCertificate(byte[] bytes) {
		super(bytes);
	}
}
