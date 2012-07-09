#!/bin/bash

remoteJenkinsUrl="http://77.66.14.3:8080"
jenkinscli="jenkins-cli.jar"
cmd="java -jar $jenkinscli -s $remoteJenkinsUrl"
jobstemplatefolder="jobsxml"
for file in `ls -1 $jobstemplatefolder`; 
do
	# name without extension
    	name=${file%.*}
	for i in $(seq 1 10);
	do
		echo "$cmd create-job $name-j$i <$file"
		$cmd create-job $name-j$i <$jobstemplatefolder/$file
	done
done

echo "$cmd groovy pollingissuetest.groovy"
$cmd groovy pollingissuetest.groovy 
