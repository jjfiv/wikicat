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

#cat_indexing_methods = [
#    "n100", "n10", "n5",
#    "p10", "p5",
#    "c10", "c5",
#    "title-only"]
cat_indexing_methods = [
    "title-only",
    "c5", "p5"
    ]

indexes = []
for method in cat_indexing_methods:
    indexes += ["cat.%s.galago" % method]
    print("cat.%s.trectext.gz:" % method)
    print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} wikicat.extract.catgraph.BuildCategoryIndex --method=%s --output=$@" % method)
    print

print(".PHONY: indexes")
print("indexes: " + ' '.join(indexes))


#galago_operator_methods = [
#    "combine",
#    "sdm"
#]
galago_operator_methods = [
    "sdm"
]

# only need to run the *full* settings on the classifier train/validate
classifier_splits = [0,1,2,3,4]

runs = []
evals = []
for method in ["direct"]:
    for qm in galago_operator_methods:
        for split in classifier_splits:
            for cat_index_m in cat_indexing_methods:
                output = "split%d.%s.%s.%s.trecrun" % (split, method, cat_index_m, qm)
                runs += [output]
                cat_index = "cat.%s.galago" % (cat_index_m)
                print(output+":")
                print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} wikicat.extract.experiments.ExperimentHarness --experiment=%s --split=%d --cat-galago-op=%s --cat-index=%s --output=$@" %(method, split, qm, cat_index))
                print

                eval_output = "split%d.%s.%s.%s.eval" % (split, method, cat_index_m, qm)
                print(eval_output+": "+output)
                print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} org.lemurproject.galago.core.tools.App eval --comparisons=false --metrics+map --metrics+ndcg --metrics+p1 --metrics+p10 --judgments=split%d.qrel --runs+$< > $@" % (split))
                print
                evals += [eval_output]

rlfiles = []
for split in classifier_splits:
    trecruns = []
    for method in ["direct"]:
        for qm in galago_operator_methods:
            for cat_index_m in cat_indexing_methods:
                trecruns += [("split%d.%s.%s.%s.trecrun" % (split, method, cat_index_m, qm))]
    rlfile = "splits%d.rlinput" % (split)
    rlfiles += [rlfile]
    print("%s: %s" % (rlfile, ' '.join(trecruns)))
    print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} wikicat.extract.experiments.TrecRunReranker --judgments=split%d.qrel --output=$@ %s" % (split, ' '.join(['--input+'+x for x in trecruns])))
    print

models = []

for ranker_type in [0,3,4,8,9]:
  (trainSplit, validateSplit) = (0,1)
  model_output = "ranklib.r%d.model" % (ranker_type)
  models += [model_output]
  inputs = [rlfiles[trainSplit], rlfiles[validateSplit]]
  print("%s: %s" % (model_output, ' '.join(inputs)))
  print("\t${PREFIX} ${JAVA} ${JOPT} -cp ${JAR} ciir.umass.edu.eval.Evaluator -train %s -validate %s -ranker %d -metric2t map -save $@" % (inputs[0], inputs[1], ranker_type))
  print


print(".PHONY: runs")
print("runs: " + ' '.join(runs))
print
print(".PHONY: evals")
print("evals: " + ' '.join(evals))
print
print(".PHONY: models")
print("models: " + ' '.join(models))
print

