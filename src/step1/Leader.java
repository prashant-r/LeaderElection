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
	public static int me;
	
	public static void createShellScript(List<HostPorts> hostPorts)
	{
		StringBuilder tmp = new StringBuilder(); // Using default 16 character size
		for(HostPorts hostPort: hostPorts)
		{
			tmp.append("ssh " + hostPort.getHostName());
			tmp.append(System.getProperty("line.separator"));
			tmp.append("java -jar Process.jar");
			tmp.append(" -p " + hostPort.getPort() );
			tmp.append(" -h " + hostFile);
			tmp.append(" -f " + maxCrashes);
			tmp.append(System.getProperty("line.separator"));
		}
		System.out.println(tmp.toString());
	}
		
	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException
	{
		System.out.println("Step #1 of project requirement..\n");
		portNumber = null;
		hostFile = null;
		maxCrashes = null;
		ParseResults parseResults = ArgumentParser.parse(args, 	Utility.maxNumReplicas);
		portNumber = parseResults.portNumber;
		hostFile = parseResults.hostFile;
		maxCrashes = parseResults.maxCrashes;
		List<HostPorts> peers = new ArrayList<HostPorts>();
		int numProcs = 0;
		System.out.println("Loading configurations from -- " + System.getProperty("user.dir") + hostFile);
		String [][] hostPorts =Utility.readConfigFile(System.getProperty("user.dir") + hostFile);
		for(int a=0; a<hostPorts.length ; a++)
		{	
			numProcs++;
			if(hostPorts[a][1] == null)
				break;
			HostPorts newHostPort = new HostPorts(Integer.parseInt(hostPorts[a][0]), hostPorts[a][1], portNumber);
			peers.add(newHostPort);
		}
		Utility.ArgumentParser.validateMaxCrashes(maxCrashes, numProcs);
		log= Logger.getLogger("Leader");
		Utility.configureLogger(log);
		createShellScript(peers);
		
	}
}
