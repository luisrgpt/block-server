package pt.ulisboa.tecnico.sec.filesystem;

import java.util.List;

import pt.tecnico.ulisboa.sec.filesystem.common.AckFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.BlockId;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedPublicKey;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedSignature;
import pt.tecnico.ulisboa.sec.filesystem.common.FileBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.KeyBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.ProcessId;
import pt.tecnico.ulisboa.sec.filesystem.common.ReadFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.ValueFlag;
import pt.tecnico.ulisboa.sec.filesystem.common.WriteFlag;

interface AuthPerfectPointToPointLinks {
	void onSend(ProcessId processId, WriteFlag writeFlag, Integer writeTimeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey);
	void onSend(ProcessId processId, AckFlag ackFlag, Integer timeStamp);
	void onSend(ProcessId processId, ReadFlag readFlag, Integer readId, BlockId blockId);
	void onSend(ProcessId processId, ReadFlag readFlag, Integer readId);
	void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, FileBlock fileBlock);
	void onSend(ProcessId processId, ValueFlag valueFlag, Integer readId, Integer timeStamp, List<EncodedPublicKey> encodedPublicKeys);
}
