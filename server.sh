#!/bin/bash
javac Server.java

if [ $# -eq 3 ]
then
  java Server $1 $2 $3
else
  echo "input all arguments"
fi
