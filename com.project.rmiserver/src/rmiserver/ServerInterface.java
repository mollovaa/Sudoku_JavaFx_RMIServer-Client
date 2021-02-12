package rmiserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface Server interface which plays the role as communication layer between the server and the client.
 */
public interface ServerInterface extends Remote {

  /**
   * Generates a {@link Sudoku} based on the given difficulty.
   *
   * @param difficulty the difficulty  a number which represents how many numbers will be hidden.
   * @return the sudoku {@link Sudoku}
   * @throws RemoteException the remote exception
   */
  Sudoku startSudoku(int difficulty) throws RemoteException;
}
