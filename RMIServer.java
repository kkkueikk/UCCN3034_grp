import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.math.BigInteger;

public class RMIServer extends UnicastRemoteObject implements RMIInterface {

    protected RMIServer() throws RemoteException {
        super();
    }

    public int addNumbers(int a, int b) throws RemoteException {
        return a + b;
    }

    public BigInteger calculatePasswordComplexity(String password) throws RemoteException {
        if (password == null || password.isEmpty()) {
            return BigInteger.ZERO;
        }

        int index = 0;
        BigInteger complexity;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        if (hasUppercase)
            index += 26;
        if (hasLowercase)
            index += 26;
        if (hasDigit)
            index += 10;
        if (hasSpecialChar)
            index += 32;

        complexity = BigInteger.valueOf(index).pow(password.length());

        return complexity;
    }

    public static void main(String[] args) {
        try {
            // Create RMI registry on port 20014
            try {
                java.rmi.registry.LocateRegistry.createRegistry(20014);
                System.out.println("'RMI Registry created on port 20014'");
            } catch (RemoteException e) {
                System.out.println("RMI Registry already running on port 20014");
            }

            RMIServer server = new RMIServer();
            // Bind the RMIServer to port 20014
            java.rmi.Naming.rebind("rmi://localhost:20014/RMIServer", server);

            System.out.println("RMI Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
