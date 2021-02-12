package rmiserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerInterfaceImpl extends UnicastRemoteObject implements ServerInterface {

  public ServerInterfaceImpl() throws RemoteException {
    super();
  }

  @Override
  public Sudoku startSudoku(int difficulty) throws RemoteException {
    return new Sudoku(difficulty);
  }
}
