package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.Constant;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedMac;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedSignature;
import pt.ulisboa.tecnico.sec.filesystem.common.FileBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.ReadFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.ValueFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.WriteFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

final class RmiNode extends UnicastRemoteObject
		implements FairLossPointToPointLinks {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2384133903371962259L;

	private FairLossPointToPointLinksListener _fairLossPointToPointLinksListener;
	private Map<ProcessId, FairLossPointToPointLinks> _serverFairLossPointToPointLinks,
	                                                  _clientFairLossPointToPointLinks;
	private ProcessId _process;
	
	RmiNode(ProcessId processes[], ProcessId process, FairLossPointToPointLinksListener fairLossPointToPointLinksListener)
			throws RemoteException {
		_fairLossPointToPointLinksListener = fairLossPointToPointLinksListener;
		
		_serverFairLossPointToPointLinks = new HashMap<>();
		_clientFairLossPointToPointLinks = new HashMap<>();
		
		_process = process;
		
    	//Submit file system server
		LocateRegistry
			.createRegistry(process.getPort())
			.rebind(Constant.SERVICE_NAME, (FairLossPointToPointLinks) this);

		//Naming.rebind(Constant.SERVICE_NAME, blockServer);
		
		for (ProcessId currentProcess : processes) {
			try {
				FairLossPointToPointLinks remoteProcess =
						(FairLossPointToPointLinks) LocateRegistry
							.getRegistry(currentProcess.getPort())
							.lookup(Constant.SERVICE_NAME);
				
				//Bind a RMI connection to BlockServer
				_serverFairLossPointToPointLinks.put(
						currentProcess,
						remoteProcess);

				if(process.equals(currentProcess)) {
					return;
				}

				remoteProcess.onSubmit(process);
			} catch (RemoteException |
					 NotBoundException exception) {
				System.out.print(" " + currentProcess.toString() + " is offline...");
			}
		}
	}
	
	public void onSubmit(ProcessId process)
			throws RemoteException, NotBoundException {
		FairLossPointToPointLinks remoteProcess =
				(FairLossPointToPointLinks) LocateRegistry
					.getRegistry(process.getPort())
					.lookup(Constant.SERVICE_NAME);

		if(process.isServer()) {
			//Bind a RMI connection to BlockServer
			_serverFairLossPointToPointLinks.put(
					process,
					remoteProcess);
		} else {
			//Bind a RMI connection to BlockServer
			_clientFairLossPointToPointLinks.put(
					process,
					remoteProcess);
		}
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_serverFairLossPointToPointLinks.get(processId).onDeliver(_process, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_serverFairLossPointToPointLinks.get(processId).onDeliver(_process, writeFlag, writeTimeStamp, hashBlock, encodedSignature, encodedMac);
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_serverFairLossPointToPointLinks.get(processId).onDeliver(_process, writeFlag, writeTimeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}
	
	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_clientFairLossPointToPointLinks.get(processId).onDeliver(_process, ackFlag, timeStamp, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_serverFairLossPointToPointLinks.get(processId).onDeliver(_process, readFlag, readId, blockId, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_serverFairLossPointToPointLinks.get(processId).onDeliver(_process, readFlag, readId, encodedMac);
	}
	
	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_clientFairLossPointToPointLinks.get(processId).onDeliver(_process, valueFlag, readId, encodedPublicKeys, encodedMac);
	}
	
	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		_clientFairLossPointToPointLinks.get(processId).onDeliver(_process, valueFlag, readId, timeStamp, fileBlock, encodedSignature, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, hashBlock, encodedSignature, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, readFlag, readId, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature , EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, fileBlock, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException, RemoteException {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
	}
}
