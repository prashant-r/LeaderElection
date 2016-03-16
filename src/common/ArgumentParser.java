package common;

import java.io.File;
import java.io.IOException;

public class ArgumentParser {
	public static void parse(String args[], Integer portNumber, String hostFile, Integer maxCrashes)
	{
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
			
			System.out.println("Things look good!");
			System.exit(1);
			
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
