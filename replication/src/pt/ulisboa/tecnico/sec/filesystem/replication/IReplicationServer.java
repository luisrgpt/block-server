package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedSignature;
import pt.ulisboa.tecnico.sec.filesystem.common.FileBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

public interface IReplicationServer {
	BlockId storePubKey(ProcessId processId, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature) throws FileSystemException;
	Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> readPubKeys() throws FileSystemException;
	ImmutableTriple<Integer, FileBlock, EncodedSignature> get(ProcessId processId, BlockId blockId) throws FileSystemException;
  	BlockId put_k(ProcessId processId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature) throws FileSystemException;
  	BlockId put_h(ProcessId processId, Integer timeStamp, HashBlock hashBlock) throws FileSystemException;
}