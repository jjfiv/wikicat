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

indexes = []
for method in ["n100", "n10", "n5", "p10", "p5", "c10", "c5", "title-only"]:
    indexes += ["cat.%s.galago" % method]
    print("cat.%s.trectext.gz:" % method)
    print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} wikicat.extract.catgraph.BuildCategoryIndex --method=%s --output=$@" % method)
    print

print(".PHONY: indexes")
print("indexes: " + ' '.join(indexes))

