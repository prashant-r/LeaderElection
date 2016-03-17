package step1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import common.Utility;
import common.Utility.ArgumentParser;
import common.Utility.HostPorts;
import common.Utility.ParseResults;

public class Process {
	public static Logger log;
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	public static List<HostPorts> peers;
	
	Process(int hostIndex)
	{
		
		
	}
	
	void receive(int port)
	{
		DatagramSocket socket; 
		try{
			socket = new DatagramSocket(port);
			byte[] recData = new byte[1024];
			byte[] sendData = new byte[1024];
			while(true)
			{
				try{
					DatagramPacket recPacket = new DatagramPacket(recData,recData.length);
					socket.receive(recPacket);
					String receivedString = new String(recPacket.getData());
					// Reset the byte array - very important
					Arrays.fill(recData, (byte) 0 );
					log.info("Received from client - "+ recPacket.getAddress()+ ":"+ recPacket.getPort()+ "-->" + receivedString);
					String [] recString = receivedString.split(" ");
					String response = "";
					// Validating the input string
					if(!(validateString(receivedString) && recString.length>=2))
						response = "400 BAD REQUEST";
					else{	
						response = "ACK";
					}
					// Collect the response and send it back to client
					sendData = response.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, recPacket.getAddress(), recPacket.getPort());
					socket.send(sendPacket);
					log.info("Replied to client " + recPacket.getAddress() + ":" + recPacket.getPort() +" " + response);
				}
				catch(Exception e)
				{
					log.error("Socket error occured with cause" + e.getMessage());
				}
			}
		}
		catch(Exception e)
		{	
			log.error("Server connection open on port "  + " exited with " + e.getMessage());
		}
	}
	
	void send(String sendProtocolString, String hostname, int port)
	{
		System.out.println("hostname : " + hostname + " port : " + port);
		try{
			byte[] sendingBytes = sendProtocolString.getBytes();
			byte[] recBytes = new byte[1024];
			DatagramPacket sendPacket = new DatagramPacket(sendingBytes, sendingBytes.length, InetAddress.getByName(hostname),port);
			DatagramPacket receivePacket = new DatagramPacket(recBytes, recBytes.length);
			DatagramSocket socket = new DatagramSocket();
			socket.send(sendPacket);
			// Set the timeout to be 5 seconds for an unresponsive server.
			socket.setSoTimeout(5000);
			while(true)
			{
				try{
				socket.receive(receivePacket);
				String receivedString = new String(receivePacket.getData());
				if(validateString(receivedString))
					System.out.println("Received from server at -" + " "+ receivedString);
				else
					System.out.println("MALFORMED response from server");
				socket.close();
				return;
				}
				catch(Exception e)
				{
					// Very important to return after an exception is caught. In the case of an exception due to timeout
					// abort connection and exit code.
					System.out.println("Datagram packet receive routine failed with " + e.getMessage());
				}
				finally{
					// In case an exception occured such as a timeout fail, abort connection and move on to next request.
					socket.close();
					
					}
				
				}
			}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void startProcess(String hostname, int port){
	
	
	
	}
	public static boolean validateString(String s)
	{
		return s.matches("\\A\\p{ASCII}*\\z");
	}
	
	public static void main(String args[]) throws NumberFormatException, IOException
	{
	
		portNumber = null;
		hostFile = null;
		maxCrashes = null;
		ParseResults parseResults = ArgumentParser.parse(args, 	Utility.maxNumReplicas);
		portNumber = parseResults.portNumber;
		hostFile = parseResults.hostFile;
		maxCrashes = parseResults.maxCrashes;
		peers = new ArrayList<HostPorts>();
		int numProcs = 0;
		Path path = Paths.get(System.getProperty("user.dir"));
		String [][] hostPorts =Utility.readConfigFile(path.getParent().getParent() + hostFile);
		String hostname = InetAddress.getLocalHost().getHostAddress();
		Integer me = null;
		for(int a=0; a<hostPorts.length ; a++)
		{	
			numProcs++;
			if(hostPorts[a][1] == null)
				break;
			HostPorts newHostPort = new HostPorts(Integer.parseInt(hostPorts[a][0]), hostPorts[a][1], portNumber);
			String match =  InetAddress.getByName(hostPorts[a][1]).getHostAddress();
			if(hostPorts[a][1].toLowerCase().trim().equalsIgnoreCase("localhost"))
				match =  InetAddress.getLocalHost().getHostAddress();
			if(match.equalsIgnoreCase(hostname))
				me = new Integer(a);
			peers.add(newHostPort);
		}
		if(me == null)
		{
			System.out.println("Error: this process's address and port is not registered in peer group list located in configs.txt");
			System.exit(-1);
		}	
		log= Logger.getLogger("Process #" + me);
		Utility.configureLogger(log);
		System.out.println("Process #" + me + " started!");
	}
}
