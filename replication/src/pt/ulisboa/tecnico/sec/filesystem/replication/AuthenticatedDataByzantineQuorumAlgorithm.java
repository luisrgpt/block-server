package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pt.ulisboa.tecnico.sec.filesystem.authentication.CitizenCardMockupAuthenticator;
import pt.ulisboa.tecnico.sec.filesystem.authentication.IAuthenticator;
import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
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
import pt.ulisboa.tecnico.sec.filesystem.common.exception.TamperedBlockException;
import pt.ulisboa.tecnico.sec.filesystem.logging.FileSystemLogger;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public final class AuthenticatedDataByzantineQuorumAlgorithm
		implements OneToNByzantineRegularRegister,
		AuthPerfectPointToPointLinksListener {
	private OneToNByzantineRegularRegisterListener _oneToNByzantineRegularRegisterListener;
	private AuthPerfectPointToPointLinks _authPerfectPointToPointLinks;
	
	private IReplicationServer _iReplicationServer;
	
	private IAuthenticator _iAuthenticator;
	
	private Integer _writeTimeStamp;
	private Map<ProcessId, Integer> _storageTimeStamps;
	private Map<ProcessId, AckFlag> _ackList;
	private int _readId;
	private Map<ProcessId, ImmutablePair<Integer, KeyBlock>> _readKeyBlockList;
	private Map<ProcessId, Set<ImmutablePair<Integer, EncodedPublicKey>>> _readPublicKeysList;
	private ProcessId _processes[],
			          _process;
	private PublicKey _publicKey;
	private int _numberOfCorrectProcesses, _numberOfFaultyProcesses;
	
	private BroadcastState _broadcastState;
	private enum BroadcastState {
		ON,
		OFF
	}
	
	public AuthenticatedDataByzantineQuorumAlgorithm(ProcessId processes[], ProcessId process, OneToNByzantineRegularRegisterListener oneToNByzantineRegularRegisterListener)
			throws FileSystemException {
		this(processes, process);
		_oneToNByzantineRegularRegisterListener = oneToNByzantineRegularRegisterListener;
	}
	
	public AuthenticatedDataByzantineQuorumAlgorithm(ProcessId processes[], ProcessId process, IReplicationServer iReplicationServer)
			throws FileSystemException {
		this(processes, process);
		_iReplicationServer = iReplicationServer;
	}
	
	private AuthenticatedDataByzantineQuorumAlgorithm(ProcessId processes[], ProcessId process)
			throws FileSystemException {
		_authPerfectPointToPointLinks = new AuthenticateAndFilterAlgorithm(processes, process, this);
		
		_iAuthenticator = new CitizenCardMockupAuthenticator();
		//_iAuthenticator = new CitizenCardAuthenticator();
		
		_storageTimeStamps = new HashMap<>();
		_writeTimeStamp = 0;
		_ackList = new HashMap<>();
		_readId = 0;
		_readKeyBlockList = new HashMap<>();
		_processes = processes;
		_process = process;
		
		_numberOfCorrectProcesses = processes.length;
		_numberOfFaultyProcesses = 0;
		
		_publicKey = _iAuthenticator.getPublicKey();
		
		_broadcastState = BroadcastState.OFF;
	}
	
	public void onWrite(KeyBlock keyBlock)
			throws FileSystemException {
		FileSystemLogger.logDescription(
				"Writting Key block from replicated file system using Block ID:" +
				System.getProperty("line.separator") +
				keyBlock.toString());
		
		EncodedSignature encodedSignature = initWrite(keyBlock.getBytes());
		for(ProcessId process : _processes) {
			//if(_broadcastState == BroadcastState.OFF) {
			//	return;
			//}
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, keyBlock, encodedSignature);
		}
	}
	
	public void onWrite(HashBlock hashBlock)
			throws FileSystemException {
		FileSystemLogger.logDescription(
				"Writting Hash block from replicated file system using Block ID:" +
				System.getProperty("line.separator") +
				hashBlock.toString());
		
		initWrite();
		for(ProcessId process : _processes) {
			//if(_broadcastState == BroadcastState.OFF) {
			//	return;
			//}
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, hashBlock);
		}
	}
	
	public EncodedPublicKey onWrite()
			throws FileSystemException {
		EncodedPublicKey encodedPublicKey = new EncodedPublicKey(_publicKey.toString().getBytes());
		
		FileSystemLogger.logDescription(
				"Writting Public Key from replicated file system using Block ID:" +
				System.getProperty("line.separator") +
				encodedPublicKey.toString());
		
		EncodedSignature encodedSignature = initWrite(encodedPublicKey.getBytes());
		for(ProcessId process : _processes) {
			//if(_broadcastState == BroadcastState.OFF) {
			//	return;
			//}
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, encodedPublicKey, encodedSignature);
		}
		
		return encodedPublicKey;
	}
	
	private EncodedSignature initWrite(byte[] data) {
		initWrite();
		
		byte[] classData = AuthenticatedDataByzantineQuorumAlgorithm.class.getName().getBytes(),
			   selfData = _publicKey.toString().getBytes(),
			   flagData = WriteFlag.class.getName().getBytes(),
			   timeStampData = _writeTimeStamp.toString().getBytes();
		
		int classLength = classData.length,
			selfLength = selfData.length,
			flagLength = flagData.length,
			timeStampLength = timeStampData.length,
			dataLength = data.length;
		
		byte[] content = new byte[classLength + selfLength + flagLength + timeStampLength + dataLength];
    	System.arraycopy(classData, 0, content, 0, classLength);
    	System.arraycopy(selfData, 0, content, classLength, selfLength);    	
    	System.arraycopy(flagData, 0, content, classLength + selfLength, flagLength);
    	System.arraycopy(timeStampData, 0, content, classLength + selfLength + flagLength, timeStampLength);
    	System.arraycopy(data, 0, content, classLength + selfLength + flagLength + timeStampLength, dataLength);
    	
		//Create encoded signature
		return new EncodedSignature(_iAuthenticator.signData(content));
	}
	
	private void initWrite() {
		_broadcastState = BroadcastState.ON;
		++_writeTimeStamp;
		_ackList = new HashMap<>();
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		/*if(!processId.equals(_process)) {
			return;
		}*/
		BlockId blockId = null;

		Integer storageTimeStamp = _storageTimeStamps.remove(processId);
		if(timeStamp > storageTimeStamp) {
			_storageTimeStamps.put(processId, timeStamp);
			blockId = _iReplicationServer.put_k(processId, timeStamp, keyBlock, encodedSignature);
			
			FileSystemLogger.logDescription(
					"Writted Public Key into server with port " + _process.toString() + ":" +
					System.getProperty("line.separator") +
					keyBlock.toString());
		}
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock)
			throws FileSystemException {
		/*if(!processId.equals(_process)) {
			return;
		}*/
		BlockId blockId = null;

		blockId = _iReplicationServer.put_h(processId, timeStamp, hashBlock);
		
		FileSystemLogger.logDescription(
				"Writted Hash Block into server with port " + _process.toString() + ":" +
				System.getProperty("line.separator") +
				hashBlock.toString());
		
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature)
			throws FileSystemException {
		/*if(!processId.equals(_process)) {
			return;
		}*/
		BlockId blockId = null;
		_storageTimeStamps.put(processId, 0);
		
		Integer storageTimeStamp = _storageTimeStamps.remove(processId);
		if(timeStamp > storageTimeStamp) {
			_storageTimeStamps.put(processId, timeStamp);
			blockId = _iReplicationServer.storePubKey(processId, timeStamp, encodedPublicKey, encodedSignature);
			
			FileSystemLogger.logDescription(
					"Writted Public Key into server with port " + _process.toString() + ":" +
					System.getProperty("line.separator") +
					encodedPublicKey.toString());
		}
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp)
			throws FileSystemException {
		if(!timeStamp.equals(_writeTimeStamp)) {
			return;
		}
		
		FileSystemLogger.logDescription(
				"Acked request " + timeStamp.toString() + " into server with port " + processId.toString() + ".");

		FileSystemLogger.log("Writted on process " + processId.toString());
		_ackList.put(processId, ackFlag);
		if(_ackList.size() > (_numberOfCorrectProcesses + _numberOfFaultyProcesses) / 2) {
			_ackList = new HashMap<>();
			_oneToNByzantineRegularRegisterListener.onWriteReturn(ackFlag);
			_broadcastState = BroadcastState.OFF;
		}
	}
	
	public void onRead(BlockId blockId)
			throws FileSystemException {
		FileSystemLogger.logDescription(
				"Reading File Block from replicated file system using Block ID:" +
				System.getProperty("line.separator") +
				blockId.toString());
		
		initRead();
		for(ProcessId process : _processes) {
			//if(_broadcastState == BroadcastState.OFF) {
			//	return;
			//}
			_authPerfectPointToPointLinks.onSend(process, new ReadFlag(), _readId, blockId);
		}
	}
	
	public void onRead()
			throws FileSystemException {
		FileSystemLogger.logDescription("Reading Public Keys from replicated file system.");
		
		initRead();
		for(ProcessId process : _processes) {
			//if(_broadcastState == BroadcastState.OFF) {
			//	return;
			//}
			_authPerfectPointToPointLinks.onSend(process, new ReadFlag(), _readId);
		}
	}
	
	private void initRead() {
		_broadcastState = BroadcastState.ON;
		++_readId;
		_readKeyBlockList = new HashMap<>();
	}
	
	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId)
			throws FileSystemException {
		ImmutableTriple<Integer, FileBlock, EncodedSignature> tuple = _iReplicationServer.get(processId, blockId);
		EncodedSignature encodedSignature = tuple.getRight();
		if(encodedSignature == null) {
			HashBlock hashBlock = (HashBlock) tuple.getMiddle();
			
			FileSystemLogger.logDescription(
					"Sending Hash Block from server with port " + _process.toString() + ":" +
					System.getProperty("line.separator") +
					hashBlock.toString());
			
			_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, hashBlock);
		} else {
			KeyBlock keyBlock = (KeyBlock) tuple.getMiddle();
			
			FileSystemLogger.logDescription(
					"Sending Key Block from server with port " + _process.toString() + ":" +
					System.getProperty("line.separator") +
					keyBlock.toString());
			
			_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, tuple.getLeft(), keyBlock, encodedSignature);
		}
	}
	
	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId)
			throws FileSystemException {
		Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys = _iReplicationServer.readPubKeys();
		
		FileSystemLogger.logDescription(
				"Sending Public Keys from server with port " + _process.toString() + ".");
		
		_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, encodedPublicKeys);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		if(readId != _readId) {
			return;
		}
		verifySign(timeStamp, keyBlock, encodedSignature);
    	
    	_readKeyBlockList.put(processId, new ImmutablePair<>(timeStamp, keyBlock));
    	if(_readKeyBlockList.size() > (_numberOfCorrectProcesses + _numberOfFaultyProcesses) / 2) {
    		FileBlock finalFileBlock = highestValue(new HashSet<>(_readKeyBlockList.values()));
    		_readKeyBlockList = new HashMap<>();
    		_oneToNByzantineRegularRegisterListener.onReadReturn(finalFileBlock);
    		_broadcastState = BroadcastState.OFF;
    		
    		FileSystemLogger.logDescription(
    				"Readed Key Block from replicated file system:" +
    				System.getProperty("line.separator") +
    				keyBlock.toString());
		}
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock)
			throws FileSystemException {
		if(!readId.equals(_readId)) {
			return;
		}
		_oneToNByzantineRegularRegisterListener.onReadReturn(hashBlock);
		_broadcastState = BroadcastState.OFF;
		
		FileSystemLogger.logDescription(
				"Readed Hash Block from replicated file system:" +
				System.getProperty("line.separator") +
				hashBlock.toString());
	}

	private void verifySign(Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		try {
			byte[] classData = AuthenticatedDataByzantineQuorumAlgorithm.class.getName().getBytes(),
				   selfData = _publicKey.toString().getBytes(),
				   flagData = WriteFlag.class.getName().getBytes(),
				   timeStampData = timeStamp.toString().getBytes(),
				   fileBlockData = keyBlock.getBytes();
			
			int classLength = classData.length,
				selfLength = selfData.length,
				flagLength = flagData.length,
				timeStampLength = timeStampData.length,
				dataLength = fileBlockData.length;
				
			byte[] content = new byte[classLength + selfLength + flagLength + timeStampLength + dataLength];
			System.arraycopy(classData, 0, content, 0, classLength);
			System.arraycopy(selfData, 0, content, classLength, selfLength);    	
			System.arraycopy(flagData, 0, content, classLength + selfLength, flagLength);
			System.arraycopy(timeStampData, 0, content, classLength + selfLength + flagLength, timeStampLength);
			System.arraycopy(fileBlockData, 0, content, classLength + selfLength + flagLength + timeStampLength, dataLength);
				
			//Verify signature
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(_publicKey);
			signature.update(content);
			if(!signature.verify(encodedSignature.getBytes())) {
				FileSystemLogger.log("Verification failed!");
				throw new TamperedBlockException();
			}
		} catch (NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys)
			throws FileSystemException {
		if(!readId.equals(_readId)) {
			return;
		}
		try {
			Set<ImmutablePair<Integer, EncodedPublicKey>> publicKeyList = new HashSet<>();
			
			Integer timeStamp;
			EncodedPublicKey encodedPublicKey;
			
			byte[] classData,
				   publicKeyData,
				   selfData,
				   flagData,
				   timeStampData,
				   content;
			
			int classLength,
				selfLength,
				flagLength,
				timeStampLength,
				dataLength;
			
			Signature signature;
	    	
			for(ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature> entry : encodedPublicKeys) {
				timeStamp = entry.getLeft();
				encodedPublicKey = entry.getMiddle();
					
				classData = AuthenticatedDataByzantineQuorumAlgorithm.class.getName().getBytes();
				publicKeyData = encodedPublicKey.getBytes();
				selfData = publicKeyData;
				flagData = WriteFlag.class.getName().getBytes();
				timeStampData = timeStamp.toString().getBytes();
				
				classLength = classData.length;
				selfLength = selfData.length;
				flagLength = flagData.length;
				timeStampLength = timeStampData.length;
				dataLength = publicKeyData.length;
				
				content = new byte[classLength + selfLength + flagLength + timeStampLength + dataLength];
		    	System.arraycopy(classData, 0, content, 0, classLength);
		    	System.arraycopy(selfData, 0, content, classLength, selfLength);    	
		    	System.arraycopy(flagData, 0, content, classLength + selfLength, flagLength);
		    	System.arraycopy(timeStampData, 0, content, classLength + selfLength + flagLength, timeStampLength);
		    	System.arraycopy(publicKeyData, 0, content, classLength + selfLength + flagLength + timeStampLength, dataLength);
		    	
				//Verify signature
		    	signature = Signature.getInstance("SHA1withRSA");
		    	signature.initVerify(_publicKey);
		    	signature.update(content);
		    	if(!signature.verify(entry.getRight().getBytes())) {
		    		FileSystemLogger.log("Verification failed!");
		    		throw new TamperedBlockException();
		    	}
		    	
		    	publicKeyList.add(new ImmutablePair<>(timeStamp, encodedPublicKey));
			}
	
	    	_readPublicKeysList.put(processId, publicKeyList);
	    	if(_readKeyBlockList.size() > (_numberOfCorrectProcesses + _numberOfFaultyProcesses) / 2) {
	    		Set<EncodedPublicKey> publicKeys = new HashSet<>();
	    		for(Set<ImmutablePair<Integer, EncodedPublicKey>> set : new HashSet<>(_readPublicKeysList.values())) {
	    			publicKeys.add(highestValue(set));
	    		}
	    		_readKeyBlockList = new HashMap<>();
	    		_oneToNByzantineRegularRegisterListener.onReadReturn(publicKeys);
	    		_broadcastState = BroadcastState.OFF;
	    		
				FileSystemLogger.logDescription(
						"Readed Public Key from replicated file system:" +
						System.getProperty("line.separator") +
						publicKeys.toString());
			}
		} catch (NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}
	
	public <Generic> Generic highestValue(Set<ImmutablePair<Integer, Generic>> set) {
		ImmutablePair<Integer, Generic> max = new ImmutablePair<>(0, null);
		for (ImmutablePair<Integer, Generic> immutablePair : set) {
			if (immutablePair.getLeft() > max.getLeft()) {
	            max = immutablePair;
	        }
	    }
	    return max.getRight();
	}

	@Override
	public void onExit() {
		_iAuthenticator.exit();
	}

	@Override
	public PublicKey getPublicKey() {
		return _publicKey;
	}
}
