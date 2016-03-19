package step1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
	public static Set<Integer> S = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
	public static volatile Integer decided;
	public static ConcurrentHashMap<Integer, Status> recStatus;
	public static Integer me;
	private static final String logfilePath = System.getProperty("user.dir") + "/log/procs";
	public static Integer proposal;
	public static FileHandler fh;
	public enum Status{
		REC, TIMEOUT, UNTRIED
	}	
	public static void configureLogger(Logger log)
	{

    try {  

        // This block configure the logger with handler and formatter  
    	fh = new FileHandler(logfilePath + me +".log",false);  
        Logger globalLogger = Logger.getLogger("global");
        Handler[] handlers = globalLogger.getHandlers();
        for(Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }
        log.setUseParentHandlers(false);
        log.addHandler(fh);
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
    } catch (SecurityException e) {  
        log.info(e.getMessage());  
    } catch (IOException e) {  
        log.info(e.getMessage());
    }  
}
	
	public static class Server implements Runnable
	{
	private static void receive(int port)
	{
		DatagramSocket socket; 
		try{
			socket = new DatagramSocket(port);
			byte[] recData = new byte[1024];
			while(true)
			{			
				try{
					DatagramPacket recPacket = new DatagramPacket(recData,recData.length);
					socket.receive(recPacket);
					new Thread(new PacketExecutor(recPacket.getAddress(),Arrays.copyOf(recData, recData.length) )).start();
					Arrays.fill(recData, (byte) 0 );
					
					// Reset the byte array - very important
					// Collect the response and send it back to client
					}
				catch(Exception e)
				{
					log.info("Socket error occured with cause" + e.getMessage());
				}
			}
		}
		catch(Exception e)
		{	
			log.info("Server connection open on port "  + " exited with " + e.getMessage());
		}
		}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		receive(portNumber);
		
	}
	}
	public static class ClientPropose implements Serializable
	{
		private static final long serialVersionUID = 1L;
		ClientPropose(int data)
		{
			this.data = data;
		}
		public int data;
	}
	public static class ClientReply implements Serializable
	{
		private static final long serialVersionUID = 1L;
		ClientReply(int data)
		{
			this.data = data;
		}
		public int data;
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
			HostPorts peer = findPeerIndex(inetAddress);
			if((peer == null))
			{
				response = "403 FORBIDDEN";
				log.info("Received at client" + me + " from client process - Unknown  result: " + response);
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
			    	recStatus.put(peer.getHostIndex(), Status.REC);
			    	send( new ClientReply(proposal), peer.getHostName() , portNumber);
			    } else if (recObj instanceof ClientReply)
			    {
			    	ClientReply incomingProposal = (ClientReply) recObj;  
			    	Integer value = new Integer(incomingProposal.data);
					S.add(value);
			    	recStatus.put(peer.getHostIndex(), Status.REC);
			    }
			    else
			    {
			    	response = "400 BAD REQUEST";
					log.info("Received at client" + me + " from client process - " + peer +  "-->" + data + " result: " + response);
					return;
			    }
			}
	    } catch (UnknownHostException e) {
			log.info("error generated: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.info("error generated: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("error generated: " + e.getMessage());
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
			socket.setSoTimeout(300);
			socket.connect(InetAddress.getByName(hostname), port);
			socket.send(sendPacket);
		}
			// Set the timeout to be 5 seconds for an unresponsive server.
		catch(Exception e)
		{
			log.info(e.getMessage());
		}
		finally{
			socket.close();
		}
	}
	private static int propose(Integer value) throws SocketException
	{
		(new Thread(new Server())).start();
		for(HostPorts hostPort: peers)
		 {
			 if(hostPort.getHostIndex()!=me){
			 log.info("me " + me + "value to send to " + hostPort.getHostIndex() );
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
		log.info(" Setting decided to " + decided + " chosen from max of set: " + S + " U " + value);
		fh.close();
		return decided;
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
	
	public static HostPorts findPeerIndex(InetAddress inetAddresss) throws UnknownHostException
	{
		String hostname = inetAddresss.getHostAddress();
		HostPorts hostPortVal = null;
		for(HostPorts hostPort: peers)
		{	
			String match =  InetAddress.getByName(hostPort.getHostName()).getHostAddress();
			if(hostPort.getHostName().toLowerCase().trim().equalsIgnoreCase("localhost"))
				match =  InetAddress.getLocalHost().getHostAddress();
			if(match.equalsIgnoreCase(hostname))
				hostPortVal = hostPort;
		}
		return hostPortVal;
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
		Path path = Paths.get(System.getProperty("user.dir"));
		String [][] hostPorts =Utility.readConfigFile(path.getParent().getParent() + hostFile);
		String hostname = InetAddress.getLocalHost().getHostAddress();
		recStatus = new ConcurrentHashMap<Integer,Status>();
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
		}
		if(me == null)
		{
			System.out.println("Error: this process's address and port is not registered in peer group list located in configs.txt");
			System.exit(-1);
		}	
		log= Logger.getLogger("Process #" + me);
		configureLogger(log);
		log.info("Process #" + me + " started!");
		proposal = me;
		propose(proposal);
	}
}
