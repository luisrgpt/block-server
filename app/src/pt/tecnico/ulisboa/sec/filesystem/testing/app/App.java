package pt.tecnico.ulisboa.sec.filesystem.testing.app;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Random;

import ccauth.CC_Auth;
import ccauth.IAuth;
import pt.ulisboa.tecnico.sec.filesystem.FileSystem;
import pt.ulisboa.tecnico.sec.filesystem.exception.FileSystemException;

public class App {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws InvalidKeyException, SignatureException {
        byte[] aux = "potatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotato".getBytes(),
        	   id,
        	   aux2 = new byte[10];
        int size=0;
		int testsPassed=0;
		int totalTestsMade=0;
		IAuth auth = new CC_Auth();
        
		try {
			PublicKey pk = auth.getPublicKey();
			
			FileSystem.FS_init();
			//client.test();
			//System.exit(1);
			byte[] buffer = "test1".getBytes();
			byte[] aux3= new byte[buffer.length];
			
			//TEST1
			System.out.println("Test1: Writting in invalid position: pos < 0");
			
			try{
				FileSystem.FS_write(-10, buffer.length, buffer);			
				size = FileSystem.FS_read(pk, -10, 5, aux2);
				System.out.println(">>>>>>>>>>>>>>Test1 - Failed!");
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println(">>>>>>>>>>>>>>Test1 - Passed!");
				testsPassed++;
			}
			totalTestsMade++;
			//TEST2
			System.out.println("Test2: Writting in valid position: pos==20");
			
			FileSystem.FS_write(20, buffer.length, buffer);
			size = FileSystem.FS_read(pk, 20, aux3.length, aux3);
			
			if (Arrays.equals(buffer, aux3)){
				System.out.println(">>>>>>>>>>>>>>>Test2 - Passed!");
				testsPassed++;
			}else{
				System.out.println(">>>>>>>>>>>>>>>Test2 - Failed!");
			}
			totalTestsMade++;
			
			//TEST3
			System.out.println("Test3: Test data overwriting : ");
			
			buffer = new byte[aux3.length];
			//generate random bytes
			new Random().nextBytes(buffer);
			FileSystem.FS_write(0, buffer.length, buffer);
			
			new Random().nextBytes(buffer);
			FileSystem.FS_write(0, buffer.length, buffer);
			size = FileSystem.FS_read(pk, 0, aux3.length, aux3);
			
			
			if (Arrays.equals(buffer, aux3)){
				System.out.println(">>>>>>>>>>>>>>>>>>>>Test3 - Passed!");
				testsPassed++;
			}else{
				System.out.println(">>>>>>>>>>>>>>>>>>>Test3 - Failed!");
			}
			totalTestsMade++;
			//TEST4 
			System.out.println("Test4: Test reading from uncreated blocks: ");
			size = FileSystem.FS_read(pk, 200, aux3.length, aux3);

			
			if(Arrays.equals(aux3, new byte[aux3.length])){
				System.out.println(">>>>>>>>>>>>>>>>>>Test4 - Passed!");
				testsPassed++;
			}else{
				System.out.println(">>>>>>>>>>>>>>>>>>Test4 - Failed!");
			}
			totalTestsMade++;
			
			System.out.println(">>>>>>>>Security Tests<<<<<<<<<");
			
			//TEST5
			try{
				System.out.println("Test5: Test tamper the blocks when the client sends to server: ");
				FileSystem.tamperAttack();
				FileSystem.FS_write(100, buffer.length, buffer);
				FileSystem.FS_read(pk, 100, aux3.length, aux3);
				System.out.println(">>>>>>>>>>>>>>>>>>Test5 Failed!");
			}catch(FileSystemException e){
				System.out.println(">>>>>>>>>>>>>>>>>>Test5 Passed!");
				testsPassed++;
			}
			
			totalTestsMade++;
			
			
			
			
		   //TEST6
		   System.out.println("Test7: The server will change the content of some block");
           System.out.println("Test7: The client when retrieves that block should reject it");
           try{
        	   FileSystem.FS_write(200, buffer.length, buffer);
        	   FileSystem.FS_read(pk, 200, aux3.length, aux3);
				System.out.println(">>>>>>>>>>>>>>>>>>Test6 Failed!");
			} catch (FileSystemException e) {
				System.out.println(">>>>>>>>>>>>>>>>>>Test6 Passed!");
				testsPassed++;
			} 
            
           totalTestsMade++;
          //TEST7
			System.out.println("Test6: Impersonation by changing the publicKey of the put_k to someone's else : ");
			try{
				FileSystem.impersonationAttack();
				FileSystem.FS_write(100, buffer.length, buffer);
				FileSystem.FS_read(pk, 100, aux3.length, aux3);
				System.out.println(">>>>>>>>>>>>>>>>>>Test7 Failed!");
			} catch (FileSystemException e) {
				System.out.println(">>>>>>>>>>>>>>>>>>Test7 Passed!");
				testsPassed++;
			}
            totalTestsMade++;
            
            System.out.println(FileSystem.FS_list().toString());
            
            System.out.println("<<<<<<RESULTS>>>>> : "+testsPassed+"/"+totalTestsMade+" tests passed!");
            
            
            
            FileSystem.exit();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}
}