package rmiserver;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javafx.application.Application;
import javafx.stage.Stage;


public class RMIServer extends Application {

  public static final String SERVICE = "Service";

  @Override
  public void start(Stage stage) throws RemoteException, AlreadyBoundException {
    ServerInterface serverInterface = new ServerInterfaceImpl();

    Registry registry = LocateRegistry.createRegistry(1099);
    registry.bind(SERVICE, serverInterface);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
