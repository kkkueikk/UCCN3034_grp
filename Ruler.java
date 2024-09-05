import java.net.*;
import java.io.*;
public class Ruler {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//set up the socket with the IP address and port number
		Socket s = new Socket("127.0.0.1", 2014);
		
		//create input stream to send and receive data from server stream
		DataInputStream din = new DataInputStream(s.getInputStream());
		DataOutputStream dout = new DataOutputStream(s.getOutputStream());
		//use bufferedreader to read from user input 
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String str = "", str2 = "";
		try {
			while (!str.equals("7")) {
				System.out.print("Option: ");
				str = br.readLine();
				// opt==3
				if(str.equals("3")) {
					//prompt for user input
					System.out.print("War channel(broadcast message): ");
					str+= "," + br.readLine();
					//send msg contain a "3, {entered message}"
				}
				//opt==4
				else if(str.equals("4")) {
					//prompt for user input
					System.out.print("Festival Channel(multicast message): ");
					str+= "," + br.readLine();
				}else if(str.equals("7")) {
					System.out.println("Exit.");
		
				}else {
					System.out.println("Unknown option");
					continue;
				}
				//send the command to the server
				dout.writeUTF(str);
				dout.flush();
				str2 = din.readUTF();
				System.out.println("Server reply: " + str2 + "\r\n");
			}
		}catch (UnknownHostException u) {
			System.err.println(u);
			
		}catch (IOException i) {
			System.err.println(i);
		}catch(Exception e){
			System.err.println(e);
		}

		br.close();
		din.close();
		dout.close();
		s.close();

	}

}
