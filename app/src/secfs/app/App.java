package secfs.app;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import secfs.library.BlockServerClient;
import secfs.library.FileSystemException;

public class App {

	public static void main(String[] args) {
        byte[] aux = "potatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotato".getBytes(),
        	   id,
        	   aux2 = new byte[10];
        int size=0;
		
		BlockServerClient client = new BlockServerClient();
		try {
			id = client.FS_init();
			//client.test();
			//System.exit(1);
			
			//TEST1
			System.out.println("Test1: Writting in invalid position: pos < 0");
			byte[] buffer = "test1".getBytes();
			try{
				client.FS_write(-10, buffer.length, buffer);			
				size = client.FS_read(id, -10, 5, aux2);
			
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Test1 - Passed!");
			}catch(NoSuchAlgorithmException e){
				System.out.println("Test1 - NoSuchAlgorithm!");
			}
			
			//TEST2
			System.out.println("Test2: Writting in valid position: pos==20");
			
			System.out.println("buffer.length = "+buffer.length);
			client.FS_write(20, buffer.length, buffer);
			byte[] aux3= new byte[buffer.length];
			try {
				size = client.FS_read(id, 20, aux3.length, aux3);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			
			if (Arrays.equals(buffer, aux3)){
				System.out.println("Test2 - Passed!");
			}else{
				System.out.println("Test2 - Failed!");
			}
			
			//TEST3
			
			//generate BLOCK_LENGTH = 300
			buffer = new byte[300];
			new Random().nextBytes(buffer);
			
			
			/*
			client.FS_write(21, aux.length, aux);
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
