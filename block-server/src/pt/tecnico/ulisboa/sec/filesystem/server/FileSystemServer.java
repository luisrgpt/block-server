package pt.tecnico.ulisboa.sec.filesystem.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pt.tecnico.ulisboa.sec.filesystem.common.BlockId;
import pt.tecnico.ulisboa.sec.filesystem.common.Constant;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedCertificate;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedPublicKey;
import pt.tecnico.ulisboa.sec.filesystem.common.EncodedSignature;
import pt.tecnico.ulisboa.sec.filesystem.common.FileBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.FileSystemServerReply;
import pt.tecnico.ulisboa.sec.filesystem.common.HashBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.IFileSystemServer;
import pt.tecnico.ulisboa.sec.filesystem.common.KeyBlock;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.BlockNotFoundException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.BlockTooSmallException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.InvalidRemoteArgumentException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.NullArgumentException;
import pt.tecnico.ulisboa.sec.filesystem.common.exception.TamperedBlockException;
import pt.tecnico.ulisboa.sec.filesystem.server.exception.FileSystemServerException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.net.URISyntaxException;


final class FileSystemServer {
	
	FileSystemServer(char[] password)
			throws RemoteException, FileSystemServerException {
		new FileSystemServerImpl(password);
	}
	
	private class FileSystemServerImpl extends UnicastRemoteObject implements IFileSystemServer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7081463307932455315L;
		
		//Constants
		private static final String POLICY_FILE_NAME  = "/security.policy",
				                    KEY_STORE_NAME    = "keyStoreName",
				                    FILE_NAME         = "FileSystemServer.class",
				                    CERTIFICATE_ALIAS = "secretKeyAlias";
		private boolean serverUnderAttack = true,
				        canAttack         = false;
		
		//Key server attributes
		private char[] _password;
		private KeyStore _keyStore;
		
		//Block server attributes
		private Map<BlockId, FileBlock> _dataBase;
		private Map<BlockId, BlockId> _keyBlockIdTable;
		
		private FileSystemServerImpl(char[] password)
				throws RemoteException, FileSystemServerException {
			try {
				//Checking parameters
				checkArgumentsNonNullability(password);
				checkBlocksSize();
				
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
				
				//Initialise attributes
				_dataBase = new HashMap<>();
				_keyBlockIdTable = new HashMap<>();
				_keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				_password = password;
				
				//Load key store
				FileInputStream fileInputStream;
				File keyStoreFile = new File(KEY_STORE_NAME);
				if(keyStoreFile.exists()) {
					fileInputStream = new FileInputStream(keyStoreFile);
				} else {
					fileInputStream = null;
				}
				
		    	_keyStore.load(fileInputStream, _password);
		    	
		    	//Submit file system server
		        Registry reg = LocateRegistry.createRegistry(Constant.REGISTRY_PORT);
				reg.rebind(Constant.SERVICE_NAME, (IFileSystemServer) this);
		
				//Naming.rebind("BlockServer", blockServer);
			} catch (BlockTooSmallException |
					 URISyntaxException |
					 KeyStoreException |
					 NoSuchAlgorithmException |
					 CertificateException |
					 IOException |
					 NullArgumentException exception) {
				throw new FileSystemServerException("[main]: " + exception.getMessage(), exception);
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
	  	public FileBlock get(BlockId blockId)
	  			throws BlockNotFoundException, TamperedBlockException, RemoteException, InvalidRemoteArgumentException, NullArgumentException, NoSuchAlgorithmException {
	  		//Check parameters
	  		checkArgumentsNonNullability(blockId);
  			
	  		//Check if block exists
	  		BlockId currentBlockId;
	    	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
	    		currentBlockId = entry.getKey();
	    		if(currentBlockId.hashCode() == blockId.hashCode()) {		
    				BlockId newBlockId = createBlockId(entry.getValue().getBytes());
    				
    				System.out.println("Value: " + Arrays.toString(newBlockId.getBytes()));
    				System.out.println("Key: " + Arrays.toString(entry.getKey().getBytes()));
    				if (_keyBlockIdTable.get(currentBlockId) != null)
    				System.out.println("Table: " + Arrays.toString(_keyBlockIdTable.get(currentBlockId).getBytes()));
    				System.out.println("Returns: " + Arrays.toString(entry.getValue().getBytes()));
    				
    				//Check if key block was tampered
    				if(!entry.getKey().equals(newBlockId) &&
					   (_keyBlockIdTable.get(currentBlockId) == null ||
					    !_keyBlockIdTable.get(currentBlockId).equals(newBlockId))) {
						throw new TamperedBlockException();
					}
    				
    				//Simulate attack
    				if(serverUnderAttack && canAttack){
    					serverUnderAttack=false;
    					int size = entry.getValue().getBytes().length;
    					byte[] fakeData = new byte[size];
    					new Random().nextBytes(fakeData);
    					return new FileBlock(fakeData);
    				}
    				
					return entry.getValue();
	    		}
	    	}
	  		
	  		throw new BlockNotFoundException();
	    }
	
		@Override
	  	public BlockId put_k(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey)
	   			throws TamperedBlockException, InvalidKeyException, InvalidKeySpecException, RemoteException, SignatureException, InvalidRemoteArgumentException, NullArgumentException, NoSuchAlgorithmException {
   			//Check parameters
	   		checkArgumentsNonNullability(keyBlock, encodedSignature, encodedPublicKey);
   			
        	//Decode public key
        	byte[] encodedKey = encodedPublicKey.getBytes();
        	X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
        	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        	PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        	//Verify signature
        	Signature signature = Signature.getInstance("SHA1withRSA");
        	signature.initVerify(publicKey);
        	signature.update(keyBlock.getBytes());
        	if(!signature.verify(encodedSignature.getBytes())){
        		serverAttack();
        		System.out.println("Verification failled!");
        		throw new TamperedBlockException();
        	}
        	
        	//Inject signature into key block
        	byte[] content = new byte[keyBlock.getBytes().length + encodedSignature.getBytes().length];
        	System.arraycopy(keyBlock.getBytes(), 0, content, 0, keyBlock.getBytes().length);
        	System.arraycopy(encodedSignature.getBytes(), 0, content, keyBlock.getBytes().length, encodedSignature.getBytes().length);
        	KeyBlock signedKeyBlock = new KeyBlock(content);

        	//Put key block into database using public key's hash
        	BlockId blockId = createBlockId(encodedPublicKey.getBytes());
        	boolean blockExists = false;

      		//Replace block if block exists
        	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
        		if(entry.getKey().hashCode() == blockId.hashCode()) {
        			_dataBase.put(entry.getKey(), signedKeyBlock);
        			_keyBlockIdTable.put(entry.getKey(), createBlockId(signedKeyBlock.getBytes()));
        			blockExists = true;
        			break;
        		}
        	}
        	//Else put block into new slot
        	if(!blockExists) {
        		_dataBase.put(blockId, signedKeyBlock);
        		_keyBlockIdTable.put(blockId, createBlockId(signedKeyBlock.getBytes()));
        	}

        	System.out.println(Arrays.toString(signedKeyBlock.getBytes()));

        	//Return block id
			return blockId;
	    }
	
