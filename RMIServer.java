import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {

    protected RMIServer() throws RemoteException {
        super();
    }

    @Override
    public int addNumbers(int a, int b) throws RemoteException {
        return a + b;
    }

    @Override
    public int calculatePasswordComplexity(String password) throws RemoteException {
        // Simple password complexity calculation: return the length of the password
        return password.length();
    }

    public static void main(String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            RMIServer server = new RMIServer();
            java.rmi.Naming.rebind("rmi://localhost/RMIServer", server);
            System.out.println("RMI Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
