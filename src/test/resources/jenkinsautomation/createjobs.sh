#!/bin/bash

remoteJenkinsUrl="http://77.66.14.3:8080"
jenkinscli="jenkins-cli.jar"
cmd="java -jar $jenkinscli -s $remoteJenkinsUrl"
jobstemplatefolder="jobsxml"
for file in `ls -1 $jobstemplatefolder/template-Poll*`; 
do
	# name without extension and path
    	name=$(basename "$file")
    	name="${name%.*}"
	for i in $(seq 1 20);
	do
		echo "$cmd create-job $name-j$i <$file"
		$cmd create-job $name-j$i <$file
	done
done
