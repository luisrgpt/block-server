package pt.ulisboa.tecnico.sec.filesystem.server;

import java.io.File;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.Constant;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedSignature;
import pt.ulisboa.tecnico.sec.filesystem.common.FileBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.HashBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.KeyBlock;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessId;
import pt.ulisboa.tecnico.sec.filesystem.common.ProcessType;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.BlockNotFoundException;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.BlockTooSmallException;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.FileSystemException;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.InvalidRemoteArgumentException;
import pt.ulisboa.tecnico.sec.filesystem.common.exception.NullArgumentException;
import pt.ulisboa.tecnico.sec.filesystem.replication.AuthenticatedDataByzantineQuorumAlgorithm;
import pt.ulisboa.tecnico.sec.filesystem.replication.IReplicationServer;
import pt.ulisboa.tecnico.sec.filesystem.replication.OneToNByzantineRegularRegister;


final class FileSystemServer {
	
	private FileSystemServerImpl _fileSystemServerImpl;
	
	
	FileSystemServer(char[] password, int port)
			throws FileSystemException {
		_fileSystemServerImpl = new FileSystemServerImpl(password, port);
	}
	
	public void connect(ProcessId processId)
			throws FileSystemException {
		_fileSystemServerImpl.connect(processId);
	}
	
	public void disconnect(ProcessId processId)
			throws FileSystemException {
		_fileSystemServerImpl.disconnect(processId);
	}
	
	public void impersonate(ProcessId processId){
		_fileSystemServerImpl.impersonate(processId);
	}
	
	public void tamper(ProcessId processId) {
		_fileSystemServerImpl.tamper(processId);
	}
	
