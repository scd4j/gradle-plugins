#!/bin/bash
time=10 # segundos
count=0
while [ $count -eq 0 ]; do
   date >> /opt/tmp/date.txt
   sleep $time
done
