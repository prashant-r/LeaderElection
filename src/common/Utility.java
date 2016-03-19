package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utility {

	public static final int maxNumReplicas = 10;
	public static final int hostPortColumn = 2;
	private static final String kickstartFilePath = System.getProperty("user.dir") + "/kickstart.sh";
	private static final String killAllFilePath = System.getProperty("user.dir") + "/killall.sh";
	private static final String kickstartDirPath = System.getProperty("user.dir");
	private static final String logfilePath = System.getProperty("user.dir") + "/log/procs";
	public static FileHandler fh;
	public static String[][] readConfigFile(String hostFile)
	{		
		String hostPorts [][] = new String[maxNumReplicas][hostPortColumn];
		try {
			java.io.BufferedReader fileReader = new BufferedReader(new FileReader(hostFile));					
			int c = 0;
			String readString = "";
			while((readString = fileReader.readLine())!= null)
			{
				c++;
				hostPorts[c-1] = readString.split("\\s+");
				if(hostPorts[c-1][1].isEmpty() || !hostPorts[c-1][0].matches("[0-9]+") || hostPorts[c-1][0].isEmpty())
				{
					System.out.println("You have made incorrect entries for addresses in config file, please investigate.");
					System.exit(-1);
				}
			}
			fileReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			System.out.println("You have made incorrect entries for addresses in config file, please investigate.");
			System.exit(-1);
		}
		return hostPorts;
	}
	
	public static class HostPorts
	{
		int hostIndex;
		String hostName;
		int port;
		@Override
		public String toString() {
			return "HostPorts [hostIndex=" + hostIndex + ", hostName=" + hostName + ", port=" + port + "]";
		}
		public int getHostIndex() {
			return hostIndex;
		}
		public void setHostIndex(int hostIndex) {
			this.hostIndex = hostIndex;
		}
		public String getHostName() {
			return hostName;
		}
		public void setHostName(String hostName) {
			this.hostName = hostName;
		}
		public int getPort() {
			return port;
		}
		public HostPorts(int hostIndex,String hostName, int port) {
			super();
			this.hostName = hostName;
			this.port = port;
			this.hostIndex = hostIndex;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}
	
	public static class ParseResults
	{
		public int portNumber;
		public String hostFile;
		public int maxCrashes;
		public ParseResults(int portNumber, String hostFile, int maxCrashes) {
			super();
			this.portNumber = portNumber;
			this.hostFile = hostFile;
			this.maxCrashes = maxCrashes;
		}
	}
	
	
	public static class ArgumentParser {
		public static ParseResults parse(String args[], int actualNumReplicas) throws NumberFormatException, IOException
		{	
			
			Integer portNumber = null;
			String hostFile = null;
			Integer maxCrashes = null;	
			if(args.length==0 )
			{
				BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
				System.out.println("Enter the port number:");
				portNumber = Integer.parseInt(br.readLine());
				System.out.println("Enter the host file:");
				hostFile = br.readLine();
				System.out.println("Enter maximum crashes allowed:");
				maxCrashes = Integer.parseInt(br.readLine());
			}
			else{
			
			    for (int i = 0; i < args.length; i++) {
			        switch (args[i]) {
			        case "-p":
			        	portNumber = Integer.parseInt(args[++i]);
			            break;
			        case "-h":
			        	hostFile = args[++i];
			        	break;
			        case "-f":
			        	maxCrashes = Integer.parseInt(args[++i]);
			        	break;
			         default:
			         break;
			        }
			    }   
			}
			    if(!validatePortNumber(portNumber))
	        	{
	        		System.out.println("No. of port must be [1024,65535] | Your input was " + portNumber);
	        		System.exit(-1);
	        	}
				if(!validateHostFile(hostFile))
	        	{
	        		System.out.println("Host File must be valid | Your input was " + hostFile);
	        		System.exit(-1);
	        	}
				if(!validateMaxCrashes(maxCrashes, actualNumReplicas))
	        	{
	        		System.out.println("MaxCrashes must be [" + Math.min(actualNumReplicas-2, 0) + "," +(actualNumReplicas -2) + "]| Your input was " + maxCrashes);
	        		System.exit(-1);
	        	}
				ParseResults parseResults = new ParseResults(portNumber, hostFile, maxCrashes);
				return parseResults;
		}
		/*
		 * Methods below are meant for parameter validation
		 */
		
		public static boolean validatePortNumber(Integer portNumber)
		{
			if(portNumber!=null)
				if(portNumber>=1024 && portNumber <= 65535)
					return true;
			return false;
		}
		
		public static boolean validateHostFile(String hostFile)
		{
			if(hostFile!=null){
			 File f = new File(hostFile);
			  try {
			    f.getCanonicalPath();
			    return true;
			  } catch (IOException e) {
			    return false;
			  }
			}
			return false;
		}
		
		public static boolean validateMaxCrashes(Integer maxCrashes, int actualNumReplicas)
		{
			if(maxCrashes!= null)
				if(maxCrashes <= actualNumReplicas-2)
					return true;
			return false;
		}

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
			tmp.append("jps -l | grep Process.jar | awk \'{print $1}\' | xargs kill -9");
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
			tmp.append("java -jar Process.jar ");
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
		p.waitFor();
	}

	public static void killAll() throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder("./killall.sh");
		pb.directory(new File(kickstartDirPath));
		java.lang.Process p = pb.start();
		p.waitFor(5, TimeUnit.SECONDS);
	}

	public static void loadingBar(int time) throws IOException, InterruptedException
	{
		String anim= "|/-\\";
		for (int x =0 ; x < 100 ; x++){
			String data = "\r" + anim.charAt(x % anim.length())  + " " + x + "%";
			System.out.write(data.getBytes());
			Thread.sleep(time);
		}
	}
	
	public static void configureLogger(Logger log, int clientId)
	{

    try {  

        // This block configure the logger with handler and formatter  
    	fh = new FileHandler(logfilePath + clientId +".log",false);  
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
	
	public static class ClientPropose implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public ClientPropose(int data)
		{
			this.data = data;
		}
		public int data;
	}
	public static class ClientReply implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public ClientReply(int data)
		{
			this.data = data;
		}
		public int data;
	}
}
