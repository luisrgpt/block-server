package secfs.library;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import secfs.common.BlockId;
import secfs.common.Encoder;
import secfs.common.IBlockServer;
import secfs.common.RmiNode;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;

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
            
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keys = keyGen.generateKeyPair();
            blockServer.put_k(
            		Encoder.getInstance(aux),
            		Encoder.getInstance(Signature.getInstance("NONEwithRSA"), keys.getPrivate(), aux),
            		Encoder.getInstance(keys.getPublic()));
            BlockId id = blockServer.put_h(Encoder.getInstance(MessageDigest.getInstance("MD5"), aux));
            blockServer.get(id);
        } catch(RemoteException e) {
            System.out.println("BlockServer: " + e.getMessage());
	    } catch(Exception e) {
            System.out.println("Lookup: " + e.getMessage());
        }
    }
}
