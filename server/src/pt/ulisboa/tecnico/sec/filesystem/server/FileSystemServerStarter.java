package pt.ulisboa.tecnico.sec.filesystem.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class FileSystemServerStarter {

	private static final char[] password = "Fidelio".toCharArray();
	private static final int portList[] = {1099, 1100, 1101, 1102};
	
	public static void main(String[] args)
			throws FileSystemServerException, RemoteException, NotBoundException {
		
		int length = portList.length;
		System.out.println("Initializing " + length + " file system servers:");
		
		for(int i = 0; i < length; i++) {
			System.out.print(" " + portList[i] + "...");
			new FileSystemServer(password, portList[i]);
			System.out.println(" Done!");
		}
		
		System.out.println("All " + length + " file systems servers have been initialized.");
	}
}
