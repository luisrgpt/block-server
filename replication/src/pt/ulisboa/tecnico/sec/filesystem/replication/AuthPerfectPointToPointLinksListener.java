package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedSignature;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.ReadFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.ValueFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.WriteFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

interface AuthPerfectPointToPointLinksListener {
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock,  EncodedSignature encodedSignature) throws FileSystemException;
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock) throws FileSystemException;
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature) throws FileSystemException;
	void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp) throws FileSystemException;
	void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId) throws FileSystemException;
	void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId) throws FileSystemException;
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature) throws FileSystemException;
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock) throws FileSystemException;
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys) throws FileSystemException;
}
