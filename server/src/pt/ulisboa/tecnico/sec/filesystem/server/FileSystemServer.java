package pt.ulisboa.tecnico.sec.filesystem.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.net.URISyntaxException;

import org.apache.commons.lang3.tuple.ImmutableTriple;


final class FileSystemServer {
	
	FileSystemServer(char[] password, int port)
			throws RemoteException, FileSystemServerException, NotBoundException {
		new FileSystemServerImpl(password, port);
	}
	
	private class FileSystemServerImpl
			implements IReplicationServer {
		private final int portList[] = {1099, 1100, 1101, 1102};
		
		//Constants
		private static final String POLICY_FILE_NAME  = "/security.policy",
				                    //KEY_STORE_NAME    = "keyStoreName",
				                    FILE_NAME         = "FileSystemServer.class";//,
				                    //CERTIFICATE_ALIAS = "secretKeyAlias";
		
		//Attack flags
		//private boolean serverUnderAttack = true,
		//		          canAttack         = false;
		
		//Block server attribute
		private Map<EncodedPublicKey, Map<BlockId, ImmutableTriple<Integer, FileBlock, EncodedSignature>>> _dataBase;
		private Map<BlockId, BlockId> _keyBlockIdTable;
		
		//Key server attribute
		private Map<ProcessId, ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature>> _pkStore;
		
		private FileSystemServerImpl(char[] password, int port)
				throws RemoteException, FileSystemServerException, NotBoundException {
			try {
				//Checking parameters
				checkArgumentsNonNullability(password, port);
				checkBlocksSize();
				
				ProcessId processes[] = new ProcessId[portList.length];
				for(int index = 0; index < portList.length; ++index) {
					processes[index] = new ProcessId(portList[index], ProcessType.SERVER);
				}
				
				new AuthenticatedDataByzantineQuorumAlgorithm(processes, new ProcessId(port, ProcessType.SERVER), this);
				
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
				
				//Initialise block server attributes
				_dataBase = new HashMap<>();
				_keyBlockIdTable = new HashMap<>();
				
				//Initialise key server attribute 
				_pkStore = new HashMap<>();
			} catch (BlockTooSmallException |
					 URISyntaxException |
					 IOException |
					 NullArgumentException exception) {
				throw new FileSystemServerException("[" + port + "@main]: " + exception.getMessage(), exception);
			}
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
  			
	  		//Check if block exists
	  		BlockId currentBlockId;
	    	for (Entry<BlockId, ImmutableTriple<Integer, FileBlock, EncodedSignature>> entry : _dataBase.get(_pkStore.get(processId).getMiddle()).entrySet()) {
	    		currentBlockId = entry.getKey();
	    		if(currentBlockId.hashCode() == blockId.hashCode()) {		
    				return entry.getValue();
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
	
	        	//Put key block into database using public key's hash
		   		EncodedPublicKey encodedPublicKey = _pkStore.get(processId).getMiddle(); 
	        	Map<BlockId, ImmutableTriple<Integer, FileBlock, EncodedSignature>> processDatabase = _dataBase.remove(encodedPublicKey);
	        	BlockId blockId = createBlockId(encodedPublicKey.getBytes());
	        	boolean blockExists = false;
	
	      		//Replace block if block exists
	        	for (Entry<BlockId, ImmutableTriple<Integer, FileBlock, EncodedSignature>> entry : processDatabase.entrySet()) {
	        		if(entry.getKey().hashCode() == blockId.hashCode()) {
	        			processDatabase.remove(entry.getKey());
	        			processDatabase.put(entry.getKey(), new ImmutableTriple<Integer, FileBlock, EncodedSignature>(timeStamp, keyBlock, encodedSignature));
	        			_dataBase.put(encodedPublicKey, processDatabase);
	        			
	        			_keyBlockIdTable.remove(entry.getKey());
	        			_keyBlockIdTable.put(entry.getKey(), createBlockId(keyBlock.getBytes()));
	        			blockExists = true;
	        			break;
	        		}
	        	}
	
	        	//Else put block into new slot
	        	if(!blockExists) {
	        		processDatabase.put(blockId, new ImmutableTriple<Integer, FileBlock, EncodedSignature>(timeStamp, keyBlock, encodedSignature));
	        		_dataBase.put(encodedPublicKey, processDatabase);
	        		
	        		_keyBlockIdTable.put(blockId, createBlockId(keyBlock.getBytes()));
	        	}
	
	        	System.out.println(Arrays.toString(keyBlock.getBytes()));
	
	        	//Return block id
				return blockId;
			} catch (NoSuchAlgorithmException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
	    }
	
		@Override
	  	public BlockId put_h(ProcessId processId, Integer timeStamp, HashBlock hashBlock, EncodedSignature encodedSignature)
	    		throws FileSystemException {
			try {
		   		//Check parameters
		    	checkArgumentsNonNullability(hashBlock);
	        	
	        	//Put hash block into database using hash block's hash
		    	EncodedPublicKey encodedPublicKey = _pkStore.get(processId).getMiddle(); 
	        	Map<BlockId, ImmutableTriple<Integer, FileBlock, EncodedSignature>> processDatabase = _dataBase.remove(encodedPublicKey);
		    	BlockId blockId = createBlockId(hashBlock.getBytes());
	        	boolean blockExists = false;
	        	
	      		//Replace block if block exists
	        	for (Entry<BlockId, ImmutableTriple<Integer, FileBlock, EncodedSignature>> entry : processDatabase.entrySet()) {
	        		if(entry.getKey().hashCode() == blockId.hashCode()) {
	        			processDatabase.remove(entry.getKey());
	        			processDatabase.put(entry.getKey(), new ImmutableTriple<Integer, FileBlock, EncodedSignature>(timeStamp, hashBlock, encodedSignature));
	        			_dataBase.put(encodedPublicKey, processDatabase);
	        			
	        			blockExists = true;
	        			break;
	        		}
	        	}
	        	//Else put block into new slot
	        	if(!blockExists) {
	        		processDatabase.put(blockId, new ImmutableTriple<Integer, FileBlock, EncodedSignature>(timeStamp, hashBlock, encodedSignature));
	        		_dataBase.put(encodedPublicKey, processDatabase);
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
				
				for(ImmutableTriple<Integer, EncodedPublicKey, EncodedSignature> value : _pkStore.values()){
					if(Arrays.equals(encodedPublicKey.getBytes(), value.getMiddle().getBytes())){
						_pkStore.remove(value);
					}
				}
				_pkStore.put(processId, new ImmutableTriple<>(timeStamp, encodedPublicKey, encodedSignature));
				_dataBase.put(encodedPublicKey, new HashMap<>());
				
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
			return new HashSet<>(_pkStore.values());
		}
	   	
	   	//private void serverAttack(){
	    //	canAttack=true;
	    //}
	}
}