package step1;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import common.Utility.ArgumentParser;
import common.Utility;
import common.Utility.HostPorts;
import common.Utility.ParseResults;

//Code comments have been only included in step4 folder for corresponding file name
//The comments are the same as the project builds up on itself at each step.
public class Leader {
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	public static int me; 
	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException
	{
		System.out.println("Preparing to execute step #1 of project requirement..\n");
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
		Utility.ArgumentParser.validateMaxCrashes(maxCrashes, numProcs);
		Utility.createStartShellScript(peers,hostFile, maxCrashes);
		Utility.createKillShellScript(peers);
		System.out.println("Killing existing programs on the multiple hosts.. \n");
		Utility.killAll();
		System.out.println("\nStarting program on the multiple hosts.. \n");
		Utility.kickstart();
		System.out.println("\nAll processes are awake. Check deliverables/step1/logs for more info.");
	}
}
