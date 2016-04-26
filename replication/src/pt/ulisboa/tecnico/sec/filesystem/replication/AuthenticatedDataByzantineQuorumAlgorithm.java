package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
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
	private Map<ProcessId, ImmutablePair<Integer, FileBlock>> _readFileBlockList;
	private Map<ProcessId, Set<ImmutablePair<Integer, EncodedPublicKey>>> _readPublicKeysList;
	private ProcessId _processes[];
	private PublicKey _publicKey;
	private int _numberOfCorrectProcesses, _numberOfFaultyProcesses;
	
	public AuthenticatedDataByzantineQuorumAlgorithm(ProcessId processes[], ProcessId process, OneToNByzantineRegularRegisterListener oneToNByzantineRegularRegisterListener)
			throws RemoteException, NotBoundException {
		this(processes, process);
		_oneToNByzantineRegularRegisterListener = oneToNByzantineRegularRegisterListener;
	}
	
	public AuthenticatedDataByzantineQuorumAlgorithm(ProcessId processes[], ProcessId process, IReplicationServer iReplicationServer)
			throws RemoteException, NotBoundException {
		this(processes, process);
		_iReplicationServer = iReplicationServer;
	}
	
	private AuthenticatedDataByzantineQuorumAlgorithm(ProcessId processes[], ProcessId process)
			throws RemoteException, NotBoundException {
		_authPerfectPointToPointLinks = new AuthenticateAndFilterAlgorithm(processes, process, this);
		
		_iAuthenticator = new CitizenCardMockupAuthenticator();
		//_iAuthenticator = new CitizenCardAuthenticator();
		
		_storageTimeStamps = new HashMap<>();
		_writeTimeStamp = 0;
		_ackList = new HashMap<>();
		_readId = 0;
		_readFileBlockList = new HashMap<>();
		_processes = processes;
		
		_numberOfCorrectProcesses = processes.length;
		_numberOfFaultyProcesses = 0;
		
		_publicKey = _iAuthenticator.getPublicKey();
	}
	
	public void onWrite(KeyBlock keyBlock)
			throws FileSystemException {
		EncodedSignature encodedSignature = onWrite(keyBlock.getBytes());
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, keyBlock, encodedSignature);
		}
	}
	
	public void onWrite(HashBlock hashBlock)
			throws FileSystemException {
		EncodedSignature encodedSignature = onWrite(hashBlock.getBytes());
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, hashBlock, encodedSignature);
		}
	}
	
	public void onWrite()
			throws FileSystemException {
		byte[] encodedPublicKey = _publicKey.getEncoded();
		EncodedSignature encodedSignature = onWrite(encodedPublicKey);
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new WriteFlag(), _writeTimeStamp, new EncodedPublicKey(encodedPublicKey), encodedSignature);
		}
	}
	
	private EncodedSignature onWrite(byte[] data) {
		++_writeTimeStamp;
		_ackList = new HashMap<>();
		
		byte[] classData = AuthenticatedDataByzantineQuorumAlgorithm.class.getName().getBytes(),
			   selfData = _publicKey.getEncoded(),
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
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		BlockId blockId = null;

		Integer storageTimeStamp = _storageTimeStamps.remove(processId);
		if(timeStamp > storageTimeStamp) {
			_storageTimeStamps.put(processId, timeStamp);
			blockId = _iReplicationServer.put_k(processId, timeStamp, keyBlock, encodedSignature);
		}
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(blockId), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		BlockId blockId = null;

		Integer storageTimeStamp = _storageTimeStamps.remove(processId);
		if(timeStamp > storageTimeStamp) {
			_storageTimeStamps.put(processId, timeStamp);
			blockId = _iReplicationServer.put_h(processId, timeStamp, hashBlock, encodedSignature);
		}
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(blockId), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature)
			throws FileSystemException {
		BlockId blockId = null;
		_storageTimeStamps.put(processId, 0);
		
		Integer storageTimeStamp = _storageTimeStamps.remove(processId);
		if(timeStamp > storageTimeStamp) {
			_storageTimeStamps.put(processId, timeStamp);
			blockId = _iReplicationServer.storePubKey(processId, timeStamp, encodedPublicKey, encodedSignature);
		}
		_authPerfectPointToPointLinks.onSend(processId, new AckFlag(blockId), timeStamp);
	}
	
	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp) {
		_ackList.put(processId, ackFlag);
		if(_ackList.size() > (_numberOfCorrectProcesses + _numberOfFaultyProcesses) / 2) {
			_ackList = new HashMap<>();
			_oneToNByzantineRegularRegisterListener.onWriteReturn(ackFlag);
		}
	}
	
	public void onRead(BlockId blockId)
			throws FileSystemException {
		++_readId;
		_readFileBlockList = new HashMap<>();
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new ReadFlag(), _readId, blockId);
		}
	}
	
	public void onRead()
			throws FileSystemException {
		++_readId;
		_readPublicKeysList = new HashMap<>();
		for(ProcessId process : _processes) {
			_authPerfectPointToPointLinks.onSend(process, new ReadFlag(), _readId);
		}
	}
	
	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId)
			throws FileSystemException {
		ImmutableTriple<Integer, FileBlock, EncodedSignature> tuple = _iReplicationServer.get(processId, blockId);
		_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, tuple.getLeft(), tuple.getMiddle(), tuple.getRight());
	}
	
	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId)
			throws FileSystemException {
		Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys = _iReplicationServer.readPubKeys();
		_authPerfectPointToPointLinks.onSend(processId, new ValueFlag(), readId, encodedPublicKeys);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		try {
			byte[] classData = AuthenticatedDataByzantineQuorumAlgorithm.class.getName().getBytes(),
				   selfData = _publicKey.getEncoded(),
				   flagData = WriteFlag.class.getName().getBytes(),
				   timeStampData = timeStamp.toString().getBytes(),
				   fileBlockData = fileBlock.getBytes();
			
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
	    	signature.update(_iAuthenticator.signData(content));
	    	if(!signature.verify(encodedSignature.getBytes())) {
	    		System.out.println("Verification failed!");
	    		throw new TamperedBlockException();
	    	}
	    	
	    	_readFileBlockList.put(processId, new ImmutablePair<>(timeStamp ,fileBlock));
	    	if(_readFileBlockList.size() > (_numberOfCorrectProcesses + _numberOfFaultyProcesses) / 2) {
	    		FileBlock finalFileBlock = highestValue(new HashSet<>(_readFileBlockList.values()));
	    		_readFileBlockList = new HashMap<>();
	    		_oneToNByzantineRegularRegisterListener.onReadReturn(finalFileBlock);
			}
		} catch (NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys)
			throws FileSystemException {
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
		    	signature.update(entry.getRight().getBytes());
		    	if(!signature.verify(content)) {
		    		System.out.println("Verification failed!");
		    		throw new TamperedBlockException();
		    	}
		    	
		    	publicKeyList.add(new ImmutablePair<>(timeStamp, encodedPublicKey));
			}
	
	    	_readPublicKeysList.put(processId, publicKeyList);
	    	if(_readFileBlockList.size() > (_numberOfCorrectProcesses + _numberOfFaultyProcesses) / 2) {
	    		Set<EncodedPublicKey> publicKeys = new HashSet<>();
	    		for(Set<ImmutablePair<Integer, EncodedPublicKey>> set : new HashSet<>(_readPublicKeysList.values())) {
	    			publicKeys.add(highestValue(set));
	    		}
	    		_readFileBlockList = new HashMap<>();
	    		_oneToNByzantineRegularRegisterListener.onReadReturn(publicKeys);
			}
		} catch (NoSuchAlgorithmException |
				 InvalidKeyException |
				 SignatureException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}
	
	public <Generic> Generic highestValue(Set<ImmutablePair<Integer, Generic>> set) {
		Map<Generic, Integer> map = new HashMap<>();

		Generic generic;
	    for (ImmutablePair<Integer, Generic> immutablePair : set) {
	    	generic = immutablePair.getRight();
	        map.put(generic, map.containsKey(generic) ? map.remove(generic) + 1 : 1);
	    }

	    Entry<Generic, Integer> max = null;
	    for (Entry<Generic, Integer> entry : map.entrySet()) {
	        if (max == null || entry.getValue() > max.getValue()) {
	            max = entry;
	        }
	    }

	    return max.getKey();
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
