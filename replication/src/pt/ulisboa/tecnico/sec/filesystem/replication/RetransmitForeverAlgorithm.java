package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
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

final class RetransmitForeverAlgorithm
		implements StubbornPointToPointLinks,
		FairLossPointToPointLinksListener {
	private StubbornPointToPointLinksListener _stubbornPointToPointLinksListener;
	private FairLossPointToPointLinks _fairLossPointToPointLinks;
	
	//private int _sent; //TODO: Fix my type!
	
	RetransmitForeverAlgorithm(ProcessId processes[], ProcessId process, StubbornPointToPointLinksListener stubbornPointToPointLinksListener)
			throws RemoteException, NotBoundException {
		_stubbornPointToPointLinksListener = stubbornPointToPointLinksListener;
		_fairLossPointToPointLinks = new RmiNode(processes, process, this);
		
		//_sent = 0;
		//TODO: Execute order 66!... I mean, startTimer :v
	}
	
	//TODO: Implement startTimer!
	
	//TODO: Implement onTimeout!
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, hashBlock, encodedSignature, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, encodedPublicKey, encodedSignature, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, ackFlag, timeStamp, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, readFlag, readId, blockId, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, readFlag, readId, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}
	
	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, fileBlock, encodedSignature, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		try {
			_fairLossPointToPointLinks.onSend(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
		} catch (RemoteException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, hashBlock, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, readFlag, readId, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, fileBlock, encodedSignature, encodedMac);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_stubbornPointToPointLinksListener.onDeliver(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
	}
}
