package pt.ulisboa.tecnico.sec.filesystem;

import pt.tecnico.ulisboa.sec.filesystem.common.BlockId;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedPublicKey;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedSignature;
import pt.tecnico.ulisboa.sec.filesystem.common.KeyBlock;

interface OneToNByzantineRegularRegister {
	void onWrite(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey);
	void onRead(BlockId blockId);
	void onRead();
}
