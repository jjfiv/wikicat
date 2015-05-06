import sys

runs = ' '.join(["--runs+"+x for x in sys.argv[1:]])

print("""
${JAVA_HOME}/bin/java -cp extract-dataset/target/extract-dataset-1.0-SNAPSHOT.jar org.lemurproject.galago.core.tools.App eval --judgments=split0.qrel %s
""" % (runs))
