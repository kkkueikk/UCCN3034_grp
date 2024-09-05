import java.io.*;
import java.net.*;

public class Turali {

	public static void main(String[] args) {
		// instance of broadcast and multicast
		BroadcastReceiver brObj= new BroadcastReceiver();
		MulticastReceiver mrObj= new MulticastReceiver();
		
		//pass instance to the thread
		Thread BRThread= new Thread(brObj);
		Thread MRThread= new Thread(mrObj);
		//start the thread
		System.out.println("Listening on War Channel(broadcast)...");
		BRThread.start();
		System.out.println("Listening on Festival Channel(multicast)...");
		MRThread.start();
	}
}

//create a class for listen to broadcast
class BroadcastReceiver implements Runnable{
	public void run() {
		byte[] buf= new byte[128]; //max character 128
		DatagramSocket brdSock=null;
		DatagramPacket packet=null;
		
		String payload=""; 
		try {
			while(true) {
				//Create a udp socket that listen on port 4014
				brdSock= new DatagramSocket(null);
				brdSock.setReuseAddress(true);
				brdSock.bind(new InetSocketAddress(4014));

				//only receive, thus create a datagram packet to receive
				packet= new DatagramPacket(buf,buf.length);
				
				//wait for the message sent to port 4014
				brdSock.receive(packet);
				
				//convert byte to string
				payload = new String(packet.getData(), 0, packet.getLength());
				
				//print the string to terminal
				System.out.println("\nWar channel: " + payload);
				
				//close socket
				brdSock.close();
			}
		}catch (UnknownHostException u) {
			System.err.println(u);
			
		}catch (IOException i) {
			System.err.println(i);
		}catch (Exception e) {
			System.err.println(e);
		}
	}
}

//create a class for listen to multicast
class MulticastReceiver implements Runnable{
	
	public void run() {
		byte[] buf= new byte[128]; //max character 128
		MulticastSocket mcSocket=null;
		InetAddress group=null;
		DatagramPacket rcvPacket=null;
		String payload ="";
		
		try {
			while(true) {
				//Create a socket with group address and port
				mcSocket= new MulticastSocket(3014);
				group= InetAddress.getByName("224.0.0.14");
				//add the multicast socket as a member of group address & enable reuse socket
				mcSocket.joinGroup(group);
				mcSocket.setReuseAddress(true);
				
				//create packet to store payload 
				rcvPacket= new DatagramPacket(buf,buf.length);
				
				//listen to group address and block program
				mcSocket.receive(rcvPacket);
				
				//convert byte to string
				payload = new String(rcvPacket.getData(),0,rcvPacket.getLength());
				
				//print the string to terminal
				System.out.println("\nFestival channel: " + payload);
				
				//leave group , close socket
				mcSocket.leaveGroup(group);
				mcSocket.close();
			} //end of while 
		}catch (UnknownHostException u) {
			System.err.println(u);
			
		}catch (IOException i) {
			System.err.println(i);
		}catch (Exception e) {
			System.err.println(e);
		}
	}
}