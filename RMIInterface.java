import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {
    int addNumbers(int a, int b) throws RemoteException;
    int calculatePasswordComplexity(String password) throws RemoteException;
}
