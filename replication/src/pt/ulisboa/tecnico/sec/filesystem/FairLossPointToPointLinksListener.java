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

interface FairLossPointToPointLinksListener {
	void onDeliver(ProcessId processId, WriteFlag writeFlag, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey, EncodedMac encodedMac);
	void onDeliver(ProcessId processId, AckFlag ackFlag, Integer timeStamp, EncodedMac encodedMac);
	void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId, EncodedMac encodedMac);
	void onDeliver(ProcessId processId, ReadFlag readFlag, Integer readId, EncodedMac encodedMac);
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock, EncodedMac encodedMac);
	void onDeliver(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys, EncodedMac encodedMac);
}
