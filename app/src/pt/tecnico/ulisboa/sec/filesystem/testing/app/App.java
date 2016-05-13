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

        int size=0;
		int testsPassed=0;
		int totalTestsMade=0;
        
		try {
			//Activate servers using ports 1099, 1100, 1101, 1102
			FileSystemServerStarter.Init();
			
			
			//FileSystemServerStarter.crash(1099);
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
			
			
			System.out.println("####### All instances are correct #######");
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
		
            System.out.println("<<<<<<RESULTS>>>>> : "+testsPassed+"/"+totalTestsMade+" tests passed!");
        
            
            
          
            System.out.println("####### 1 instance is crashed #######");
            FileSystemServerStarter.crash(1099);
            
            totalTestsMade=0;
            testsPassed=0;
            //TEST1
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
			FileSystemServerStarter.recover(1099);
			
			totalTestsMade++;
            System.out.println("<<<<<<RESULTS>>>>> : "+testsPassed+"/"+totalTestsMade+" tests passed!");
            
            
            
            
            
            
            
            System.out.println("####### 1 instance is byzanthine -type 1 #######");
            System.out.println("this attack changes blockId of key blocks upon put_k is executed");
            FileSystemServerStarter.byzantinetypeone(1099);;
            
            totalTestsMade=0;
            testsPassed=0;
            //TEST1
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
            
            System.out.println("<<<<<<RESULTS>>>>> : "+testsPassed+"/"+totalTestsMade+" tests passed!");
            
            
            
            FileSystem.exit();
		} catch (FileSystemException e) {
			e.printStackTrace();
		}
	}
}
