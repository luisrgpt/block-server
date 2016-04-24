package pt.ulisboa.tecnico.sec.filesystem;

import java.util.List;

import pt.tecnico.ulisboa.sec.filesystem.common.EncodedPublicKey;
import pt.tecnico.ulisboa.sec.filesystem.common.FileBlock;

interface OneToNByzantineRegularRegisterListener {
	void onWriteReturn();
	void onReadReturn(FileBlock fileBlock);
	void onReadReturn(List<EncodedPublicKey> encodedPublicKeys);
}
