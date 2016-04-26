package pt.ulisboa.tecnico.sec.filesystem;

import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.sec.filesystem.common.AckFlag;
import pt.ulisboa.tecnico.sec.filesystem.common.BlockId;
import pt.ulisboa.tecnico.sec.filesystem.common.Constant;
import pt.ulisboa.tecnico.sec.filesystem.common.EncodedPublicKey;
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
import pt.ulisboa.tecnico.sec.filesystem.common.exception.TamperedBlockException;
import pt.ulisboa.tecnico.sec.filesystem.replication.AuthenticatedDataByzantineQuorumAlgorithm;
import pt.ulisboa.tecnico.sec.filesystem.replication.OneToNByzantineRegularRegister;
import pt.ulisboa.tecnico.sec.filesystem.replication.OneToNByzantineRegularRegisterListener;

final class FileSystemClient {
	
	private FileSystemClientImpl _fileSystemClientImpl;
	
	FileSystemClient()
			throws FileSystemException {
		_fileSystemClientImpl = new FileSystemClientImpl();
	}

	void writeIntoFileSystemServer(int pos, byte[] contents)
			throws FileSystemException {
		_fileSystemClientImpl.writeIntoFileSystemServer(pos, contents);
	}

	int readFromFileSystemServer(PublicKey pk, int pos, byte[] contents)
			throws FileSystemException {
		return _fileSystemClientImpl.readFromFileSystemServer(pk, pos, contents);
	}

	List<PublicKey> getPublicKeysFromFileSystemServer()
			throws FileSystemException {
		return _fileSystemClientImpl.getPublicKeysFromFileSystemServer();
	}

	public void exit() {
		_fileSystemClientImpl.exit();
	}

	PublicKey getClientPublicKey(){
		return _fileSystemClientImpl.getPublicKey();
	}
	
