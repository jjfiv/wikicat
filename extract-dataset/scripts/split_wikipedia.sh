#!/bin/bash
set -eu

INPUT_DIR=$1
JAR='target/extract-dataset-1.0-SNAPSHOT.jar'

ITER=0
for x in $(ls ${INPUT_DIR}); do
  INPUT="wiki.$ITER.zip"
  echo qsub -b y -l mem_free=4G -l mem_token=4G -o split.$ITER.out -e split.$ITER.err -cwd java -ea -Xmx3G -cp ${JAR} wikicat.extract.SplitHTMLZip --input=${INPUT_DIR}/$x --output=${INPUT}
  ITER=$(expr $ITER + 1)
done;

