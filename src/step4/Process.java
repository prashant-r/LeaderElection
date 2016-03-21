package step4;


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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import common.Utility;
import common.Utility.ArgumentParser;
import common.Utility.ClientReply;
import common.Utility.HeartBeatBack;
import common.Utility.HostPorts;
import common.Utility.Kill;
import common.Utility.ParseResults;
import common.Utility.PrintFormat;
import common.Utility.PrintType;
import common.Utility.HeartBeat;
import common.Utility.ReElect;


public class Process {
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	public static List<HostPorts> peers;
	public static volatile Integer decided = null;
	public static volatile boolean decidedAltered = true;
	public static ConcurrentHashMap<Integer, Status> recStatus;
	public static ConcurrentHashMap<Integer, Status> pulseStatus;
	public static Integer me;
	public static volatile boolean crashed = false;
	public static Integer proposal;
	public enum Status{
		REC, TIMEOUT, UNTRIED
	}	
	public static class Server implements Runnable
	{
		private static void receive(int port)
		{
			DatagramSocket socket =null;
			try{
				socket = new DatagramSocket(port);
				byte[] recData = new byte[1024];
				while(!crashed)
				{			
					try{
						DatagramPacket recPacket = new DatagramPacket(recData,recData.length);
						socket.receive(recPacket);
						if(!crashed)
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
				if(socket!=null)
					socket.close();
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
				if (recObj instanceof ClientReply)
			    {
			    	ClientReply incomingProposal = (ClientReply) recObj;  
			    	Integer value = new Integer(incomingProposal.data);
			    	if(value > decided)
					{
						decided = value;
						decidedAltered = true;
					}
			    	recStatus.put(peer.getHostIndex(), Status.REC);
			    }
				else if (recObj instanceof HeartBeat)
				{
					 
					// System.out.println(" Received heartbeat at " + me + " from " + peer.getHostIndex());
					 send(new HeartBeatBack(),peer.getHostName(), peer.getPort() );
				}
				else if( recObj instanceof HeartBeatBack)
				{
					pulseStatus.put(peer.getHostIndex(), Status.REC);
				}
				else if (recObj instanceof ReElect)
				{
					System.out.println(new PrintFormat(me, decided, PrintType.CRASHED));
					try {
						decidedAltered = true;
						decided = new Integer(proposal);
						tResilience(proposal);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				else if( recObj instanceof Kill)
				{
					crashed = true;
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
	private static int tResilience(Integer value) throws SocketException, InterruptedException
	{
		for(int k = 0; k <= maxCrashes; k ++)
		{
			if(decidedAltered)
				broadcastMsg(new ClientReply(decided.intValue()));
			decidedAltered = false;
			Thread.sleep(1000);
		}		
		System.out.println(new PrintFormat(me, decided, PrintType.ELECTED));
		return decided;
	}
	
	public static void broadcastMsg(Object proposal) throws SocketException
	{
		for(HostPorts hostPort: peers)
		 {
			 if(hostPort.getHostIndex()!=me){
					 send(proposal, hostPort.getHostName(), hostPort.getPort());
			 }
		}
	}
	
	public static boolean allReceived()
	{
		boolean result = true;
		for(HostPorts hostPort: peers)
		{
			if(hostPort.getHostIndex()!= me)
				if(recStatus.get(hostPort.getHostIndex()).compareTo(Status.REC) != 0)
					result = false;
		}
		return result;
	}
	

	public static class HeartBeatClientThread extends Thread {
	    private long THREE_SECONDS = 3000;
	    
	    public void pulseMonitor(Object heartBeat) throws SocketException, InterruptedException
	    {
	    	
	    	
			Random r = new Random();
			//crash chance 1/60
			int randInt = r.nextInt(59);
			if ( randInt == 10){
				for(HostPorts hostPort: peers)
				{
					if(hostPort.getHostIndex()!=me){
						if(hostPort.getHostIndex() == decided.intValue())
						{
							send(new Kill(), hostPort.getHostName(), hostPort.getPort());
						}
					}
				}
				
			}
	    	for(HostPorts hostPort: peers)
	    		pulseStatus.put(hostPort.getHostIndex(), Status.UNTRIED);
	    	for(HostPorts hostPort: peers)
			 {
				 if(hostPort.getHostIndex()!=me){
					 if(pulseStatus.get(hostPort.getHostIndex()).compareTo(Status.REC)!=0)
						 send(heartBeat, hostPort.getHostName(), hostPort.getPort());
						 Thread.sleep(100);
						 if(pulseStatus.get(hostPort.getHostIndex()).compareTo(Status.REC)!=0)
						 {
							 if(hostPort.getHostIndex() == decided)
							 { // Leader is down start

								System.out.println(new PrintFormat(me, null, PrintType.START));
								decided = new Integer(proposal);
								broadcastMsg(new ReElect());
							 }
							 else
							 {
								 pulseStatus.put(hostPort.getHostIndex(), Status.TIMEOUT);
							 }
						 }
				 }
			}	
	    }
	    
	    public HeartBeatClientThread() throws IOException {
	        super("HeartBeatClientThread");
	    }
	    
	    @Override
	    public void run() {
	        while (true) {
	            try {
	            	pulseMonitor(new HeartBeat());
	            	// sleep for a while
	                try {
	                    sleep((long)(THREE_SECONDS));
	                } catch (InterruptedException e) { }
	            } catch (IOException e) {
	                e.printStackTrace();
	            } catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	    }
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
		if(!parseResults.kill){
		peers = new ArrayList<HostPorts>();
		Path path = Paths.get(System.getProperty("user.dir"));
		String [][] hostPorts =Utility.readConfigFile(path.getParent().getParent() + hostFile);
		String hostname = InetAddress.getLocalHost().getHostAddress();
		recStatus = new ConcurrentHashMap<Integer,Status>();
		pulseStatus = new ConcurrentHashMap<Integer, Status>();
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
			recStatus.put(new Integer(hostPorts[a][0]), Status.UNTRIED);
			pulseStatus.put(new Integer(hostPorts[a][0]), Status.UNTRIED);
		}
		if(me == null)
		{
			System.out.println("Error: this process's address and port is not registered in peer group list located in configs.txt");
			System.exit(-1);
		}
		
		//new HeartBeatClientThread().start();
		proposal = me;
		decided = new Integer(proposal);
		(new Thread(new Server())).start();
		new HeartBeatClientThread().start();
		Thread.sleep(2000);
		tResilience(proposal);
		}
	}
	
	
public static class Elect {

		
		public static void firstElection(Integer self) throws IOException, InterruptedException
		{
			System.out.println(new PrintFormat(self, null, PrintType.START));
			Utility.kickstart();
		}
	}
}
