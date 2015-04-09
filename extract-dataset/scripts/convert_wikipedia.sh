#!/bin/bash
set -eu

INPUT_DIR=$1
JAR='target/extract-dataset-1.0-SNAPSHOT.jar'

ITER=0
for x in $(ls ${INPUT_DIR}); do
  OUT="wiki.$ITER.zip"
  if [ ! -f ${OUT} ]; then
    echo qsub -b y -l mem_free=4G -l mem_token=4G -o convert.$ITER.out -e convert.$ITER.err -cwd java -ea -Xmx3G -jar ${JAR} --fn=wikipedia-to-html --input=${INPUT_DIR}/$x --output=${OUT}
  fi
  ITER=$(expr $ITER + 1)
done;

