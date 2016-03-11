package secfs.app;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Random;

import com.sun.security.ntlm.Client;

import jdk.nashorn.internal.ir.Block;
import secfs.library.BlockServerClient;
import secfs.library.FileSystemException;

public class App {

	public static void main(String[] args) throws InvalidKeyException, SignatureException {
        byte[] aux = "potatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotato".getBytes(),
        	   id,
        	   aux2 = new byte[10];
        int size=0;
		
		BlockServerClient client = new BlockServerClient();
		
		try {
			id = client.FS_init();
			//client.test();
			//System.exit(1);
			byte[] buffer = "test1".getBytes();
			byte[] aux3= new byte[buffer.length];
			
			//TEST1
			System.out.println("Test1: Writting in invalid position: pos < 0");
			
			try{
				client.FS_write(-10, buffer.length, buffer);			
				size = client.FS_read(id, -10, 5, aux2);
				System.out.println(">>>>>>>>>>>>>>Test1 - Failed!");
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println(">>>>>>>>>>>>>>Test1 - Passed!");
			}
			//TEST2
			System.out.println("Test2: Writting in valid position: pos==20");
			
			client.FS_write(20, buffer.length, buffer);
			size = client.FS_read(id, 20, aux3.length, aux3);
			
			if (Arrays.equals(buffer, aux3)){
				System.out.println(">>>>>>>>>>>>>>>Test2 - Passed!");
			}else{
				System.out.println(">>>>>>>>>>>>>>>Test2 - Failed!");
			}
			
			//TEST3
			System.out.println("Test3: Test data overwriting : ");
			
			buffer = new byte[aux3.length];
			//generate random bytes
			new Random().nextBytes(buffer);
			client.FS_write(0, buffer.length, buffer);
			
			new Random().nextBytes(buffer);
			client.FS_write(0, buffer.length, buffer);
			size = client.FS_read(id, 0, aux3.length, aux3);
			
			
			if (Arrays.equals(buffer, aux3)){
				System.out.println(">>>>>>>>>>>>>>>>>>>>Test3 - Passed!");
			}else{
				System.out.println(">>>>>>>>>>>>>>>>>>>Test3 - Failed!");
			}
			
			//TEST4 
			System.out.println("Test4: Test reading from uncreated blocks: ");
			size = client.FS_read(id, 200, aux3.length, aux3);

			
			if(Arrays.equals(aux3, new byte[aux3.length])){
				System.out.println(">>>>>>>>>>>>>>>>>>Test4 - Passed!");
			}else{
				System.out.println(">>>>>>>>>>>>>>>>>>Test4 - Failed!");
			}
			
			System.out.println(">>>>>>>>Security Tests<<<<<<<<<");
			
			//TEST5
			try{
				System.out.println("Test5: Test tamper the blocks when the client sends to server: ");
				client.tamperAttack();
				client.FS_write(100, buffer.length, buffer);
				client.FS_read(id, 100, aux3.length, aux3);
				System.out.println(">>>>>>>>>>>>>>>>>>Test5 Failed!");
			}catch(FileSystemException e){
				System.out.println(">>>>>>>>>>>>>>>>>>Test5 Passed!");
			}
			
			
			
			
		   //TEST6
		   System.out.println("Test7: The server will change the content of some block");
           System.out.println("Test7: The client when retrieves that block should reject it");
           try{
				client.FS_write(200, buffer.length, buffer);
				client.FS_read(id, 200, aux3.length, aux3);
				System.out.println(">>>>>>>>>>>>>>>>>>Test6 Failed!");
			} catch (FileSystemException e) {
				System.out.println(">>>>>>>>>>>>>>>>>>Test6 Passed!");
			} 
            
          //TEST7
			System.out.println("Test6: Impersonation by changing the publicKey of the put_k to someone's else : ");
			try{
				client.impersonationAttack();
				client.FS_write(100, buffer.length, buffer);
				client.FS_read(id, 100, aux3.length, aux3);
				System.out.println(">>>>>>>>>>>>>>>>>>Test7 Failed!");
			} catch (FileSystemException e) {
				System.out.println(">>>>>>>>>>>>>>>>>>Test7 Passed!");
			}
            
			/*client.FS_write(21, aux.length, aux);
			client.FS_write(21, aux.length, aux);
			size = client.FS_read(id, 21, 10, aux2);
			System.out.println(size);*/
			//client.FS_write(2, 5, aux);
			//size = client.FS_read(id, 1, 5, aux);
			//System.out.println(size);
	        //aux[0] = (byte) 0;
	        //aux[1] = (byte) 0;
	        //aux[2] = (byte) 0;
	        //aux[3] = (byte) 0;
	        //aux[4] = (byte) 0;
			//size = client.FS_read(id, 6, 5, aux);
			//System.out.println(size);
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}
}
