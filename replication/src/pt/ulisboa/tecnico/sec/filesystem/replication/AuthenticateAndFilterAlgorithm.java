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

final class AuthenticateAndFilterAlgorithm
		implements AuthPerfectPointToPointLinks,
		StubbornPointToPointLinksListener {
	private AuthPerfectPointToPointLinksListener _authPerfectPointToPointLinksListener;
	private StubbornPointToPointLinks _stubbornPointToPointLinks;
	
	//private int _delivered; //TODO: Fix my type!
	
	AuthenticateAndFilterAlgorithm(ProcessId processes[], ProcessId process, AuthPerfectPointToPointLinksListener authPerfectPointToPointLinksListener)
			throws RemoteException, NotBoundException {
		_authPerfectPointToPointLinksListener = authPerfectPointToPointLinksListener;
		_stubbornPointToPointLinks = new RetransmitForeverAlgorithm(processes, process, this);
		
		//_delivered = 0;
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedMac);
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, hashBlock, encodedSignature, encodedMac);
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, blockId, encodedMac);
	}
	
	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, fileBlock, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys)
			throws FileSystemException {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, hashBlock, encodedSignature);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, encodedPublicKey, encodedSignature);
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, readFlag, readId);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, fileBlock, encodedSignature);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, encodedPublicKeys);
	}
}
