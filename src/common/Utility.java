package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Utility {

	public static final int numReplicas = 10;
	public static final int hostPortColumn = 3;
	public static Logger log;
	private static final String configsPath = System.getProperty("user.dir") + "/configs.txt";
	private static final String logfilePath = System.getProperty("user.dir") + "/procs.log";
	public static void configureLogger()
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
	
	public static String[][] readConfigFile()
	{		
		String hostPorts [][] = new String[numReplicas][hostPortColumn];
		try {
			java.io.BufferedReader fileReader = new BufferedReader(new FileReader(configsPath));			
			int c = 0;

			while(c++!=numReplicas)
			{
				hostPorts[c-1] = fileReader.readLine().split("\\s+");
				if(hostPorts[c-1][1].isEmpty() || !hostPorts[c-1][2].matches("[0-9]+") || hostPorts[c-1][2].isEmpty() || !hostPorts[c-1][0].matches("[0-9]+") || hostPorts[c-1][0].isEmpty())
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
		public static ParseResults parse(String args[]) throws NumberFormatException, IOException
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
	        		System.out.println("No. of port must be [0,65535] | Your input was " + portNumber);
	        		System.exit(-1);
	        	}
				if(!validateHostFile(hostFile))
	        	{
	        		System.out.println("Host File must be valid | Your input was " + hostFile);
	        		System.exit(-1);
	        	}
				if(!validateMaxCrashes(maxCrashes))
	        	{
	        		System.out.println("MaxCrashes must be [0,10]| Your input was " + maxCrashes);
	        		System.exit(-1);
	        	}
				ParseResults parseResults = new ParseResults(portNumber, hostFile, maxCrashes);
				return parseResults;
		}
		/*
		 * Methods below are meant for parameter validation
		 */
		
		static boolean validatePortNumber(Integer portNumber)
		{
			if(portNumber!=null)
				if(portNumber>=0 && portNumber <= 65365)
					return true;
			return false;
		}
		
		static boolean validateHostFile(String hostFile)
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
		
		static boolean validateMaxCrashes(Integer maxCrashes)
		{
			if(maxCrashes!= null)
				if(maxCrashes >= 0 && maxCrashes <= 10)
					return true;
			return false;
		}

	}
	
}
