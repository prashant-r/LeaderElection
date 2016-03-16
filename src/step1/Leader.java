package step1;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import common.Utility.ArgumentParser;
import common.Utility;
import common.Utility.HostPorts;
import common.Utility.ParseResults;

public class Leader {
	
	
	public static Logger log;
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	public static List<HostPorts> peers;
	public static int me;

	void propose()
	{
		log.info("ready");
	}
	
	
	public static void main(String args[]) throws NumberFormatException, IOException
	{
		System.out.println("Step #1 of project requirement.. \n");
		portNumber = null;
		hostFile = null;
		maxCrashes = null;
		ParseResults parseResults = ArgumentParser.parse(args);
		portNumber = parseResults.portNumber;
		hostFile = parseResults.hostFile;
		maxCrashes = parseResults.maxCrashes;
		Leader leader = new Leader();
		String [][] hostPorts =Utility.readConfigFile();
		peers = new ArrayList<HostPorts>();
		String hostname = InetAddress.getLocalHost().getHostAddress();
		Integer me = null;
		for(int a=0; a< hostPorts.length ; a++)
		{	
			HostPorts newHostPort = new HostPorts(Integer.parseInt(hostPorts[a][0]), hostPorts[a][1], Integer.parseInt(hostPorts[a][2]));
			String match =  InetAddress.getByName(hostPorts[a][1]).getHostAddress();
			if(hostPorts[a][1].toLowerCase().trim().equalsIgnoreCase("localhost"))
				match =  InetAddress.getLocalHost().getHostAddress();
			if(match.equalsIgnoreCase(hostname) &&  Integer.parseInt(hostPorts[a][2]) == portNumber)
				me = new Integer(a);
			peers.add(newHostPort);
		}
		if(me == null)
		{
			System.out.println("Error: this process's address and port is not registered in peer group list located in configs.txt");
			System.exit(-1);
		}	
		Utility.log= Logger.getLogger("Process #" + me);
		log = Utility.log;
		Utility.configureLogger();
		leader.propose();
		
	}
}
