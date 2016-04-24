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

final class RmiNode
		implements FairLossPointToPointLinks,
		FairLossPointToPointLinksListener {
	
	FairLossPointToPointLinksListener _fairLossPointToPointLinksListener,
	                                  _remoteFairLossPointToPointLinksListener;
	
	RmiNode(FairLossPointToPointLinksListener fairLossPointToPointLinksListener) {
		_fairLossPointToPointLinksListener = fairLossPointToPointLinksListener;
		
		//TODO: Connect me to _remoteFairLossPointToPointLinksListener!
	}
	
	public void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey, EncodedMac encodedMac) {
		//TODO: Implement me!
		_remoteFairLossPointToPointLinksListener.onDeliver(processId, writeFlag, writeTimeStamp, keyBlock, encodedSignature, encodedPublicKey, encodedMac);
	}

	public void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac) {
		//TODO: Implement me!
		_remoteFairLossPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac) {
		//TODO: Implement me!
		_remoteFairLossPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId, encodedMac);
	}

	public void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac) {
		//TODO: Implement me!
		_remoteFairLossPointToPointLinksListener.onDeliver(processId, readFlag, readId, encodedMac);
	}
	
	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys, EncodedMac encodedMac) {
		//TODO: Implement me!
		_remoteFairLossPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, encodedPublicKeys, encodedMac);
	}
	
	public void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedMac encodedMac) {
		//TODO: Implement me!
		_remoteFairLossPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, fileBlock, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey, EncodedMac encodedMac) {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, writeFlag, timeStamp, keyBlock, encodedSignature, encodedPublicKey, encodedMac);
	}

	public void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac) {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, ackFlag, timeStamp, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac) {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, readFlag, readId, blockId, encodedMac);
	}

	public void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac) {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, readFlag, readId, encodedMac);
	}
	
	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedMac encodedMac) {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, fileBlock, encodedMac);
	}

	public void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys, EncodedMac encodedMac) {
		//TODO: Implement me!
		_fairLossPointToPointLinksListener.onDeliver(processId, valueFlag, readId, timeStamp, encodedPublicKeys, encodedMac);
	}
}
