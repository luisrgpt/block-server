package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedMac;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedSignature;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.ReadFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.ValueFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.WriteFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

interface FairLossPointToPointLinksListener extends Remote {
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock HashBlock, EncodedMac encodedMac) throws FileSystemException, RemoteException;
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac) throws FileSystemException, RemoteException;
}
