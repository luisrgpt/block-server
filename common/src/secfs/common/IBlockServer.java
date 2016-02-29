package secfs.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBlockServer extends Remote {
	public static final String SERVICE_NAME = "BlockServer";
	
  	byte[] get(int id) throws RemoteException;
    int put_k(byte[] data, byte[] signature, byte[] public_key) throws RemoteException;
    int put_h(byte[] data) throws RemoteException;
}