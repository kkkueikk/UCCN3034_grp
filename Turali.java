import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Turali {

	public static void main(String[] args) {
		// instance of broadcast and multicast
		BroadcastReceiver brObj = new BroadcastReceiver();
		MulticastReceiver mrObj = new MulticastReceiver();

		// pass instance to the thread
		Thread BRThread = new Thread(brObj);
		Thread MRThread = new Thread(mrObj);

		// start the thread
		System.out.println("Listening on War Channel(broadcast)...");
		BRThread.start();
		System.out.println("Listening on Festival Channel(multicast)...");
		MRThread.start();
	}
}

// create a class for listening to broadcast
class BroadcastReceiver implements Runnable {
	private static final int BROADCAST_SERVER_PORT = 4014;
	private static final String HASH_ALGORITHM = "SHA-256";
	private static final int SHIFT = 3; // Caesar Cipher shift

	public void run() {
		byte[] buf = new byte[128]; // max character 128
		DatagramSocket brdSock = null;
		DatagramPacket packet = null;

		String payload = "";

		try {
			//move creation of datagramSocket from inside the while loop
			//to outside - no need to open and close socket for each message received
			//this will only give delays and missing of incoming packets
			brdSock = new DatagramSocket(null);
			brdSock.setReuseAddress(true); //allow multiple sockets to bind to the same port
			brdSock.setSoTimeout(1200000); // set timeout to 2 mins
			brdSock.bind(new InetSocketAddress(BROADCAST_SERVER_PORT));

			while (true) {
				//receive only, create a datagram packet to receive
				packet = new DatagramPacket(buf, buf.length);

				//wait for the message to be sent here
				brdSock.receive(packet);

				// Convert byte to string
				payload = new String(packet.getData(), 0, packet.getLength());

				// Print the string to terminal
				System.out.println("\nWar channel: " + payload);

				// Process the received message (decrypt, verify)
				processMessage(payload);

				// Close socket
			}
		} catch (UnknownHostException u) {
			System.err.println(u);
		} catch (IOException i) {
			System.err.println(i);
		} catch (Exception e) {
			System.err.println(e);
		}finally{
			if (brdSock != null && !brdSock.isClosed()) {
				brdSock.close(); // close socket when done
			}
		}
	}

	private void processMessage(String payload) {
		try {
			// Split the message and hash
			String[] parts = payload.split("\\^o\\^");
			if (parts.length != 2) {
				System.out.println("Invalid message format.");
				return;
			}
			String encryptedMessage = parts[0];
			String receivedHash = parts[1];

			// Decrypt the message
			String decryptedMessage = decrypt(encryptedMessage);

			// Verify the hash
			if (verifyHash(decryptedMessage, receivedHash)) {
				System.out.println(decryptedMessage);
			} else {
				System.out.println("Received invalid message.");
			}
		} catch (Exception e) {
			System.err.println("Error processing message: " + e.getMessage());
		}
	}

	private String decrypt(String message) {
		// Caesar Cipher with shift -3
		StringBuilder decrypted = new StringBuilder();
		for (char c : message.toCharArray()) {
			decrypted.append((char) (c - SHIFT));
		}
		return decrypted.toString();
	}

	private String generateHash(String message) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
		byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private boolean verifyHash(String decryptedMessage, String receivedHash) throws NoSuchAlgorithmException {
		try {
			String localHash = generateHash(decryptedMessage.trim());
			return localHash.equals(receivedHash);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Hash verification error! " + e.getMessage());
			throw e;
		}
	}
}

// create a class for listening to multicast
class MulticastReceiver implements Runnable {
	private static final int MULTICAST_SERVER_PORT = 3014;
	private static final String MULTICAST_GROUP_ADDRESS = "224.0.0.14";
	private static final String HASH_ALGORITHM = "SHA-256";
	private static final int SHIFT = 3; // Caesar Cipher shift

	public void run() {
		byte[] buf = new byte[128]; // max character 128
		MulticastSocket mcSocket = null;
		InetAddress group = null;
		DatagramPacket rcvPacket = null;
		String payload = "";

		try {
			// create a socket with group address and port
			mcSocket = new MulticastSocket(MULTICAST_SERVER_PORT);
			// add the multicast socket as a member of group address & enable reuse socket
			mcSocket.setReuseAddress(true);
			mcSocket.setSoTimeout(1200000);
			group = InetAddress.getByName(MULTICAST_GROUP_ADDRESS);
			mcSocket.joinGroup(group);
			while (true) {
				//create packet to store payload
				rcvPacket = new DatagramPacket(buf, buf.length);

				//listen to group address and block program
				mcSocket.receive(rcvPacket);

				// Convert byte to string
				payload = new String(rcvPacket.getData(), 0, rcvPacket.getLength());

				// Print the string to terminal
				System.out.println("\nFestival channel: " + payload);

				// Process the received message
				processMessage(payload);

				// Leave group, close socket
			}
		} catch (UnknownHostException u) {
			System.err.println(u);
		} catch (IOException i) {
			System.err.println(i);
		} catch (Exception e) {
			System.err.println(e);
		}finally{
			try {
				if (mcSocket != null && !mcSocket.isClosed()) {
					mcSocket.leaveGroup(group);
					mcSocket.close();
				}
			} catch (Exception e) {
				mcSocket.close();
				System.err.println(e);
			}

		}
	}

	private void processMessage(String payload) {
		try {
			// Split the message and hash
			String[] parts = payload.split("\\^o\\^");
			if (parts.length != 2) {
				System.out.println("Invalid message format.");
				return;
			}
			String encryptedMessage = parts[0];
			String receivedHash = parts[1];

			// Decrypt the message
			String decryptedMessage = decrypt(encryptedMessage);

			// Verify the hash
			if (verifyHash(decryptedMessage, receivedHash)) {
				System.out.println(decryptedMessage);
			} else {
				System.out.println("Received invalid message.");
			}
		} catch (Exception e) {
			System.err.println("Error processing message: " + e.getMessage());
		}
	}

	private String decrypt(String message) {
		StringBuilder decrypted = new StringBuilder();
		for (char c : message.toCharArray()) {
			decrypted.append((char) (c - SHIFT));
		}
		return decrypted.toString();
	}

	private String generateHash(String message) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
		byte[] hash = digest.digest(message.trim().getBytes(StandardCharsets.UTF_8));
		StringBuilder hexString = new StringBuilder();
		for (byte b : hash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private boolean verifyHash(String decryptedMessage, String receivedHash) throws NoSuchAlgorithmException {
		try {
			String localHash = generateHash(decryptedMessage.trim());
			return localHash.equals(receivedHash);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Hash verification error! " + e.getMessage());
			throw e;
		}
	}
}
