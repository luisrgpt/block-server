package secfs.library;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import secfs.common.BlockId;
import secfs.common.EncodedPublicKey;
import secfs.common.EncodedSignature;
import secfs.common.HashBlock;
import secfs.common.IBlockServer;
import secfs.common.KeyBlock;
import secfs.common.RmiNode;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
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
            
            KeyBlock keyBlock = new KeyBlock(aux);
            
            Signature signature = Signature.getInstance("NONEwithRSA");
            signature.initSign(keys.getPrivate()); //TODO: add random value
    		signature.update(aux);
    		EncodedSignature encodedSignature = new EncodedSignature(signature.sign());
    		
    		EncodedPublicKey encodedPublicKey = new EncodedPublicKey(keys.getPublic().getEncoded());
    		
    		HashBlock hashBlock = new HashBlock(aux);
    		
    		BlockId id;
            id = blockServer.put_k(keyBlock, encodedSignature, encodedPublicKey);
            blockServer.get(id);
            
            id = blockServer.put_h(hashBlock);
            blockServer.get(id);
        } catch(RemoteException e) {
            System.out.println("BlockServer: " + e.getMessage());
	    } catch(Exception e) {
            System.out.println("Lookup: " + e.getMessage());
        }
    }
}
