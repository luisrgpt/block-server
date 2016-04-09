package pt.tecnico.ulisboa.sec.filesystem.testing.app;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ccauth.CC_Auth;
import pt.ulisboa.tecnico.sec.filesystem.FileSystem;
import pt.ulisboa.tecnico.sec.filesystem.exception.FileSystemException;

public class App {

	public static void main(String[] args) {
        byte[] aux = "potatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotato".getBytes(),
        	   id,
        	   aux2 = new byte[10];
        int size=0;
		int testsPassed=0;
		int totalTestsMade=0;
        
		try {
			CC_Auth auth = new CC_Auth();
			PublicKey pk = auth.getPublicKey();
			auth.exit();
			
			FileSystem.FS_init();

			byte[] buffer = "test1".getBytes();
			byte[] aux3= new byte[buffer.length];
			
			//TEST1
			System.out.println("Test1: Writting in invalid position: pos < 0");
			
			try{
				FileSystem.FS_write(-10, buffer.length, buffer);			
				aux2 = FileSystem.FS_read(pk, -10, 5, size);
				System.out.println(">>>>>>>>>>>>>>Test1 - Failed!");
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println(">>>>>>>>>>>>>>Test1 - Passed!");
				testsPassed++;
			}
			totalTestsMade++;
			//TEST2
			System.out.println("Test2: Writting in valid position: pos==20");
			
			FileSystem.FS_write(20, buffer.length, buffer);
			aux3 = FileSystem.FS_read(pk, 20, aux3.length, size);
			
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
			aux3 = FileSystem.FS_read(pk, 0, aux3.length, size);
			
			
			if (Arrays.equals(buffer, aux3)){
				System.out.println(">>>>>>>>>>>>>>>>>>>>Test3 - Passed!");
				testsPassed++;
			}else{
				System.out.println(">>>>>>>>>>>>>>>>>>>Test3 - Failed!");
			}
			totalTestsMade++;
			//TEST4 
			System.out.println("Test4: Test reading from uncreated blocks: ");
			aux3 = FileSystem.FS_read(pk, 200, aux3.length, size);

			
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
				FileSystem.FS_read(pk, 100, aux3.length, size);
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
        	   FileSystem.FS_read(pk, 200, aux3.length, size);
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
				FileSystem.FS_read(pk, 100, aux3.length, size);
				System.out.println(">>>>>>>>>>>>>>>>>>Test7 Failed!");
			} catch (FileSystemException e) {
				System.out.println(">>>>>>>>>>>>>>>>>>Test7 Passed!");
				testsPassed++;
			}
            totalTestsMade++;
            
            System.out.println("<<<<<<Tests for Part2>>>>> : ");
            
            System.out.println("Test8: Try to get someone else file : ");
            
          /*  FileSystem.FS_write(0, buffer.length, buffer);
            FileSystem.FS_read(id, 0, aux3.length, aux3);
            //Initialize a new user to get a file from the previous client
            BlockServerClient client2 = new BlockServerClient();
            byte[] client2_fId;
            
            client2_fId = client2.FS_init();   
            client2.FS_read(id, 0, aux3.length, aux3);
            
            if(Arrays.equals(buffer, aux3)){
            	System.out.println("Test8: Passed!");
            	testsPassed++;
            }else{
            	System.out.println("Test8: Failed!");
            }
            totalTestsMade++;
            
            ArrayList<byte[]> listOfPublicKeys =client2.FS_list();
            
            System.out.println("Test9: Try to get a file that does not exist");
            client2.FS_read("WrongFileId".getBytes(), 0, aux3.length, aux3);
            
            */
            System.out.println("<<<<<<RESULTS>>>>> : "+testsPassed+"/"+totalTestsMade+" tests passed!");
            
            
            
            
            
            
            FileSystem.exit();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}
}
