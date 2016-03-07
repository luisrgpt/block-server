package secfs.app;

import secfs.library.BlockServerClient;
import secfs.library.FileSystemException;

public class App {

	public static void main(String[] args) {
        byte[] aux = new byte[10],
        	   id,
        	   aux2 = new byte[10];
        int size;
        aux[0] = (byte) 8;
        aux[1] = (byte) 0;
        aux[2] = (byte) 0;
        aux[3] = (byte) 8;
        aux[4] = (byte) 5;
        aux[5] = (byte) 8;
        aux[6] = (byte) 0;
        aux[7] = (byte) 0;
        aux[8] = (byte) 8;
        aux[9] = (byte) 5;
		
		BlockServerClient client = new BlockServerClient();
		try {
			id = client.FS_init();
			client.FS_write(21, 10, aux);
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
