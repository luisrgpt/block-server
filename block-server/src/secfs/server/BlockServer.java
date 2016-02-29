package secfs.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import secfs.common.IBlockServer;

import java.lang.System;

public class BlockServer extends UnicastRemoteObject implements IBlockServer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7081463307932455315L;

	public BlockServer() throws RemoteException {
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