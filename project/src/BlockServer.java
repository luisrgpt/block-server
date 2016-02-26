import java.rmi.*;
import java.util.Vector;

public interface BlockServer extends Remote {
  	byte[] get(int id) throws RemoteException;
    int put_k(byte[] data, byte[] signature, byte[] public_key) throws RemoteException;
    int put_h(byte[] data) throws RemoteException;
}
