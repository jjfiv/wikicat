JAVA:='/home/jfoley/bin/java8/bin/java'
JAR:='extract-dataset/target/extract-dataset-1.0-SNAPSHOT.jar'

%.galago: %.trectext.gz
	${JAVA} -cp ${JAR} org.lemurproject.galago.core.tools.App build --inputPath=$< --indexPath=$@

.PHONY: indices
indices: cat.c10.galago cat.c5.galago cat.n10.galago cat.n5.galago cat.p10.galago cat.p5.galago cat.title-only.galago
