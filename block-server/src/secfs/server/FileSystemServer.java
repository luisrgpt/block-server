package secfs.server;

import java.rmi.RemoteException;
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

import secfs.common.BlockId;
import secfs.common.EncodedCertificate;
import secfs.common.EncodedPublicKey;
import secfs.common.EncodedSignature;
import secfs.common.FileBlock;
import secfs.common.FileSystemServerReply;
import secfs.common.HashBlock;
import secfs.common.IFileSystemServer;
import secfs.common.KeyBlock;
import secfs.common.exception.BlockNotFoundException;
import secfs.common.exception.InvalidRemoteArgumentException;
import secfs.common.exception.TamperedBlockException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;


public final class FileSystemServer extends UnicastRemoteObject implements IFileSystemServer {
	private static final String CERTIFICATE_ALIAS = "secretKeyAlias";

	/**
	 * 
	 */
	private static final long serialVersionUID = -7081463307932455315L;

	private static final String KEY_STORE_NAME = "keyStoreName";
	private boolean serverUnderAttack= true;
	private boolean canAttack=false;
	private char[] _password;
	
	private KeyStore _keyStore;
	
	Map<BlockId, FileBlock> _dataBase;
	Map<BlockId, BlockId> _keyBlockIdTable;
	
    protected FileSystemServer() throws RemoteException {
		super();
	}
    
	private static void checkArgumentsNonNullability(Object ... objects)
			throws InvalidRemoteArgumentException {
		for(Object object : objects) {
			if(object == null) {
				throw new InvalidRemoteArgumentException();
			}
		}
	}
	
	void initializeFileSystemServer(char[] password)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidRemoteArgumentException {
		//Throws InvalidRemoteArgumentException
		checkArgumentsNonNullability(password);
		
		//Initialize tables
		_dataBase = new HashMap<>();
		_keyBlockIdTable = new HashMap<>();
		_keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		_password = password;
		
		//Get user password and file input stream
    	FileInputStream fileInputStream = new FileInputStream(KEY_STORE_NAME);
    	_keyStore.load(fileInputStream, _password);
    	
	}

  	private BlockId createBlockId(byte[] bytes) throws NoSuchAlgorithmException, InvalidRemoteArgumentException{
  		//Throws InvalidRemoteArgumentException
        checkArgumentsNonNullability(bytes);
  		
    	//Create block id using block's hash
    	MessageDigest messageDigest;
		messageDigest = MessageDigest.getInstance("SHA-512");
    	messageDigest.update(bytes);
    	byte[] hash = messageDigest.digest();
    	return new BlockId(hash);
  	}

  	public FileBlock get(BlockId blockId)
  			throws BlockNotFoundException, TamperedBlockException, RemoteException, InvalidRemoteArgumentException {
  		//Throws InvalidRemoteArgumentException
  		checkArgumentsNonNullability(blockId);
  		
  		try {
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
		} catch (NoSuchAlgorithmException e) {
			//Not suppose to happen, even as an invalid state
			System.out.println(e.getMessage());
			System.exit(1);
			throw new RemoteException("get: " + e.getMessage());
		}
  		
  		throw new BlockNotFoundException();
    }

   	public BlockId put_k(KeyBlock keyBlock, EncodedSignature encodedSignature, EncodedPublicKey encodedPublicKey)
   			throws TamperedBlockException, InvalidKeyException, InvalidKeySpecException, RemoteException, SignatureException, InvalidRemoteArgumentException {
  		//Throws InvalidRemoteArgumentException
   		checkArgumentsNonNullability(keyBlock, encodedSignature, encodedPublicKey);

        try {
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
		} catch (NoSuchAlgorithmException e) {
			//Not suppose to happen, even as an invalid state
			System.out.println(e.getMessage());
			System.exit(1);
			throw new RemoteException("put_k: " + e.getMessage());
		}
    }

    public BlockId put_h(HashBlock hashBlock)
    		throws RemoteException, InvalidRemoteArgumentException {
  		//Throws InvalidRemoteArgumentException
    	checkArgumentsNonNullability(hashBlock);
        
        try {
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
		} catch (NoSuchAlgorithmException e) {
			//Not suppose to happen, even as an invalid state
			System.out.println(e.getMessage());
			System.exit(1);
			throw new RemoteException("put_h: " + e.getMessage());
		}
    }
    
    public void serverAttack(){
    	canAttack=true;
    }

	@Override
	public FileSystemServerReply storePubKey(EncodedCertificate encodedCertificate)
    		throws RemoteException, InvalidRemoteArgumentException, CertificateException, KeyStoreException {
  		//Throws InvalidRemoteArgumentException
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
}