	private class FileSystemClientImpl
			implements OneToNByzantineRegularRegisterListener {
		private OneToNByzantineRegularRegister _oneToNByzantineRegularRegister;
		
		private BlockId _fileId;
	
		private Map<Integer, BlockId> _blockTable;
		private Map<Integer, BlockId> _previousBlockTable;
		
		private int _bytesRead;
		
		private final int portList[] = {1099, 1100, 1101, 1102} ;
		private final int port = 1104;
		
		private Set<EncodedPublicKey> _encodedPublicKeys;
		private FileBlock _fileBlock;
		private BlockId _blockId;
	
		private FileSystemClientImpl() 
				throws FileSystemException {
			try {
				//Check parameters
				checkBlocksSize();
				
				ProcessId processes[] = new ProcessId[portList.length];
				for(int index = 0; index < portList.length; ++index) {
					processes[index] = new ProcessId(portList[index], ProcessType.SERVER);
				}
				
				_oneToNByzantineRegularRegister = new AuthenticatedDataByzantineQuorumAlgorithm(processes, new ProcessId(port, ProcessType.CLIENT), this);
				
				//Initialise attributes
				_fileId = null;
				
				_blockTable = new HashMap<>();
				_previousBlockTable = new HashMap<>();
				
				_bytesRead = 0;
				
				_oneToNByzantineRegularRegister.onWrite();
				_fileId = _blockId;
			} catch (RemoteException |
					 NotBoundException |
					 BlockTooSmallException exception) {
				throw new FileSystemException("[FS_init]: " + exception.getMessage(), exception);
			}
		}

		@SuppressWarnings("unused")
		private void checkBlocksSize()
				throws BlockTooSmallException {
			//Check block length
			if(Constant.BLOCK_LENGTH < 140) {
				throw new BlockTooSmallException();
			}
		}

		private void checkArgumentsNonNullability(Object ... objects)
				throws NullArgumentException {
			for(Object object : objects) {
				if(object == null) {
					throw new NullArgumentException();
				}
			}
		}

		private BlockId createBlockId(PublicKey publicKey) throws NoSuchAlgorithmException {
			byte[] encodedDigest = publicKey.toString().getBytes();
			MessageDigest messageDigest;
			messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(encodedDigest);
			return new BlockId(messageDigest.digest());
		}
	
		private Map<Integer, BlockId> getBlockTable(BlockId fileId)
				throws FileSystemException {
			try {
				_blockTable = new HashMap<>();
				
				System.out.println("[getBlockTable] ID: " + Arrays.toString(fileId.getBytes()));
				
				_oneToNByzantineRegularRegister.onRead(fileId);
				byte[] block = _fileBlock.getBytes();
				
				System.out.println("[getBlockTable] Returned: " + Arrays.toString(block));
				
				BlockId nullPointer = new BlockId(new byte[64]);
				_blockTable.putAll(getBlockTable(block));
				
				while(!_blockTable.get(-1).equals(nullPointer)) {
					System.out.println("[getBlockTable] IdTable2");
					for (Map.Entry<Integer, BlockId> entry : _blockTable.entrySet()) {
						System.out.println("[getBlockTable] " + entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
					}
					System.out.println();
					_oneToNByzantineRegularRegister.onRead(_blockTable.get(-1));
					block = _fileBlock.getBytes();
					_blockTable.remove(-1);
					System.out.println("[getBlockTable] Returned2: " + Arrays.toString(block));
					_blockTable.putAll(getBlockTable(block));
				}
				_blockTable.remove(-1);
				System.out.println("[getBlockTable] IdTable3");
				for (Map.Entry<Integer, BlockId> entry : _blockTable.entrySet()) {
					System.out.println("[getBlockTable] Returned3: " + entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
				}
				System.out.println();
				
				return _blockTable;
			} catch (BlockNotFoundException e) {
				System.out.println("[getBlockTable] Block not found -> Creating new file");
				return new HashMap<>();
			}
		}
		
	  	private byte[] getBytes(int i) {
			ByteBuffer dbuf = ByteBuffer.allocate(4);
			dbuf.putInt(i);
			return dbuf.array();
	  	}
	  	
	  	private int getInt(byte[] array) {
			ByteBuffer buffer = ByteBuffer.wrap(array);
			return buffer.getInt();
	  	}
	  	
	  	private byte[] getBytes(List<Integer> keySubList, List<BlockId> valueSubList) {
			byte[] block = new byte[Constant.BLOCK_LENGTH];
			int size = Math.min((int) Math.floor((double) (Constant.BLOCK_LENGTH - 4) / 68), keySubList.size());
			System.arraycopy(getBytes(size), 0, block, 0, 4);
	
			byte[] blockId;
			Integer key;
			for(int index = 0; index < size; index++) {
				key = keySubList.get(index);
				blockId = valueSubList.get(index).getBytes();
	
				System.arraycopy(getBytes(key), 0, block, 4 + 68*index, 4);
				System.arraycopy(blockId, 0, block, 4 + 68*index + 4, 64);
			}
			System.out.println("[getBytes] content: "+ Arrays.toString(block));
			
			return block;
	  	}
	  	
	  	private Map<Integer, BlockId> getBlockTable(byte[] array) {
	  		Map<Integer, BlockId> blockTable = new HashMap<>();
			byte[] sizeArray = new byte[4];
			System.arraycopy(array, 0, sizeArray, 0, 4);
			_bytesRead += 4;
	
			int size = getInt(sizeArray);
			byte[] keyArray = new byte[4], blockId = new byte[64];
			for(int index = 0; index < size; index++) {
				System.arraycopy(array, 4 + 68*index, keyArray, 0, 4);
				_bytesRead += 4;
				System.arraycopy(array, 4 + 68*index + 4, blockId, 0, 64);
				_bytesRead += 64;
	
				blockTable.put((Integer) getInt(keyArray), new BlockId(blockId.clone()));
			}
			
			System.out.println("IdTable2");
			for (Map.Entry<Integer, BlockId> entry : blockTable.entrySet()) {
				System.out.println("[getBlockTable] "+entry.getKey() + " -> " + Arrays.toString(entry.getValue().getBytes()));
			}
			System.out.println();
			
			return blockTable;
	  	}

	  	private void sendIndexBlocks(Map<Integer, BlockId> map)
	  			throws FileSystemException {
	  		int size = (int) Math.floor((double) (Constant.BLOCK_LENGTH - 4) / 68) - 1,
					mapSize = map.size();
			BlockId blockId = new BlockId(new byte[64]);
			//if(size < mapSize) {
				int blockSize = (int) Math.ceil((double) mapSize / size),
					firstKey, lastKey;
				System.out.println("[sendIndexBlocks] blockSize = " + blockSize + " size = " + size + " mapSize = " + mapSize);
				List<Integer> keyList = new ArrayList<Integer>(map.keySet());
				List<BlockId> valueList = new ArrayList<BlockId>(map.values());
				
				System.out.println("[sendIndexBlocks] Key set = " + Arrays.toString(keyList.toArray()));
				for(BlockId value: valueList) {
					System.out.println("[sendIndexBlocks] Value set = " + Arrays.toString(value.getBytes()));
				}
				
				for(int index = Math.min(size, mapSize) - 1; index >= 0; index--) {
					firstKey = blockSize * index;
					lastKey = Math.min(firstKey + blockSize, mapSize);
					
					System.out.println("[sendIndexBlocks] Index     = " + index);
					System.out.println("[sendIndexBlocks] First key = " + firstKey);
					System.out.println("[sendIndexBlocks] Last key  = " + lastKey);
					
					List<Integer> keySubList = new ArrayList<>(keyList.subList(firstKey, lastKey));
					List<BlockId> valueSubList = new ArrayList<>(valueList.subList(firstKey, lastKey));
					
					keySubList.add(-1);
					valueSubList.add(blockId);
					
					for (Integer entry : keySubList) {
						System.out.println("[sendindexBlocks] keySublist: " + entry);
					}
					
					for (BlockId entry : valueSubList) {
						System.out.println("[sendindexBlocks] valueSubList: " + Arrays.toString(entry.getBytes()));
					}
					
					byte[] array = getBytes(keySubList, valueSubList);
					if(index > 0) {
						_oneToNByzantineRegularRegister.onWrite(new HashBlock(array));
						blockId = _blockId;
					} else {
						//Create key block
						KeyBlock keyBlock = new KeyBlock(array);
					
						/*if(AttackFlag.isBeingTampered()){
							//After generate the signature with the data we should change the block content
							System.out.println("Tampering data...");
							keyBlock = new KeyBlock("U were cracked!".getBytes());
							AttackFlag.deactivateTamperingFlag();
						}
						
						if(AttackFlag.isBeingImpersonated()){
							System.out.println("Changing public key...");
							KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
							keyGen.initialize(1024);
							KeyPair keys = keyGen.generateKeyPair();
					
							//Set key pair state
							EncodedPublicKey fakeEncodedPublicKey = new EncodedPublicKey(keys.getPublic().getEncoded());
							encodedPublicKey = fakeEncodedPublicKey;
							AttackFlag.deactivateImpersonationFlag();
						}*/
						
						_oneToNByzantineRegularRegister.onWrite(keyBlock);
					}
					
					System.out.println();
				}
			//}
	  	}
		
		private int copyContent(int pos, int size, byte[] newBlock, byte[] content, int posContent)
				throws FileSystemException {
			try {
				int indexA = posContent;
				for(int indexB = pos,
						limitA = size,
						limitB = newBlock.length;
						indexA < limitA &&
						indexB < limitB;
						indexA++, indexB++) {
					newBlock[indexB] = content[indexA];
				}
				
				return indexA;
			} catch (ArrayIndexOutOfBoundsException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
		}
		
		private int extractContent(int pos, int size, byte[] newBlock, byte[] content, int posContent)
				throws FileSystemException {
			try {
				int indexA = posContent;
				for(int indexB = pos,
						limitA = size,
						limitB = newBlock.length;
						indexA < limitA &&
						indexB < limitB;
						indexA++, indexB++) {
					content[indexA] = newBlock[indexB];
					_bytesRead++;
				}
				
				return indexA;
			} catch (ArrayIndexOutOfBoundsException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
		}
		
		//only applicable to hash blocks
	  	private FileBlock getAndVerifyHashBlock(BlockId blockId, BlockId prevBlockId)
	  			throws FileSystemException {
	  		try {
		  		_oneToNByzantineRegularRegister.onRead(blockId);
		  		FileBlock fileBlock = _fileBlock;
		  		
		    	//Create block id using block's hash
		    	MessageDigest messageDigest;
				messageDigest = MessageDigest.getInstance("SHA-512");
				try{
					messageDigest.update(fileBlock.getBytes());
				}catch( NullPointerException e){
					System.out.println("Could not get the block bytes");
				}
				
		    	byte[] hash = messageDigest.digest();
		    
		    	if(prevBlockId==null || Arrays.equals(hash, prevBlockId.getBytes()) || _previousBlockTable.isEmpty()){
		    		//then return block
		    		return fileBlock;
		    	}else{
		    		//else throw exception
		    		throw new TamperedBlockException();
		    	}
			} catch (NoSuchAlgorithmException exception) {
				throw new FileSystemException(exception.getMessage(), exception);
			}
	  	}
		
		private void writeIntoFileSystemServer(int pos, byte[] contents)
				throws FileSystemException {
			try {
				//Check parameters
				checkArgumentsNonNullability(pos, contents);
				
				System.out.println("[FS_Write] Content: " + Arrays.toString(contents));
				
				int size = contents.length;
				Map<Integer, BlockId> blockTable = getBlockTable(_fileId);
		
				List<byte[]> blockList = new ArrayList<>();
				byte[] currentBlock;
				int firstKey = (int) Math.floor((double) pos / Constant.BLOCK_LENGTH),
				    lastKey = (int) Math.ceil(((double) pos + size) / Constant.BLOCK_LENGTH),
				    posContent = 0;
				
				System.out.println("[FS_Write] FirstKey: " + firstKey + "; LastKey: " + lastKey);
		
				if(blockTable.containsKey((Integer) firstKey)) {
					_oneToNByzantineRegularRegister.onRead(blockTable.remove(firstKey)); 
					currentBlock = _fileBlock.getBytes();
				} else {
					currentBlock = new byte[Constant.BLOCK_LENGTH];
				}
				
				posContent = copyContent(pos % Constant.BLOCK_LENGTH, size, currentBlock, contents, posContent);
		
				System.out.println("[FS_Write] Block 0: " + Arrays.toString(currentBlock));
				blockList.add(currentBlock);

				if(lastKey > firstKey + 1 || (pos + size) >= Constant.BLOCK_LENGTH) {
					for (int index = firstKey + 1; index <= lastKey; index++) {
						if(blockTable.containsKey((Integer) index)) {
							//currentBlock = getAndVerifyHashBlock(blockTable.remove(index),_previousBlockTable.remove(index)).getBytes();
							_oneToNByzantineRegularRegister.onRead(blockTable.remove(index));
							currentBlock = _fileBlock.getBytes();
						} else {
							currentBlock = new byte[Constant.BLOCK_LENGTH];
						}
						
						System.out.println("[FS_Write] Pos: " + posContent + "; index: " + index + "; size: " + size);
						posContent = copyContent(0, size, currentBlock, contents, posContent);
		
						System.out.println("[FS_Write] Block " + index + ": " + Arrays.toString(currentBlock));
						blockList.add(currentBlock);
					}
				}
				
				BlockId blockId;
				int index = firstKey;
				for(byte[] block : blockList) {
					_oneToNByzantineRegularRegister.onWrite(new HashBlock(block));
					blockId = _blockId;
					blockTable.put((Integer) index, blockId);
					_previousBlockTable.put((Integer) index, blockId);
					index++;
				}
		
				sendIndexBlocks(blockTable);
			} catch (BlockNotFoundException |
					 TamperedBlockException |
					 InvalidRemoteArgumentException |
					 NullArgumentException exception)  {
				throw new FileSystemException("[FS_write] " + exception.getMessage(), exception);
			}
		}
		
		private int readFromFileSystemServer(PublicKey pk, int pos, byte[] contents)
				throws FileSystemException {
			try {
				//Check parameters
				checkArgumentsNonNullability(pk, pos, contents);
				
				int size = contents.length;
				_bytesRead = 0;
				Map<Integer, BlockId> blockTable = getBlockTable(createBlockId(pk));
				
				byte[] currentBlock;
				int firstKey = (int) Math.floor((double) (pos / Constant.BLOCK_LENGTH)),
				    lastKey = (int) Math.ceil((double) ((pos + size) / Constant.BLOCK_LENGTH)),
				    posContent = 0;
				
				System.out.println("[FS_Read]  FirstKey: " + firstKey + "; LastKey: " + lastKey);
				
				if(blockTable.containsKey((Integer) firstKey)) {
					_oneToNByzantineRegularRegister.onRead(blockTable.remove(firstKey));
					currentBlock = _fileBlock.getBytes();
				} else {
					currentBlock = new byte[Constant.BLOCK_LENGTH];
				}
				
				posContent = extractContent(pos % Constant.BLOCK_LENGTH, size, currentBlock, contents, posContent);
				
				System.out.println("[FS_Read]  Block 0: " + Arrays.toString(currentBlock));
				System.out.println("[FS_Read]: " + Arrays.toString(contents));
				
				if(lastKey > firstKey + 1 || (pos + size) >= Constant.BLOCK_LENGTH) {
					for (int index = firstKey + 1; index <= lastKey; index++) {
						if(blockTable.containsKey((Integer) index)) {
							//currentBlock = getAndVerifyHashBlock(blockTable.remove(index),_previousBlockTable.get(index)).getBytes();
							_oneToNByzantineRegularRegister.onRead(blockTable.remove(index));
							currentBlock = _fileBlock.getBytes();
						} else {
							System.out.print("new block");
							currentBlock = new byte[Constant.BLOCK_LENGTH];
						}
						
						System.out.println("Pos: " + posContent);
						posContent = extractContent(0, size, currentBlock, contents, posContent);
		
						System.out.println(Arrays.toString(currentBlock));
						System.out.println(Arrays.toString(contents));
					}
				}
		
				return _bytesRead;
			} catch (BlockNotFoundException |
					 NoSuchAlgorithmException |
					 TamperedBlockException |
					 InvalidRemoteArgumentException |
					 NullArgumentException exception) {
				throw new FileSystemException("[FS_read] " + exception.getMessage(), exception);
			}
		}
		
		private List<PublicKey> getPublicKeysFromFileSystemServer()
				throws FileSystemException {
			try {
				_oneToNByzantineRegularRegister.onRead();
				Set<EncodedPublicKey> encodedPublicKeys = _encodedPublicKeys;
				
				List<PublicKey> output= new ArrayList<>();
		    	//Decode public key
		    	byte[] EncodedPublicKey;
		    	X509EncodedKeySpec publicKeySpec;
		    	KeyFactory keyFactory;
		    	PublicKey publicKey;
				for(EncodedPublicKey encodedPublicKey : encodedPublicKeys){
		        	//Decode public key
		        	EncodedPublicKey = encodedPublicKey.getBytes();
		        	publicKeySpec = new X509EncodedKeySpec(EncodedPublicKey);
		        	keyFactory = KeyFactory.getInstance("RSA");
		        	publicKey = keyFactory.generatePublic(publicKeySpec);
					
					output.add(publicKey);
				}
				return output;
			} catch (NoSuchAlgorithmException |
					 InvalidKeySpecException exception) {
				throw new FileSystemException("[FS_list] " + exception.getMessage(), exception);
			}
		}
		
		public void onWriteReturn(AckFlag ackFlag) {
			_blockId = ackFlag.getBlockId();
			System.out.println("[onWriteReturn] : " + Arrays.toString(_blockId.getBytes()));
		}
		
		public void onReadReturn(FileBlock fileBlock) {
			_fileBlock = fileBlock;
		}
		
		public void onReadReturn(Set<EncodedPublicKey> encodedPublicKeys) {
			_encodedPublicKeys = encodedPublicKeys;
		}
		
		private void exit() {
			_oneToNByzantineRegularRegister.onExit();
		}
		
		public PublicKey getPublicKey() {
			return _oneToNByzantineRegularRegister.getPublicKey();
		}
	}
}