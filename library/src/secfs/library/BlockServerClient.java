package secfs.library;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import secfs.common.IBlockServer;
import secfs.common.RmiNode;

public class BlockServerClient extends RmiNode {
	
  	public String FS_init() {
  	    System.out.println("Invoking FS_init");
        return "";
     }

   	public void FS_write(int pos, int size, byte[] contents) {
        System.out.println("Invoking FS_write");
    }

   	public byte[] FS_read(String id, int pos, int size, byte[] contents) {
        System.out.println("Invoking FS_read");
        byte[] aux = new byte[2];
        aux[0] = (byte) 0;
        aux[1] = (byte) 0;
        return aux;
    }
	
    public void test() {
    	setSecurityParameters(); 		

        IBlockServer blockServer;
        try {	      	
        	Registry registry = LocateRegistry.getRegistry();
        	blockServer = (IBlockServer)registry.lookup(IBlockServer.SERVICE_NAME);

 			System.out.println("Found server");
            byte[] aux = new byte[2];
            aux[0] = (byte) 0;
            aux[1] = (byte) 0;
            blockServer.get(0);
            blockServer.put_k(aux, aux, aux);
            blockServer.put_h(aux);
        } catch(RemoteException e) {
            System.out.println("BlockServer: " + e.getMessage());
	    } catch(Exception e) {
            System.out.println("Lookup: " + e.getMessage());
        }
    }
}
