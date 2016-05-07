package pt.ulisboa.tecnico.sec.filesystem.replication;

import java.security.PublicKey;

import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;

public interface OneToNByzantineRegularRegister extends TestingModule {
	void onWrite(KeyBlock keyBlock) throws FileSystemException;
	void onWrite(HashBlock hashBlock) throws FileSystemException;
	EncodedPublicKey onWrite() throws FileSystemException;
	void onRead(BlockId blockId) throws FileSystemException;
	void onRead() throws FileSystemException;
	void onExit();
	PublicKey getPublicKey();
}
