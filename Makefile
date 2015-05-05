JAVA:='/home/jfoley/bin/java8/bin/java'
JAR:='extract-dataset/target/extract-dataset-1.0-SNAPSHOT.jar'

%.galago: %.trectext.gz
	${JAVA} -cp ${JAR} org.lemurproject.galago.core.tools.App build --inputPath=$< --indexPath=$@

