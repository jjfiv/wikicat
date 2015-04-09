#!/bin/bash
## Originally by sjh
## submits lines of a run file to qsub while the number of currently running jobs is less than ${NJ}

files=$@
NJ=15

for f in $files; do
  while read line; do

    running=`qstat | grep $USER | wc -l`
    while [ $running -gt ${NJ} ]; do
      sleep 10
      running=`qstat | grep $USER | wc -l`
    done

    $line &
    sleep 0.1

  done < $f
done
