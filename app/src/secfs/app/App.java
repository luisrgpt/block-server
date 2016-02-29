package secfs.app;

import secfs.library.BlockServerClient;

public class App {

	public static void main(String[] args) {
        byte[] aux = new byte[2];
        aux[0] = (byte) 0;
        aux[1] = (byte) 0;
		
		BlockServerClient client = new BlockServerClient();
		client.FS_init();
		client.FS_write(0, 0, aux);
		aux = client.FS_read("", 0, 0, aux);
		client.test();
	}

}
