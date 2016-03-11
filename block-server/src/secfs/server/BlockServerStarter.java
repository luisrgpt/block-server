package secfs.server;

import secfs.common.IBlockServer;
import secfs.common.RmiNode;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import java.lang.System;

public class BlockServerStarter extends RmiNode {

	private static int REGISTRY_PORT = 1099;
	
	public static void main(String[] args) {
		setSecurityParameters();
		
        System.out.println("Main OK");
        try{
            IBlockServer blockServer = new BlockServer();
            System.out.println("After create");
            
            System.out.println("Test7: The server will change the content of some block");
            System.out.println("Test7: The client when retrieves that block should reject it");
            

            Registry reg = LocateRegistry.createRegistry(REGISTRY_PORT);
			reg.rebind(IBlockServer.SERVICE_NAME, blockServer);

			//Naming.rebind("BlockServer", blockServer);

            System.out.println("Block server ready");
        }catch(Exception e) {
            System.out.println("Block server main " + e.getMessage());
        }
	}

}
