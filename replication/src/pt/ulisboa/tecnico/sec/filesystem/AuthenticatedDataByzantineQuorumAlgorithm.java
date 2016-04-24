package pt.ulisboa.tecnico.sec.filesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.tecnico.ulisboa.sec.filesystem.common.AckFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.BlockId;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedPublicKey;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedSignature;
import pt.tecnico.ulisboa.sec.filesystem.common.FileBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.IReplicationServer;
import pt.tecnico.ulisboa.sec.filesystem.common.KeyBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.ProcessId;
import pt.tecnico.ulisboa.sec.filesystem.common.ReadFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.ValueFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.WriteFlag;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

final class AuthenticatedDataByzantineQuorumAlgorithm
		implements OneToNByzantineRegularRegister,
		AuthPerfectPointToPointLinksListener {
	private OneToNByzantineRegularRegisterListener _oneToNByzantineRegularRegisterListener;
	private AuthPerfectPointToPointLinks _authPerfectPointToPointLinks;
	
	private IReplicationServer _iReplicationServer;
	
	private ImmutableTriple<Integer, KeyBlock, EncodedSignature> _tsvalsigma;
	private int _writeTimeStamp;
	private Map<ProcessId, AckFlag> _ackList;
	private int _readId;
	private Map<ProcessId, ImmutablePair<Integer, KeyBlock>> _readList;
	private Set<ProcessId> _processes;
	
	AuthenticatedDataByzantineQuorumAlgorithm(OneToNByzantineRegularRegisterListener oneToNByzantineRegularRegisterListener) {
		_oneToNByzantineRegularRegisterListener = oneToNByzantineRegularRegisterListener;
		_authPerfectPointToPointLinks = new AuthenticateAndFilterAlgorithm(this);
		
		_tsvalsigma = new ImmutableTriple<>(0, null, null);
		_writeTimeStamp = 0;
		_ackList = new HashMap<>();
		_readId = 0;
		_readList = new HashMap<>();
		_processes = new HashSet<>();
	}
	
	AuthenticatedDataByzantineQuorumAlgorithm(IReplicationServer iReplicationServer) {
		_iReplicationServer = iReplicationServer;
		_authPerfectPointToPointLinks = new AuthenticateAndFilterAlgorithm(this);
		
		_tsvalsigma = new ImmutableTriple<>(0, null, null);
		_writeTimeStamp = 0;
		_ackList = new HashMap<>();
		_readId = 0;
		_readList = new HashMap<>();
		_processes = new HashSet<>();
	}
	
	public void onWrite(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey) {
		++_writeTimeStamp;
		_ackList = new HashMap<>();
		//TODO: Know how to make BONRR signature
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, keyBlock, encodedSignature, encodedPublicKey);
		}
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey) {
		//TODO: Implement me!
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp) {
		//TODO: Implement me!
		_oneToNByzantineRegularRegisterListener.onWriteReturn();
	}
	
	public void onRead(BlockId blockId) {
		++_readId;
		_readList = new HashMap<>();
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new ReadFlag(), _readId, blockId);
		}
	}
	
	public void onRead() {
		++_readId;
		_readList = new HashMap<>();
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new ReadFlag(), _readId);
		}
	}
	
	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId) {
		_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, _tsvalsigma.getLeft(), _tsvalsigma.getMiddle());
	}
	
	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId) {
		_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, 0, (List<EncodedPublicKey>) new ArrayList<EncodedPublicKey>()); //TODO: Fix me!
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock) {
		//TODO: Implement me!
		_oneToNByzantineRegularRegisterListener.onReadReturn(fileBlock);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys) {
		//TODO: Implement me!
		_oneToNByzantineRegularRegisterListener.onReadReturn(encodedPublicKeys);
	}
}
