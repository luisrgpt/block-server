package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.AccessException;
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

interface FairLossPointToPointLinks extends FairLossPointToPointLinksListener {
	void onSubmit(ProcessId processId) throws AccessException, RemoteException, NotBoundException;
	void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac) throws FileSystemException, RemoteException;
}