import java.rmi.Remote;
import java.rmi.RemoteException;
import java.math.BigInteger;
public interface RMIInterface extends Remote {
    int addNumbers(int a, int b) throws RemoteException;
    BigInteger calculatePasswordComplexity(String password) throws RemoteException;
}