		@Override
	  	public BlockId put_h(HashBlock hashBlock)
	    		throws RemoteException, InvalidRemoteArgumentException, NullArgumentException, NoSuchAlgorithmException {
	   		//Check parameters
	    	checkArgumentsNonNullability(hashBlock);
        	
        	//Put hash block into database using hash block's hash
        	BlockId blockId = createBlockId(hashBlock.getBytes());
        	boolean blockExists = false;
        	
      		//Replace block if block exists
        	for (Map.Entry<BlockId, FileBlock> entry : _dataBase.entrySet()) {
        		if(entry.getKey().hashCode() == blockId.hashCode()) {
        			blockExists = true;
        			break;
        		}
        	}
        	//Else put block into new slot
        	if(!blockExists) {
        		_dataBase.put(blockId, hashBlock);
        	}
        	
        	//Return block id
			return blockId;
	    }
	
		@Override
	  	public FileSystemServerReply storePubKey(EncodedCertificate encodedCertificate)
	    		throws RemoteException, InvalidRemoteArgumentException, CertificateException, KeyStoreException, NullArgumentException {
	   		//Check parameters
			checkArgumentsNonNullability(encodedCertificate);
			
			//Decode certificate
			InputStream inputStream = new ByteArrayInputStream(encodedCertificate.getBytes());
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificates(inputStream);
	
			//Store certificate
			KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(_password);
		    KeyStore.TrustedCertificateEntry trustedCertificateEntry = new KeyStore.TrustedCertificateEntry(certificate);
		    _keyStore.setEntry(CERTIFICATE_ALIAS, trustedCertificateEntry, protectionParameter);
			
			return FileSystemServerReply.ACK;
		}
	
		@Override
	  	public List<EncodedPublicKey> readPubKeys()
				throws KeyStoreException {
			Certificate[] certificates = _keyStore.getCertificateChain(CERTIFICATE_ALIAS);
			
			//Get public keys from key store
			List<EncodedPublicKey> publicKeys = new ArrayList<>();
			EncodedPublicKey encodedPublicKey;
			for(Certificate certificate : certificates) {
				encodedPublicKey = new EncodedPublicKey(certificate.getPublicKey().toString().getBytes());
				publicKeys.add(encodedPublicKey);
			}
			
			return publicKeys;
			
		}
	   	
	   	private void serverAttack(){
	    	canAttack=true;
	    }
	}
}