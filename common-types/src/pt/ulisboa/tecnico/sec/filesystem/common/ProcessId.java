package pt.ulisboa.tecnico.sec.filesystem.common;

import java.io.Serializable;

public final class ProcessId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4787749635605194684L;
	
	private int _port;
	private ProcessType _processType;
	
	public ProcessId(int port, ProcessType processType) {
		_port = port;
		_processType = processType;
	}
	
	public int getPort() {
		return _port;
	}
	
	public boolean isServer() {
		return _processType == ProcessType.SERVER;
	}
	
	@Override
	public String toString() {
		return Integer.toString(_port);
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof ProcessId) {
			return equals((ProcessId) object);
		} else {
			return false;
		}
	}
	
	public boolean equals(ProcessId processId) {
		return _port == processId.getPort();
	}
	
	@Override
	public int hashCode() {
		return _port;
	}
}
