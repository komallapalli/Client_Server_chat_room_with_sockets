#!/bin/bash
javac Client.java
if [ $# -eq 3 ]
then
  java Client $1 $2 $3
else
  echo "input all arguments"
fi
