sh ./killall.sh
sleep 5  
echo " Leader program started.. " 
ssh -T xinu11.cs.purdue.edu <<'ENDSSH0' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH0
ssh -T xinu20.cs.purdue.edu <<'ENDSSH1' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH1
ssh -T xinu2.cs.purdue.edu <<'ENDSSH2' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH2
ssh -T xinu5.cs.purdue.edu <<'ENDSSH3' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH3
ssh -T xinu8.cs.purdue.edu <<'ENDSSH4' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2 -k
ENDSSH4
ssh -T xinu7.cs.purdue.edu <<'ENDSSH5' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2 -k
ENDSSH5
ssh -T xinu13.cs.purdue.edu <<'ENDSSH6' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH6
ssh -T xinu14.cs.purdue.edu <<'ENDSSH7' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH7
ssh -T xinu16.cs.purdue.edu <<'ENDSSH8' &
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH8
ssh -T xinu17.cs.purdue.edu <<'ENDSSH9' 
cd /u/antor/u7/ravi18/LeaderElection/deliverables/step4
java -jar Process.jar  -p 1200 -h /configs.txt -f 2
ENDSSH9
