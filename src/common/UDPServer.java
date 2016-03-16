package common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class UDPServer {

	final static Logger log = Logger.getLogger(UDPServer.class);
	private static HashMap<String, String> hash;
	static void configureLogger()
	{
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.ALL);
		console.activateOptions();
		//add appender to any Logger (here is root)
		log.addAppender(console);

		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile("log/project1/_udp_server.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		//add appender to any Logger (here is root)
		log.addAppender(fa);
		log.setAdditivity(false);
		//repeat with all other desired appenders
	}
	/*
	 * Logic for the UDP Server that runs endlessly
	 */
	
	public static void configure()
	{
		configureLogger();
		//hash serves as the dictionary
		hash = new HashMap<String, String>();

		if(args.length != 1)
		{
			log.fatal("Usage:- portnumber");
			System.exit(-1);
		}
		DatagramSocket socket; 
		try{
			socket = new DatagramSocket(Integer.parseInt(args[0]));
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
					String command = recString[0].toUpperCase();
					String key = recString[1].trim();
					String values = " ";
					for(int i = 2; i< recString.length; i++)
						values = values + recString[i] + " ";
					values = values.trim();
					// Based on the command act accordingly with the hash 
					switch(command.trim()){
					case "GET":
						if(hash.containsKey(key))
							response = hash.get(key);
						else
							{response = "No key "+ key + " matches db ";
							log.error("No key "+ key + " matches db ");}
						break;
					case "PUT":
						hash.put(key,values);
						response = "200 OK";
						break;
					case "DELETE":
						if(hash.containsKey(key)){
							hash.remove(key);
							response = "Key - " +  key + " has been removed ";}
						else
							response = "No such key "+ key +  "exists";
						break;
					default:
						response = "Invalid command " + command + " was sent";
						log.error(response);
						break;
					}
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
			log.error("Server connection open on port " + args[0] + " exited with " + e.getMessage());
		}

	}
	public static boolean validateString(String s)
	{
		return s.matches("\\A\\p{ASCII}*\\z");
	}

}