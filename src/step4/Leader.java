package step4;


import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import step4.Process.Elect;
import common.Utility.ArgumentParser;
import common.Utility;
import common.Utility.HostPorts;
import common.Utility.ParseResults;
public class Leader {
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	public static Integer me; 
	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException
	{
		me = new Integer(0);
		portNumber = null;
		hostFile = null;
		maxCrashes = null;
		ParseResults parseResults = ArgumentParser.parse(args, 	Utility.maxNumReplicas);
		portNumber = parseResults.portNumber;
		hostFile = parseResults.hostFile;
		maxCrashes = parseResults.maxCrashes;
		
		List<HostPorts> peers = new ArrayList<HostPorts>();
		int numProcs = 0;
		Path path = Paths.get(System.getProperty("user.dir"));
		System.out.println("Loading configurations from -- " + path.getParent().getParent() + hostFile + "\n");
		String [][] hostPorts =Utility.readConfigFile(path.getParent().getParent() + hostFile);
		for(int a=0; a<hostPorts.length ; a++)
		{	
			numProcs++;
			if(hostPorts[a][1] == null)
				break;
			HostPorts newHostPort = new HostPorts(Integer.parseInt(hostPorts[a][0]), hostPorts[a][1], portNumber);
			peers.add(newHostPort);
		}
		HostPorts hostPort = Utility.findPeerIndex(InetAddress.getLocalHost(), peers);
		if(hostPort!=null)
			me = new Integer(hostPort.getHostIndex());
		Utility.ArgumentParser.validateMaxCrashes(maxCrashes, numProcs);
		List<HostPorts> killList = new ArrayList<HostPorts>();
		for(HostPorts p : peers)
			killList.add(p);
		Collections.shuffle(killList);
		Utility.createStartShellScript(peers,hostFile, maxCrashes, killList.subList(0, maxCrashes));
		Utility.createKillShellScript(peers);
	}
}