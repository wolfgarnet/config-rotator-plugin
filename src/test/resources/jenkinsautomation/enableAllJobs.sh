#!/bin/bash

remoteJenkinsUrl="http://77.66.14.3:8080"
jenkinscli="jenkins-cli.jar"
cmd="java -jar $jenkinscli -s $remoteJenkinsUrl"

echo "$cmd groovy pollingissuetest.groovy"
$cmd groovy pollingissuetest.groovy 
