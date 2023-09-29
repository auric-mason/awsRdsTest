#!/bin/bash
export url=$1;
export dbuser=$2;
export dbpassword=$3;
testSize=$4;
concurrent=10;
for ((i=0; i<concurrent; i++))
do
  if [[ $VAR -lt 1 ]]
  then
    java -jar target/awsRdsTest.jar true &
  else
    java -jar target/awsRdsTest.jar &
  fi
done