package step1;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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
	private static final String kickstartFilePath = System.getProperty("user.dir") + "/kickstart.sh";
	private static final String killAllFilePath = System.getProperty("user.dir") + "/killall.sh";
	private static final String kickstartDirPath = System.getProperty("user.dir");
	private static final String logfilePath = System.getProperty("user.dir") + "/log/procs";

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
		log= Logger.getLogger("Leader");
		configureLogger(log);
		createStartShellScript(peers,hostFile, maxCrashes);
		createKillShellScript(peers);
		System.out.println("Killing existing programs on the multiple hosts.. \n");
		//killAll();
		System.out.println("Starting program on the multiple hosts.. \n");
		kickstart();
		System.out.println("\n All processes are awake. Check deliverables/step1/proclogs for more info.");
	}
	public static void configureLogger(Logger log)
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
		fa.setFile(logfilePath + me +".log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		//add appender to any Logger (here is root)
		log.addAppender(fa);
		//repeat with all other desired appenders
		log.setAdditivity(false);
	}

	public static void createKillShellScript(List<HostPorts> hostPorts) throws IOException
	{
		StringBuilder tmp = new StringBuilder(); // Using default 16 character size
		int counter = 0;
		String prepend = "ENDSSH";
		for(HostPorts hostPort: hostPorts)
		{
			tmp.append("ssh -T " + hostPort.getHostName());
			tmp.append(" <<" + "\'" + prepend + (counter) + "\' &");
			tmp.append(System.getProperty("line.separator"));
			tmp.append("jps | grep jar | awk \'{print $1}\' | xargs kill -9");
			tmp.append(System.getProperty("line.separator"));
			tmp.append(prepend + (counter++));
			tmp.append(System.getProperty("line.separator"));
		}
		writeToFile(tmp,killAllFilePath);
	}
	public static void createStartShellScript(List<HostPorts> hostPorts, String hostFile, Integer maxCrashes) throws IOException
	{
		StringBuilder tmp = new StringBuilder(); // Using default 16 character size
		int counter = 0;
		String prepend = "ENDSSH";
		for(HostPorts hostPort: hostPorts)
		{
			tmp.append("ssh -T " + hostPort.getHostName());
			tmp.append(" <<" + "\'" + prepend + (counter) + "\' &");
			tmp.append(System.getProperty("line.separator"));
			tmp.append("cd " + System.getProperty("user.dir"));
			tmp.append(System.getProperty("line.separator"));
			tmp.append("java -jar Process.jar");
			tmp.append(" -p " + hostPort.getPort() );
			tmp.append(" -h " + hostFile);
			tmp.append(" -f " + maxCrashes);
			tmp.append(System.getProperty("line.separator"));
			tmp.append(prepend + (counter++));
			tmp.append(System.getProperty("line.separator"));
		}
		writeToFile(tmp, kickstartFilePath);
	}
	public static void writeToFile(StringBuilder tmp, String filePath) throws IOException
	{
		byte data[] = tmp.toString().getBytes();
		Path file = Paths.get(filePath);
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		//add owners permission
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		//add group permissions
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		//add others permissions
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_WRITE);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);
		Files.write(file, data);
		Files.setPosixFilePermissions(file, perms);
	}

	public static void kickstart() throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder("./kickstart.sh");
		pb.directory(new File(kickstartDirPath));
		java.lang.Process p = pb.start();
		p.waitFor(15, TimeUnit.SECONDS);
	}

	public static void killAll() throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder("./killall.sh");
		pb.directory(new File(kickstartDirPath));
		java.lang.Process p = pb.start();
		p.waitFor();
	}

	public static void loadingBar() throws IOException, InterruptedException
	{
		String anim= "|/-\\";
		for (int x =0 ; x < 100 ; x++){
			String data = "\r" + anim.charAt(x % anim.length())  + " " + x + "%";
			System.out.write(data.getBytes());
			Thread.sleep(100);
		}
	}
}
