#!/usr/bin/python2

import socket

host = socket.gethostname()

boilerplate = """
JAVA:=$(shell echo $(JAVA_HOME)/bin/java)
JOPT:=-ea -Xmx7G
JAR:='extract-dataset/target/extract-dataset-1.0-SNAPSHOT.jar'
"""
print(boilerplate)

if host == 'sydney.cs.umass.edu':
    print("PREFIX=qsub -b y -cwd -sync y -l mem_free=8G -l mem_token=8G -o $@.out -e $@.err ")
else:
    print("PREFIX:=")

# default rule
print(".PHONY: default")
print(".DEFAULT: default")
print("default:")
print("\t@echo ${PREFIX} ${JAVA}")
print


indexingRule = """
%.galago: %.trectext.gz
	${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} org.lemurproject.galago.core.tools.App build --inputPath=$< --indexPath=$@
"""
print(indexingRule)

cat_indexing_methods = [
    "n100", "n10", "n5",
    "p10", "p5",
    "c10", "c5",
    "title-only"]

indexes = []
for method in cat_indexing_methods:
    indexes += ["cat.%s.galago" % method]
    print("cat.%s.trectext.gz:" % method)
    print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} wikicat.extract.catgraph.BuildCategoryIndex --method=%s --output=$@" % method)
    print

print(".PHONY: indexes")
print("indexes: " + ' '.join(indexes))


galago_operator_methods = [
    "combine",
    "sdm"
]
splits = [0,1,2,3,4]

for method in ["direct"]:
    for qm in galago_operator_methods:
        for split in splits:
            for cat_index_m in cat_indexing_methods:
                output = "split%d.%s.%s.%s.trecrun" % (split, method, cat_index_m, qm)
                cat_index = "cat.%s.galago" % (cat_index_m)
                print(output+":")
                print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} wikicat.extract.experiments.ExperimentHarness --experiment=%s --split=%d --cat-galago-op=%s --cat-index=%s --output=$@" %(method, split, qm, cat_index))
                print

