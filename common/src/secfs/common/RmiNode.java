package secfs.common;

import java.io.File;
import java.net.URISyntaxException;

public class RmiNode {
	
	protected static final String POLICY_FILE_NAME = "/security.policy";
	
    protected static void setSecurityParameters() {
		try {
			System.setProperty(
					"java.security.policy",
					new File(RmiNode.class.getResource("RmiNode.class").toURI())
							.getAbsoluteFile()
							.getParentFile()
							.getAbsolutePath() + POLICY_FILE_NAME);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }
}
