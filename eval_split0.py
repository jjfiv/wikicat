import sys

runs = ' '.join(["--runs+"+x for x in sys.argv[1:]])

print("""
${JAVA_HOME}/bin/java -cp extract-dataset/target/extract-dataset-1.0-SNAPSHOT.jar org.lemurproject.galago.core.tools.App eval --comparisons=false --metrics+map --metrics+ndcg --metrics+p1 --metrics+p10 --judgments=split0.qrel %s
""" % (runs))
