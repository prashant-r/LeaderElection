package step1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import common.Utility;
import common.Utility.ArgumentParser;
import common.Utility.ClientPropose;
import common.Utility.ClientReply;
import common.Utility.HostPorts;
import common.Utility.ParseResults;

public class Process {
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	public static List<HostPorts> peers;
	public static Set<Integer> S = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
	public static volatile Integer decided = null;
	public static ConcurrentHashMap<Integer, RecStatus> recStatus;
	public static Integer me;
	public static Integer proposal;
	public enum RecStatus{
		REC, TIMEOUT, UNTRIED
	}	
	public static class Server implements Runnable
	{
		private static void receive(int port)
		{
			DatagramSocket receiveSocket =null;
			try{
				receiveSocket = new DatagramSocket(port);
				byte[] recData = new byte[1024];
				while(decided == null)
				{			
					try{
						DatagramPacket recPacket = new DatagramPacket(recData,recData.length);
						receiveSocket.receive(recPacket);
						new Thread(new PacketExecutor(recPacket.getAddress(),Arrays.copyOf(recData, recData.length) )).start();
						Arrays.fill(recData, (byte) 0 );
					
						// Reset the byte array - very important
						// Collect the response and send it back to client
						}
					catch(Exception e)
					{
						System.out.println("Socket error occured with cause" + e.getMessage());
					}
				}
			}
			catch(Exception e)
			{	
				System.out.println("Server connection open on port "  + " exited with " + e.getMessage());
			}
			finally
			{
				if(receiveSocket!=null)
					receiveSocket.close();
			}
		}

	@Override
	public void run() {
		receive(portNumber);
	}
	}
	public static class PacketExecutor implements Runnable {
		InetAddress inetAddress;
		byte[] data;
	    public PacketExecutor(InetAddress inetAddress, byte [] data) {
	        this.inetAddress = inetAddress;
	        this.data = data; 
	    }

	    public void run() {
	    
		String response = "";
			// Validating the input string
		try{
			HostPorts peer = Utility.findPeerIndex(inetAddress, peers);
			if((peer == null))
			{
				response = "403 FORBIDDEN";
				System.out.println("Received at client" + me + " from client process - Unknown  result: " + response);
				return;
			}
			else{
				Object recObj = null;
				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(data));
				recObj = iStream.readObject();
				iStream.close();
				response = "ACK";
			    if(recObj instanceof ClientPropose) {
			       	ClientPropose incomingProposal = (ClientPropose) recObj;
			    	Integer value = new Integer(incomingProposal.data);
			    	S.add(value);
			    	recStatus.put(peer.getHostIndex(), RecStatus.REC);
			    	send( new ClientReply(proposal), peer.getHostName() , portNumber);
			    } else if (recObj instanceof ClientReply)
			    	
			    {
			    	ClientReply incomingProposal = (ClientReply) recObj;  
			    	Integer value = new Integer(incomingProposal.data);
					S.add(value);
			    	recStatus.put(peer.getHostIndex(), RecStatus.REC);
			    }
			    else
			    {
			    	response = "400 BAD REQUEST";
					System.out.println("Received at client" + me + " from client process - " + peer +  "-->" + data + " result: " + response);
					return;
			    }
			}
	    } catch (UnknownHostException e) {
			System.out.println("error generated: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("error generated: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("error generated: " + e.getMessage());
		}	
	    }
		public boolean validateString(String s)
		{
			return s.matches(".*\\d+.*");
		}
		
	}
	
	public static void send(Object proposalOverlay, String hostname, int port) throws SocketException
	{
		DatagramSocket socket= new DatagramSocket();
		try{
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(bStream); 
			oo.writeObject(proposalOverlay);
			oo.close();
			byte[] sendingBytes = bStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendingBytes, sendingBytes.length, InetAddress.getByName(hostname),port);
			socket.send(sendPacket);
		}
			// Set the timeout to be 5 seconds for an unresponsive server.
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		finally{
			socket.close();
		}
	}
	private static int propose(Integer value) throws SocketException, InterruptedException
	{
		(new Thread(new Server())).start();
		Thread.sleep(200);
		for(HostPorts hostPort: peers)
		 {
			 if(hostPort.getHostIndex()!=me){
				 if(recStatus.get(hostPort.getHostIndex()).compareTo(RecStatus.UNTRIED) == 0)
					send(new ClientPropose(value.intValue()), hostPort.getHostName(), hostPort.getPort());
			 }
		 }
		while(!allReceived()){
			
		}
		Integer max = null;
		if(S.isEmpty() || S == null)
			max = new Integer(Integer.MIN_VALUE);
		else
			max = Collections.max(S);

		decided = Math.max(value, max);
		System.out.println(" Setting decided to " + decided + " chosen from max of set: " + S + " U " + value);
		return decided;
	}
	
	public static boolean allReceived()
	{
		boolean result = true;
		for(HostPorts hostPort: peers)
		{
			if(hostPort.getHostIndex()!= me)
				if(recStatus.get(hostPort.getHostIndex()).compareTo(RecStatus.REC) != 0)
					result = false;
		}
		return result;
	}
	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException
	{
		portNumber = null;
		hostFile = null;
		maxCrashes = null;
		ParseResults parseResults = ArgumentParser.parse(args, 	Utility.maxNumReplicas);
		portNumber = parseResults.portNumber;
		hostFile = parseResults.hostFile;
		maxCrashes = parseResults.maxCrashes;
		peers = new ArrayList<HostPorts>();
		Path path = Paths.get(System.getProperty("user.dir"));
		String [][] hostPorts =Utility.readConfigFile(path.getParent().getParent() + hostFile);
		String hostname = InetAddress.getLocalHost().getHostAddress();
		recStatus = new ConcurrentHashMap<Integer,RecStatus>();
		for(int a=0; a<hostPorts.length ; a++)
		{	
			if(hostPorts[a][1] == null)
				break;
			HostPorts newHostPort = new HostPorts(Integer.parseInt(hostPorts[a][0]), hostPorts[a][1], portNumber);
			String match =  InetAddress.getByName(hostPorts[a][1]).getHostAddress();
			if(hostPorts[a][1].toLowerCase().trim().equalsIgnoreCase("localhost"))
				match =  InetAddress.getLocalHost().getHostAddress();
			if(match.equalsIgnoreCase(hostname))
				me = new Integer(hostPorts[a][0]);
			peers.add(newHostPort);
			recStatus.put(new Integer(hostPorts[a][0]), RecStatus.UNTRIED);
		}
		if(me == null)
		{
			System.out.println("Error: this process's address and port is not registered in peer group list located in configs.txt");
			System.exit(-1);
		}	
		proposal = me;
		propose(proposal);
	}
}
