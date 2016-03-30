package secfs.server;

import secfs.common.IFileSystemServer;
import secfs.common.RmiNode;

import java.rmi.registry.Registry;
import java.security.KeyStoreException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.lang.System;

public final class FileSystemServerStarter extends RmiNode {

	private static int REGISTRY_PORT = 1099;
	
	private static FileSystemServer _blockServer = null;
	
	public static void main(String[] args) {
		setSecurityParameters();
		
        try{
        	//Initialize file system server
            _blockServer = new FileSystemServer();
            _blockServer.initializeFileSystemServer();

            //Submit file system server
            Registry reg = LocateRegistry.createRegistry(REGISTRY_PORT);
			reg.rebind(FileSystemServer.SERVICE_NAME, (IFileSystemServer) _blockServer);

			//Naming.rebind("BlockServer", blockServer);

            System.out.println("Block server ready");
        } catch (KeyStoreException | RemoteException e) {
        	_blockServer = null;
            System.out.println("Block server main " + e.getMessage());
        }
	}

}
