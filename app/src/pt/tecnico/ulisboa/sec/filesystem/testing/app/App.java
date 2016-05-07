package pt.tecnico.ulisboa.sec.filesystem.testing.app;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

import pt.ulisboa.tecnico.sec.filesystem.FileSystem;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;
import pt.ulisboa.tecnico.sec.filesystem.server.FileSystemServerStarter;

public class App {
	
	private static final int INTERVAL = 1000;

	public static void main(String[] args) throws InterruptedException {
        //byte[] aux = "potatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotato".getBytes(),
        //	   id,
        //	   aux2 = new byte[10];
        int size=0;
		int testsPassed=0;
		int totalTestsMade=0;
        
		try {
			//Activate servers using ports 1099, 1100, 1101, 1102
			FileSystemServerStarter.Init();
			
			FileSystemServerStarter.crash(1099);
			//FileSystemServerStarter.crash(1100);
			//FileSystemServerStarter.crash(1101);
			//FileSystemServerStarter.crash(1102);
			PublicKey pk = FileSystem.FS_init();
			//FileSystemServerStarter.recover(1099);
			//FileSystemServerStarter.recover(1100);
			//FileSystemServerStarter.recover(1101);
			//FileSystemServerStarter.recover(1102);

			//Thread.sleep(INTERVAL + 15000);
			
			byte[] buffer = "test1".getBytes();
			byte[] aux3= new byte[buffer.length];
			
			//TEST1
			System.out.println("Test1: Writting in invalid position: pos < 0");
			
			try{
				FileSystem.FS_write(-10, buffer.length, buffer);			
				FileSystem.FS_read(pk, -10, 5, size);
				System.out.println(">>>>>>>>>>>>>>Test1 - Failed!");
			}catch(FileSystemException e){
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
			
			Thread.sleep(INTERVAL);
			
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
			
			Thread.sleep(INTERVAL);
			
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
			
			Thread.sleep(INTERVAL);
			
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
			
			Thread.sleep(INTERVAL);
			
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
           
           Thread.sleep(INTERVAL);
           
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
            
            Thread.sleep(INTERVAL);
            
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
