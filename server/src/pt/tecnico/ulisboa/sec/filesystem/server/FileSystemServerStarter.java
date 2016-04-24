package pt.tecnico.ulisboa.sec.filesystem.server;

import java.rmi.RemoteException;

import pt.tecnico.ulisboa.sec.filesystem.server.exception.FileSystemServerException;

public final class FileSystemServerStarter {

	private static final char[] password = "Fidelio".toCharArray();
	private static final int portList[] = {1099, 1100, 1101, 1102} ;
	
	public static void main(String[] args)
			throws FileSystemServerException, RemoteException {
		
		for(int i = 0; i<portList.length; i++) {
			System.out.println(portList[i]);
			new FileSystemServer(password, portList[i]);
		}
		System.out.println("File system server is online.");
	}
}
