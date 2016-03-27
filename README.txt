DEBUG TIPS:
If you get a permission denied on trying to execute kickstart.sh go the the file and right click -> properties and then go to permissions folder to change the access rights to read and write as well as checking the flag," execute as a program" to cicrcumvent this error.


RUN TIPS:

Find a cs machine to execute below code or even use ssh to log into one remotely but the files must
be placed on the xinu machines to execute below code.
The project is structured with having code in src folder and the resulting jar files from the 
compiled code in the deliverables folder.

To execute first go to the LeaderElection folder( main project folder)
and execute:

  "ant jar" (no quotes)

This will create JARs in the deliverables folder.

Next go to the deliverables folder

	"cd deliverables"(no quotes)
	
Next go the step that you are interested in testing. For instance step4 

	"cd step4" (no quotes)
	
Now run

	"java -jar Leader.jar -p 1200 -f 4 -h /configs.txt" 
	
Here the Leader program creates the start script for starting the program on the rest of the machines.
Also, the location of the configs file is relative to the LeaderElection folder. If you would like to modify location
make sure to change the -h flag in the above command line command. -p is port number, -f is max crashes tolerated, -h is the location of configs file.
Now, a kickstart.sh and killall.sh shell files are generated in the deliverables/step4 folder

To start the program on all the servers execute

	"sh kickstart.sh"
	
Please ignore that bash returns after executing this command, after waiting a couple seconds,
outputs begin to appear

To force kill the program on all the servers execute 
	"sh killall.sh" 
