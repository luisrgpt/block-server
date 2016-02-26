import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.lang.System;

public class SecBlockServer extends UnicastRemoteObject implements BlockServer {
    public SecBlockServer() throws RemoteException {
    }

  	public byte[] get(int id) throws RemoteException {
  	    System.out.println("Invoking get");
        return null;
     }

   	public int put_k(byte[] data, byte[] signature, byte[] public_key) throws RemoteException {
        System.out.println("Invoking put_k");
        return 0;
    }

    public int put_h(byte[] data) throws RemoteException {
        System.out.println("Invoking put_h");
        return 0;
    }
}
