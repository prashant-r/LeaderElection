//package common;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//
//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.FileAppender;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
//
//public class UDPClient {
//
//	public static void main(String[] args) {
//
//		String sendProtocolString = "";
//		if(args.length < 4 )
//		{
//			log.fatal("Fatal error : Usage - hostname port instruction key value" );
//			System.exit(-1);
//		}
//		else if (args.length == 4)
//		{   
//			// Command is a GET or DELETE(args[2]) and args[3] is the key, command must be applied to.
//			sendProtocolString = args[2] + " " + args[3];
//		}
//		else
//		{// Command is a PUT(args[2]) and the other arguments value/s
//			sendProtocolString = args[2];
//			for(int a = 3; a <args.length; a++)
//				sendProtocolString += " "+args[a];
//		}
//		log.info("Client sent " + sendProtocolString + " to server -" + args[0] + " " + args[1]);
//		
//		try{
//			byte[] sendingBytes = sendProtocolString.getBytes();
//			byte[] recBytes = new byte[1024];
//			DatagramPacket sendPacket = new DatagramPacket(sendingBytes, sendingBytes.length, InetAddress.getByName(args[0]),Integer.parseInt(args[1]));
//			DatagramPacket receivePacket = new DatagramPacket(recBytes, recBytes.length);
//			DatagramSocket socket = new DatagramSocket();
//			socket.send(sendPacket);
//			// Set the timeout to be 5 seconds for an unresponsive server.
//			socket.setSoTimeout(5000);
//			while(true)
//			{
//				try{
//				socket.receive(receivePacket);
//				String receivedString = new String(receivePacket.getData());
//				if(validateString(receivedString))
//					log.info("Received from server at -" + args[0] + ":" + args[1] + " "+ receivedString);
//				else
//					log.info("MALFORMED response from server");
//				socket.close();
//				return;
//				}
//				catch(Exception e)
//				{
//					// Very important to return after an exception is caught. In the case of an exception due to timeout
//					// abort connection and exit code.
//					log.error("Datagram packet receive routine failed with " + e.getMessage());
//					return;
//				}
//				finally{
//					// In case an exception occured such as a timeout fail, abort connection and move on to next request.
//					socket.close();
//					
//				}
//				
//			}
//		}
//		catch(Exception e)
//		{
//			log.error("Datagram packet initialization throws error " + e.getMessage());
//		}
//			
//		}
//	public static boolean validateString(String s)
//	{
//		return s.matches("\\A\\p{ASCII}*\\z");
//	}
//}
