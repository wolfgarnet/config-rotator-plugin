#!/bin/bash

remoteJenkinsUrl="http://77.66.14.3:8080"
jenkinscli="jenkins-cli.jar"
cmd="java -jar $jenkinscli -s $remoteJenkinsUrl"

echo "$cmd groovy enableAllJobs.groovy"
$cmd groovy enableAllJobs.groovy 
