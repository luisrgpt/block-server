package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
import pt.ulisboa.tecnico.sec.filesystem.logging.FileSystemLogger;

final class AuthenticateAndFilterAlgorithm
implements AuthPerfectPointToPointLinks,
StubbornPointToPointLinksListener {
	private AuthPerfectPointToPointLinksListener _authPerfectPointToPointLinksListener;
	private StubbornPointToPointLinks _stubbornPointToPointLinks;

	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, ImmutablePair<KeyBlock, EncodedSignature>>>> _deliveredWriteKeyBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, HashBlock>>> _deliveredWriteHashBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, ImmutablePair<EncodedPublicKey, EncodedSignature>>>> _deliveredWritePublicKey;
	private Set<ImmutablePair<ProcessId, ImmutablePair<AckFlag, Integer>>> _deliveredAck;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ReadFlag, Integer>, BlockId>>> _deliveredReadFileBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ReadFlag, Integer>>> _deliveredReadPublicKeys;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, ImmutableTriple<Integer, KeyBlock, EncodedSignature>>>> _deliveredValueKeyBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, HashBlock>>> _deliveredValueHashBlock;
	private Set<ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>>>>> _deliveredValuePublicKeys;

	private Mac _mac;

	AuthenticateAndFilterAlgorithm(ProcessId processes[], ProcessId process, AuthPerfectPointToPointLinksListener authPerfectPointToPointLinksListener)
			throws FileSystemException {
		try {
			_authPerfectPointToPointLinksListener = authPerfectPointToPointLinksListener;
			_stubbornPointToPointLinks = new RetransmitForeverAlgorithm(processes, process, this);
	
			_deliveredWriteKeyBlock = new HashSet<>();
			_deliveredWriteHashBlock = new HashSet<>();
			_deliveredWritePublicKey = new HashSet<>();
			_deliveredAck = new HashSet<>();
			_deliveredReadFileBlock = new HashSet<>();
			_deliveredReadPublicKeys = new HashSet<>();
			_deliveredValueKeyBlock = new HashSet<>();
			_deliveredValueHashBlock = new HashSet<>();
			_deliveredValuePublicKeys = new HashSet<>();

			_mac = Mac.getInstance("HmacSHA256");
			_mac.init(new SecretKeySpec("For simplification sake".getBytes(), "HmacSHA256"));
		} catch (InvalidKeyException |
				 NoSuchAlgorithmException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public EncodedMac authenticate(byte[] ... data)
			throws FileSystemException {	
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			for(byte[] datum : data) {
				outputStream.write( datum );
			}
			
			return new EncodedMac(_mac.doFinal(outputStream.toByteArray()));
		} catch(IOException exception) {
			throw new FileSystemException(exception.getMessage(), exception);
		}
	}

	public boolean verifyAuthentication(EncodedMac encodedMac, byte[] ... data)
			throws FileSystemException {
		return Arrays.equals(authenticate(data).getBytes(), encodedMac.getBytes());
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(writeFlag.toString().getBytes(), writeTimeStamp.toString().getBytes(), keyBlock.getBytes(), encodedSignature.getBytes());
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, HashBlock hashBlock)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(writeFlag.toString().getBytes(), writeTimeStamp.toString().getBytes(), hashBlock.getBytes());
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, hashBlock, encodedMac);
	}

	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(writeFlag.toString().getBytes(), writeTimeStamp.toString().getBytes(), encodedPublicKey.getBytes(), encodedSignature.getBytes());
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, encodedPublicKey, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(ackFlag.toString().getBytes(), timeStamp.toString().getBytes());
		_stubbornPointToPointLinks.onSend(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(readFlag.toString().getBytes(), readId.toString().getBytes(), blockId.getBytes());
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, blockId, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(readFlag.toString().getBytes(), readId.toString().getBytes());
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(valueFlag.toString().getBytes(), readId.toString().getBytes(), timeStamp.toString().getBytes(), keyBlock.getBytes(), encodedSignature.getBytes());
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, keyBlock, encodedSignature, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(valueFlag.toString().getBytes(), readId.toString().getBytes(), hashBlock.getBytes());
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, hashBlock, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys)
			throws FileSystemException {
		EncodedMac encodedMac = authenticate(valueFlag.toString().getBytes(), readId.toString().getBytes(), null); //TODO: Find a way to join all values
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, encodedPublicKeys, encodedMac);
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, ImmutablePair<KeyBlock, EncodedSignature>>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(writeFlag, timeStamp), new ImmutablePair<>(keyBlock, encodedSignature)));
		if(verifyAuthentication(encodedMac, writeFlag.toString().getBytes(), timeStamp.toString().getBytes(), keyBlock.getBytes(), encodedSignature.getBytes()) &&
				!_deliveredWriteKeyBlock.contains(message)) {
			_deliveredWriteKeyBlock.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature);			
		}
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, HashBlock>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(writeFlag, timeStamp), hashBlock));
		if(verifyAuthentication(encodedMac, writeFlag.toString().getBytes(), timeStamp.toString().getBytes(), hashBlock.getBytes()) &&
				!_deliveredWriteHashBlock.contains(message)) {
			_deliveredWriteHashBlock.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, hashBlock);
		}
	}

	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<WriteFlag, Integer>, ImmutablePair<EncodedPublicKey, EncodedSignature>>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(writeFlag, timeStamp), new ImmutablePair<>(encodedPublicKey, encodedSignature)));
		if(verifyAuthentication(encodedMac, writeFlag.toString().getBytes(), timeStamp.toString().getBytes(), encodedPublicKey.getBytes(), encodedSignature.getBytes()) &&
				!_deliveredWritePublicKey.contains(message)) {
			_deliveredWritePublicKey.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, encodedPublicKey, encodedSignature);
		}
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<AckFlag, Integer>> message = new ImmutablePair<>(processId, new ImmutablePair<>(ackFlag, timeStamp));
		if(verifyAuthentication(encodedMac, ackFlag.toString().getBytes(), timeStamp.toString().getBytes()) &&
				!_deliveredAck.contains(message)) {
			_deliveredAck.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp);
		}
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ReadFlag, Integer>, BlockId>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(readFlag, readId), blockId));
		if(verifyAuthentication(encodedMac, readFlag.toString().getBytes(), readId.toString().getBytes(), blockId.getBytes()) &&
				!_deliveredReadFileBlock.contains(message)) {
			_deliveredReadFileBlock.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId);
		}
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ReadFlag, Integer>> message = new ImmutablePair<>(processId, new ImmutablePair<>(readFlag, readId));
		if(verifyAuthentication(encodedMac, readFlag.toString().getBytes(), readId.toString().getBytes()) &&
				!_deliveredReadPublicKeys.contains(message)) {
			_deliveredReadPublicKeys.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, readFlag, readId);
		}
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, ImmutableTriple<Integer, KeyBlock, EncodedSignature>>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(valueFlag, readId), new ImmutableTriple<>(timeStamp, keyBlock, encodedSignature)));
		if(verifyAuthentication(encodedMac, valueFlag.toString().getBytes(), readId.toString().getBytes(), timeStamp.toString().getBytes(), keyBlock.getBytes(), encodedSignature.getBytes()) &&
				!_deliveredValueKeyBlock.contains(message)) {
			_deliveredValueKeyBlock.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, keyBlock, encodedSignature);
		}
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, HashBlock hashBlock, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, HashBlock>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(valueFlag, readId), hashBlock));
		if(verifyAuthentication(encodedMac, valueFlag.toString().getBytes(), readId.toString().getBytes(), hashBlock.getBytes()) &&
				!_deliveredValueHashBlock.contains(message)) {
			_deliveredValueHashBlock.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, hashBlock);
		}
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> encodedPublicKeys, EncodedMac encodedMac)
			throws FileSystemException {
		ImmutablePair<ProcessId, ImmutablePair<ImmutablePair<ValueFlag, Integer>, Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>>>> message = new ImmutablePair<>(processId, new ImmutablePair<>(new ImmutablePair<>(valueFlag, readId), encodedPublicKeys));
		if(verifyAuthentication(encodedMac, valueFlag.toString().getBytes(), readId.toString().getBytes(), null) &&  //TODO: Find a way to join all values
				!_deliveredValuePublicKeys.contains(message)) {
			_deliveredValuePublicKeys.add(message);
			_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, encodedPublicKeys);
		}
	}
}