	private class FileSystemServerImpl
			implements IReplicationServer {
		private OneToNByzantineRegularRegister _oneToNByzantineRegularRegister;
		
		private final int portList[] = {1099, 1100, 1101, 1102};
		private ProcessId _processId;
		//Constants
		private static final String POLICY_FILE_NAME  = "/security.policy",
				                    //KEY_STORE_NAME    = "keyStoreName",
				                    FILE_NAME         = "FileSystemServer.class";//,
				                    //CERTIFICATE_ALIAS = "secretKeyAlias";
		
		private boolean impersonateAttack = false;
		private boolean tamperAttack = false;
		
		//Server attributes
		private Map<EncodedPublicKey, Map<BlockId, ImmutableTriple<Integer, KeyBlock, EncodedSignature>>> _keyBlockDataBase;
		private Map<BlockId, ImmutablePair<Integer, HashBlock>> _hashBlockDataBase;
		private Map<ProcessId, ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> _publicKeyDataBase;
		
		private FileSystemServerImpl(char[] password, int port)
				throws FileSystemException {
			try {
				//Checking parameters
				checkArgumentsNonNullability(password, port);
				checkBlocksSize();
				
				ProcessId processes[] = new ProcessId[portList.length];
				for(int index = 0; index < portList.length; ++index) {
					processes[index] = new ProcessId(portList[index], ProcessType.SERVER);
				}
				
				_oneToNByzantineRegularRegister = new AuthenticatedDataByzantineQuorumAlgorithm(processes, new ProcessId(port, ProcessType.SERVER), this);
				
				//Set security policy
				System.setProperty(
						"java.security.policy",
						new File(FileSystemServer.class.getResource(FILE_NAME).toURI())
								.getAbsoluteFile()
								.getParentFile()
								.getAbsolutePath() + POLICY_FILE_NAME);
				if(System.getSecurityManager() == null) {
		            System.setSecurityManager(new SecurityManager());
		        }
				
				//Initialise server attributes
				_keyBlockDataBase = new HashMap<>();
				_hashBlockDataBase = new HashMap<>();
				_publicKeyDataBase = new HashMap<>();
			} catch (BlockTooSmallException |
					 URISyntaxException |
					 NullArgumentException exception) {
				throw new FileSystemServerException("[" + port + "@main]: " + exception.getMessage(), exception);
			}
	    }
		
		public void tamper(ProcessId processId) {
			//clear previous attacks
			impersonateAttack = false;
			
			_processId=processId;
			this.tamperAttack=true;
		}

		public void impersonate(ProcessId processId) {
			tamperAttack = false;
			
			_processId=processId;
			this.impersonateAttack=true;
		}

		public void connect(ProcessId processId)
				throws FileSystemException {
			_oneToNByzantineRegularRegister.connect(processId);
		}
		
		public void disconnect(ProcessId processId)
				throws FileSystemException {
			_oneToNByzantineRegularRegister.disconnect(processId);
		}
	    
		@SuppressWarnings("unused")
		private void checkBlocksSize()
				throws BlockTooSmallException {
			//Check block size
			if(Constant.BLOCK_LENGTH < 140) {
				throw new BlockTooSmallException();
			}
		}
	    
		private void checkArgumentsNonNullability(Object ... objects)
				throws NullArgumentException {
			//Check arguments non nullability
			for(Object object : objects) {
				if(object == null) {
					throw new NullArgumentException();
				}
			}
		}
		
		private BlockId createBlockId(byte[] bytes)
				throws NoSuchAlgorithmException, InvalidRemoteArgumentException{
			//Create block id using block's hash
	    	MessageDigest messageDigest;
			messageDigest = MessageDigest.getInstance("SHA-512");
	    	messageDigest.update(bytes);
	    	byte[] hash = messageDigest.digest();
	    	return new BlockId(hash);
	  	}
	
		@Override
	  	public ImmutableTriple<Integer, FileBlock, EncodedSignature> get(ProcessId processId, BlockId blockId)
	  			throws FileSystemException {
	  		//Check parameters
	  		checkArgumentsNonNullability(blockId);
  			
	  		EncodedPublicKey encodedPublicKey = _publicKeyDataBase.get(processId).getMiddle();
	  		
	  		//Check if block exists on key block database
	  		BlockId currentBlockId;
	    	for (Entry<BlockId, ImmutableTriple<Integer, KeyBlock, EncodedSignature>> entry : _keyBlockDataBase.get(encodedPublicKey).entrySet()) {
	    		currentBlockId = entry.getKey();
	    		if(currentBlockId.hashCode() == blockId.hashCode()) {
	    	  		ImmutableTriple<Integer, KeyBlock, EncodedSignature> triple = entry.getValue();
    				return new ImmutableTriple<Integer, FileBlock, EncodedSignature>(triple.getLeft(), triple.getMiddle(), triple.getRight());
	    		}
	    	}
	  		
	  		//Check if block exists on hash block database
	  		for (Entry<BlockId, ImmutablePair<Integer, HashBlock>> entry : _hashBlockDataBase.entrySet()) {
	    		currentBlockId = entry.getKey();
	    		if(currentBlockId.hashCode() == blockId.hashCode()) {
	    			ImmutablePair<Integer, HashBlock> pair = entry.getValue();
    				return new ImmutableTriple<Integer, FileBlock, EncodedSignature>(pair.getLeft(), pair.getRight(), null);
	    		}
	    	}
	  		
	  		throw new BlockNotFoundException();
	  		
	    }
	
		@Override
	  	public BlockId put_k(ProcessId processId, Integer timeStamp, KeyBlock keyBlock, EncodedSignature encodedSignature)
	   			throws FileSystemException {
			try {
	   			//Check parameters
		   		checkArgumentsNonNullability(processId, timeStamp, keyBlock, encodedSignature);
	
		   		EncodedPublicKey encodedPublicKey;
	        	//Put key block into database using public key's hash
		   		if(impersonateAttack){
		   			encodedPublicKey = _publicKeyDataBase.get(_processId).getMiddle();
		   		}else{
		   			encodedPublicKey = _publicKeyDataBase.get(processId).getMiddle();
		   		}
		   		
	        	Map<BlockId, ImmutableTriple<Integer, KeyBlock, EncodedSignature>> processDatabase = _keyBlockDataBase.remove(encodedPublicKey);
	        	BlockId blockId = createBlockId(encodedPublicKey.getBytes());
	        	boolean blockExists = false;
	
	      		//Replace block if block exists
	        	for (Entry<BlockId, ImmutableTriple<Integer, KeyBlock, EncodedSignature>> entry : processDatabase.entrySet()) {
	        		if(entry.getKey().hashCode() == blockId.hashCode()) {
	        			processDatabase.remove(entry.getKey());
	        			processDatabase.put(entry.getKey(), new ImmutableTriple<Integer, KeyBlock, EncodedSignature>(timeStamp, keyBlock, encodedSignature));
	        			_keyBlockDataBase.put(encodedPublicKey, processDatabase);
	        			
	        			blockExists = true;
	        			break;
	        		}
	        	}
	
	        	//Else put block into new slot
	        	if(!blockExists) {
	        		processDatabase.put(blockId, new ImmutableTriple<Integer, KeyBlock, EncodedSignature>(timeStamp, keyBlock, encodedSignature));
	        		_keyBlockDataBase.put(encodedPublicKey, processDatabase);
	        	}
	
	        	//Return block id
				return blockId;
			} catch (NoSuchAlgorithmException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
	    }
	
		@Override
	  	public BlockId put_h(ProcessId processId, Integer timeStamp, HashBlock hashBlock)
	    		throws FileSystemException {
			try {
		   		//Check parameters
		    	checkArgumentsNonNullability(hashBlock);
	        	
	        	//Put hash block into database using hash block's hash
		    	BlockId blockId = createBlockId(hashBlock.getBytes());
	        	boolean blockExists = false;
	        	
	        	if(tamperAttack){
	        		hashBlock = new HashBlock("hashblockAttack".getBytes());
		   		}
	        	
	      		//Replace block if block exists
	        	for (Entry<BlockId, ImmutablePair<Integer, HashBlock>> entry : _hashBlockDataBase.entrySet()) {
	        		if(entry.getKey().hashCode() == blockId.hashCode()) {
	        			_hashBlockDataBase.remove(entry.getKey());
	        			_hashBlockDataBase.put(entry.getKey(), new ImmutablePair<Integer, HashBlock>(timeStamp, hashBlock));
	        			
	        			blockExists = true;
	        			break;
	        		}
	        	}
	        	//Else put block into new slot
	        	if(!blockExists) {
	        		_hashBlockDataBase.put(blockId, new ImmutablePair<Integer, HashBlock>(timeStamp, hashBlock));
	        	}
	        	
	        	//Return block id
				return blockId;
			} catch (NoSuchAlgorithmException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
	    }
	
		@Override
	  	public BlockId storePubKey(ProcessId processId, Integer timeStamp, EncodedPublicKey encodedPublicKey, EncodedSignature encodedSignature)
	    		throws FileSystemException {
			try {
				//Check parameters
		    	checkArgumentsNonNullability(timeStamp, encodedPublicKey, encodedSignature);
				
				for(ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature> value : _publicKeyDataBase.values()){
					if(Arrays.equals(encodedPublicKey.getBytes(), value.getMiddle().getBytes())){
						_publicKeyDataBase.remove(value);
					}
				}
				_publicKeyDataBase.put(processId, new ImmutableTriple<>(timeStamp, encodedPublicKey, encodedSignature));
				_keyBlockDataBase.put(encodedPublicKey, new HashMap<>());
				
				//Return block id
				return createBlockId(encodedPublicKey.getBytes());
			} catch (NoSuchAlgorithmException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
		}
	
		@Override
	  	public Set<ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> readPubKeys()
				throws FileSystemException {
			
			//Get public keys from key store
			return new HashSet<>(_publicKeyDataBase.values());
		}
	   	
	   	//private void serverAttack(){
	    //	canAttack=true;
	    //}
	}

	
}