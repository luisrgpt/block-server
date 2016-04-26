package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.util.Set;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.FileBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

public interface OneToNByzantineRegularRegisterListener {
	void onWriteReturn(AckFlag ackFlag);
	void onReadReturn(FileBlock fileBlock) throws FileSystemException;
	void onReadReturn(Set<EncodedPublicKey> encodedPublicKeys) throws FileSystemException;
}
