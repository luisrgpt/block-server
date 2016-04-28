package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.tuple.ImmutablePair;
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

final class AuthenticateAndFilterAlgorithm
		implements AuthPerfectPointToPointLinks,
		StubbornPointToPointLinksListener {
	private AuthPerfectPointToPointLinksListener _authPerfectPointToPointLinksListener;
	private StubbornPointToPointLinks _stubbornPointToPointLinks;
	
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, ImmutablePair<KeyBlock, EncodedSignature>>>> _deliveredWriteKeyBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, HashBlock>>> _deliveredWriteHashBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, ImmutablePair<EncodedPublicKey, EncodedSignature>>>> _deliveredWritePublicKey;
	private Set<ImmutablePair<ProcessId, ImmutablePair<AckFlag, Integer>>> _deliveredAck;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ReadFlag, Integer>, BlockId>>> _deliveredReadBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ReadFlag, Integer>>> _deliveredReadPublicKeys;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, ImmutableTriple<Integer, KeyBlock, EncodedSignature>>>> _deliveredValueKeyBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, HashBlock>>> _deliveredValueHashBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>>>>> _deliveredValuePublicKeys;
	
	private String _secretKey;
	
	AuthenticateAndFilterAlgorithm(ProcessId processes[], ProcessId process, AuthPerfectPointToPointLinksListener authPerfectPointToPointLinksListener)
			throws FileSystemException {
		_authPerfectPointToPointLinksListener = authPerfectPointToPointLinksListener;
		_stubbornPointToPointLinks = new RetransmitForeverAlgorithm(processes, process, this);
		
		_deliveredWriteKeyBlock = new HashSet<>();
		_deliveredWriteHashBlock = new HashSet<>();
		_deliveredWritePublicKey = new HashSet<>();
		_deliveredAck = new HashSet<>();
		_deliveredReadBlock = new HashSet<>();
		_deliveredReadPublicKeys = new HashSet<>();
		_deliveredValueKeyBlock = new HashSet<>();
		_deliveredValueHashBlock = new HashSet<>();
		_deliveredValuePublicKeys = new HashSet<>();
		
		_secretKey = "For simplification sake";
	}
	
	public EncodedMac generateHMac(byte[] ... data)
			throws FileSystemException {	
	    try {
	        Mac mac = Mac.getInstance("HmacSHA256");
	        mac.init(new SecretKeySpec(_secretKey.getBytes(), "HmacSHA256"));

	        return new EncodedMac(mac.doFinal(null)); //Too tired :v TODO: Find a way to join all values
		} catch(InvalidKeyException |
	    	    NoSuchAlgorithmException exception) {
	    	throw new FileSystemException(exception.getMessage(), exception);
	    }
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), writeFlag.toString().getBytes(), writeTimeStamp.toString().getBytes(), keyBlock.getBytes(), encodedSignature.getBytes());
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedMac);
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), writeFlag.toString().getBytes(), writeTimeStamp.toString().getBytes(), hashBlock.getBytes());
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, hashBlock, encodedMac);
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), writeFlag.toString().getBytes(), writeTimeStamp.toString().getBytes(), encodedPublicKey.getBytes(), encodedSignature.getBytes());
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), ackFlag.toString().getBytes(), timeStamp.toString().getBytes());
		_stubbornPointToPointLinks.onSend(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), readFlag.toString().getBytes(), readId.toString().getBytes(), blockId.getBytes());
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, blockId, encodedMac);
	}
	
	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), readFlag.toString().getBytes(), readId.toString().getBytes());
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), valueFlag.toString().getBytes(), readId.toString().getBytes(), timeStamp.toString().getBytes(), keyBlock.getBytes(), encodedSignature.getBytes());
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, keyBlock, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), valueFlag.toString().getBytes(), readId.toString().getBytes(), hashBlock.getBytes());
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, hashBlock, encodedMac);
	}
	
	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys)
			throws FileSystemException {
		EncodedMac encodedMac = generateHMac(processId.toString().getBytes(), valueFlag.toString().getBytes(), readId.toString().getBytes(), null); //TODO: Find a way to join all values
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, hashBlock);
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
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, keyBlock, encodedSignature);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, hashBlock);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, encodedPublicKeys);
	}
}
