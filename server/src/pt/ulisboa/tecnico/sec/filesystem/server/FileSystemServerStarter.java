package pt.ulisboa.tecnico.sec.filesystem.server;

import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;
import pt.ulisboa.tecnico.sec.filesystem.logging.FileSystemLogger;

public final class FileSystemServerStarter {

	private static final char[] password = "Fidelio".toCharArray();
	private static final int portList[] = {1099, 1100, 1101, 1102};
	
	public static void main(String[] args)
			throws FileSystemException {

		//Initialise log
		FileSystemLogger.initLogger(FileSystemServerStarter.class);
		
		int length = portList.length;
		FileSystemLogger.log("Initializing " + length + " file system servers:");
		
		for(int i = 0; i < length; i++) {
			FileSystemLogger.log("Initializing server with port " + portList[i] + "...");
			new FileSystemServer(password, portList[i]);
			FileSystemLogger.log("Initialized server with port " + portList[i]);
		}
		
		FileSystemLogger.log("InitializingAll " + length + " file system servers!");
	}
}
