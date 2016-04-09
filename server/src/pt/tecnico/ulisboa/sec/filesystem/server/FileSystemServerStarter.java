package pt.tecnico.ulisboa.sec.filesystem.server;

import java.rmi.RemoteException;

import pt.tecnico.ulisboa.sec.filesystem.server.exception.FileSystemServerException;

public final class FileSystemServerStarter {

	private static final char[] password = "Fidelio".toCharArray();
	
	public static void main(String[] args)
			throws FileSystemServerException, RemoteException {
		new FileSystemServer(password);
		System.out.println("File system server is online.");
	}
}
