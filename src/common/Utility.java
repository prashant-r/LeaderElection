package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Utility {

	public static final int maxNumReplicas = 10;
	public static final int hostPortColumn = 2;
	private static final String logfilePath = System.getProperty("user.dir") + "/procs.log";
	private static final String kickstartPath = System.getProperty("user.dir") + "/kickstart.sh";
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
		fa.setFile(logfilePath);
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		//add appender to any Logger (here is root)
		log.addAppender(fa);
		//repeat with all other desired appenders
		log.setAdditivity(false);
	}
	
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
	
	public static void writeToFile(StringBuilder sb) throws IOException
	{
		
		FileUtils.writeStringToFile(new File(kickstartPath),sb.toString());
	}
	
	public static void createShellScript(List<HostPorts> hostPorts, String hostFile, Integer maxCrashes) throws IOException
	{
		StringBuilder tmp = new StringBuilder(); // Using default 16 character size
		int counter = 0;
		String prepend = "ENDSSH";
		// count up timer for 5 seconds
		tmp.append("for i in {1..5}; do ");
		tmp.append(System.getProperty("line.separator"));
		tmp.append("	printf \'\\r%2d\' $i");
		tmp.append(System.getProperty("line.separator"));
		tmp.append("	sleep 1");
		tmp.append(System.getProperty("line.separator"));
		tmp.append("done");
		tmp.append(System.getProperty("line.separator"));
		for(HostPorts hostPort: hostPorts)
		{
			tmp.append("ssh " + hostPort.getHostName());
			tmp.append(" <<" + "\'" + prepend + (counter) + "\'");
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
		Utility.writeToFile(tmp);
	}
}
