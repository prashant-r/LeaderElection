package step1;

import common.ArgumentParser;

public class Leader {
	
	public static Integer portNumber;
	public static String hostFile;
	public static Integer maxCrashes;
	
	public void Leader()
	{
		
	}
	
	
	void propose()
	{
		
	}
	
	public static void main(String args[])
	{
		System.out.println("Step #1 of project requirement.. \n");
		portNumber = null;
		hostFile = null;
		maxCrashes = null;
		ArgumentParser.parse(args, portNumber, hostFile, maxCrashes);
		Leader leader = new Leader();
		leader.propose();
		
	}
}
