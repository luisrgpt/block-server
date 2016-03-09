package secfs.app;

import secfs.library.BlockServerClient;
import secfs.library.FileSystemException;

public class App {

	public static void main(String[] args) {
        byte[] aux = "potatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotatopotato".getBytes(),
        	   id,
        	   aux2 = new byte[10];
        int size;
		
		BlockServerClient client = new BlockServerClient();
		try {
			id = client.FS_init();
			//client.test();
			//System.exit(1);
			client.FS_write(21, aux.length, aux);
			client.FS_write(21, aux.length, aux);
			size = client.FS_read(id, 21, 10, aux2);
			System.out.println(size);
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
