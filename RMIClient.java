import java.rmi.Naming;

public class RMIClient {

    public static void main(String[] args) {
        try {
            RMIServerInterface server = (RMIServerInterface) Naming.lookup("rmi://localhost/RMIServer");

            int result = server.addNumbers(10, 5);
            System.out.println("Addition Result: " + result);

            int complexity = server.calculatePasswordComplexity("MyPassword123");
            System.out.println("Password Complexity: " + complexity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
