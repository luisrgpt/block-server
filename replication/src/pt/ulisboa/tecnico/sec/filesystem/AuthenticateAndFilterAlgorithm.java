package pt.ulisboa.tecnico.sec.filesystem;

import java.util.List;

import pt.tecnico.ulisboa.sec.filesystem.common.AckFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.BlockId;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedMac;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedPublicKey;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedSignature;
import pt.tecnico.ulisboa.sec.filesystem.common.FileBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.KeyBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.ProcessId;
import pt.tecnico.ulisboa.sec.filesystem.common.ReadFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.ValueFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.WriteFlag;

final class AuthenticateAndFilterAlgorithm
		implements AuthPerfectPointToPointLinks,
		StubbornPointToPointLinksListener {
	private AuthPerfectPointToPointLinksListener _authPerfectPointToPointLinksListener;
	private StubbornPointToPointLinks _stubbornPointToPointLinks;
	
	private int _delivered; //TODO: Fix my type!
	
	AuthenticateAndFilterAlgorithm(AuthPerfectPointToPointLinksListener authPerfectPointToPointLinksListener) {
		_authPerfectPointToPointLinksListener = authPerfectPointToPointLinksListener;
		_stubbornPointToPointLinks = new RetransmitForeverAlgorithm(this);
		
		_delivered = 0;
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey) {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedPublicKey, encodedMac);
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp) {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId) {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, blockId, encodedMac);
	}
	
	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId) {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, readFlag, readId, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock) {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, fileBlock, encodedMac);
	}

	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys) {
		//TODO: Implement me!
		EncodedMac encodedMac = null; 
		_stubbornPointToPointLinks.onSend(processId, valueFlag, readId, timeStamp, encodedPublicKeys, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey, EncodedMac encodedMac) {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature, encodedPublicKey);
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac) {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac) {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac) {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, readFlag, readId);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedMac encodedMac) {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, fileBlock);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys, EncodedMac encodedMac) {
		//TODO: Implement me!
		_authPerfectPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, encodedPublicKeys);
	}
}
