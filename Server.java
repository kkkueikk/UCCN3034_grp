import java.net.*;
import java.io.*;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			while(true) {
				//Create a serversocket
				ServerSocket ss= new ServerSocket(2014);
				
				//Block and listen to the connection to this socket. return a new socket
				System.out.println("\nListening...");
				Socket s= ss.accept();
				System.out.println("Ruler connected");
				
				//create input stream to receive input from client stream
				DataInputStream in = new DataInputStream(s.getInputStream());
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				
				String msg="";
				DatagramSocket brdSock=null;
				char opt='\0';
				String payload="", status="";
				//Server receive msg till 7 is sent from the ruler
				do {
					System.out.println("\nWaiting for msg...");
					msg= in.readUTF();
					opt= msg.charAt(0);
					status="";
					//msg[0]=="3"
					if(opt=='3') {
						//print received command,msg and next operation
						System.out.println("Option "+ opt+": War channel(broadcast)");
						
						//create the socket with broadcast(the one who sends: port is assgined by OS) 
						brdSock= new DatagramSocket();
						
						//configure the socket to broadcast message.
						brdSock.setBroadcast(true);
						
						//create datagram packet containing message, destIP and port
						InetAddress brdAddr=InetAddress.getByName("255.255.255.255");
						payload= msg.substring(2, msg.length());
						DatagramPacket sndPacket= 
						new DatagramPacket(payload.getBytes(),payload.getBytes().length,brdAddr,4014);
						
						//broadcast the packet containing the msg to every devices
						brdSock.send(sndPacket);
						
						// close the socket
						brdSock.close();
						
						//print the complete status
						status= "Message is broadcasted.";
										
					}
					//msg[0]=="4"
					else if(opt=='4') {
						//print received command, msg and next operation
						System.out.println("Option "+ opt+": Festival channel(multicast)");
						
						//create the socket with multicast port 3014
						DatagramSocket ms= new DatagramSocket(0);
						//(in c)configure the socket to multicast message.
						//in java just send the packet to the group address
						
						//create a datagram packet with byte[],length, destIP and port
						payload= msg.substring(2, msg.length());
						byte[] buf= payload.getBytes();
						InetAddress group = InetAddress.getByName("224.0.0.14");
						
						DatagramPacket mcPacket= new DatagramPacket(buf,buf.length,group,3014);
						
						//send the packet using socket and close
						ms.send(mcPacket);
						ms.close();
						
						//print the complete status
						status= "Message is sent to Turali.";
						
					}
					else if(opt=='7') {
						System.out.println("Option "+opt+": Exit");
						status= "Ruler disconnected from server";
					}
					else {
						status = "Unknown option.";
					}
					System.out.println("Status: " +status);	
					//send the status back to the ruler
					out.writeUTF(status);
					out.flush();
					
				} while(opt!='7');
				out.close();
				in.close();  
				s.close();  
				ss.close();  
			} //end of outer while

		}catch (UnknownHostException u) {
			System.err.println(u);
			
		}catch (IOException i) {
			System.err.println(i);
		}catch(Exception e){
			System.err.println(e);
		}
	}//end of main
}
