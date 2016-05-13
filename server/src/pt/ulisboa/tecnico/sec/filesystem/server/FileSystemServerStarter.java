package pt.ulisboa.tecnico.sec.filesystem.server;

import java.util.Map;
import java.util.HashMap;

import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessType;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;
import pt.ulisboa.tecnico.sec.filesystem.logging.FileSystemLogger;

public final class FileSystemServerStarter {

	private static final char _password[] = "Fidelio".toCharArray();
	private static final int _portList[] = {1099, 1100, 1101, 1102};
	private static Map<Integer, FileSystemServer> _correctProcesses, _byzanthineProcesses;
	
	public static void main(String args[]) throws FileSystemException {
		Init();
	}
	
	public static void Init()
			throws FileSystemException {

		//Initialize log
		FileSystemLogger.initLogger(FileSystemServerStarter.class);
		
		int length = _portList.length;
		_correctProcesses = new HashMap<>(length);
		_byzanthineProcesses = new HashMap<>(length);
		FileSystemLogger.log("Initializing " + length + " file system servers:");
		
		for(int i = 0; i < length; i++) {
			FileSystemLogger.log("Initializing server with port " + _portList[i] + "...");
			_correctProcesses.put(_portList[i], new FileSystemServer(_password, _portList[i]));
			FileSystemLogger.log("Initialized server with port " + _portList[i]);
		}
		
		FileSystemLogger.log("InitializingAll " + length + " file system servers!");
	}
	
	public static void recover(int port)
			throws FileSystemException {
		FileSystemServer byzanthineProcess = _byzanthineProcesses.remove(port);
		byzanthineProcess.connect(new ProcessId(port, ProcessType.SERVER));
		_correctProcesses.put(port, byzanthineProcess);
	}

	public static void crash(int port)
			throws FileSystemException {
		FileSystemServer correctProcess = _correctProcesses.remove(port);
		correctProcess.disconnect(new ProcessId(port, ProcessType.SERVER));
		_byzanthineProcesses.put(port, correctProcess);
	}
	
	public static void byzantinetypeone(int port){
		//This byzantine server changes the keyblock's public key
		FileSystemServer correctProcess = _correctProcesses.remove(port);
		correctProcess.impersonate(new ProcessId(port, ProcessType.SERVER));
		_byzanthineProcesses.put(port, correctProcess);
	}
}
