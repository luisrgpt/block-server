package pt.ulisboa.tecnico.sec.filesystem.replication;

import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

interface TestingModule {
	void connect(ProcessId processId) throws FileSystemException;
	void disconnect(ProcessId processId) throws FileSystemException;
}
