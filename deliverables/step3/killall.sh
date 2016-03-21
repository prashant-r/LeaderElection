 echo ' Killing Process on host with index # 1 '
ssh -T xinu11.cs.purdue.edu <<'ENDSSH0' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH0
 echo ' Killing Process on host with index # 2 '
ssh -T xinu20.cs.purdue.edu <<'ENDSSH1' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH1
 echo ' Killing Process on host with index # 3 '
ssh -T xinu2.cs.purdue.edu <<'ENDSSH2' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH2
 echo ' Killing Process on host with index # 4 '
ssh -T xinu5.cs.purdue.edu <<'ENDSSH3' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH3
 echo ' Killing Process on host with index # 5 '
ssh -T xinu8.cs.purdue.edu <<'ENDSSH4' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH4
 echo ' Killing Process on host with index # 6 '
ssh -T xinu7.cs.purdue.edu <<'ENDSSH5' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH5
 echo ' Killing Process on host with index # 7 '
ssh -T xinu13.cs.purdue.edu <<'ENDSSH6' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH6
 echo ' Killing Process on host with index # 8 '
ssh -T xinu14.cs.purdue.edu <<'ENDSSH7' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH7
 echo ' Killing Process on host with index # 9 '
ssh -T xinu16.cs.purdue.edu <<'ENDSSH8' &
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH8
 echo ' Killing Process on host with index # 10 '
ssh -T xinu17.cs.purdue.edu <<'ENDSSH9' 
jps -l | grep Process.jar | awk '{print $1}' | xargs kill -9
ENDSSH9
