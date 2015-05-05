package wikicat.extract.catgraph;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.IO;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * @author jfoley.
 */
public class BuildCategoryIndex {
	public static void main(String[] args) throws Exception {
		Parameters argp = Parameters.create();
		Map<String, CategoryNode> graph = LoadCategoryGraph.load(argp.get("graph", "data/graph.tsv.gz"));

		for (String method : Arrays.asList("title-only", "p10", "c10", "p5", "c5", "n10", "n5")) {
			String outputName = String.format("cat.%s.trectext.gz", method);
			File outF = new File(outputName);
			if(outF.exists() && outF.isDirectory()) {
				System.err.println("# skipping "+outputName);
				continue;
			}

			System.err.println("Begin build: " + outputName);
			try (PrintWriter out = IO.printWriter(outputName)) {
				for (CategoryNode categoryNode : graph.values()) {
					Document title = new Document();
					title.name = categoryNode.name;

					StringBuilder text = new StringBuilder();
					text.append(categoryNode.name).append('\n');
					if (method.equals("title-only")) {
						// just title.
					} else if (method.startsWith("p")) {
						int count = Integer.parseInt(method.substring(1));
						for (String node : categoryNode.crawl(CategoryNode.CrawlType.PARENTS, count)) {
							text.append(node).append('\n');
						}
					} else if (method.startsWith("c")) {
						int count = Integer.parseInt(method.substring(1));
						for (String node : categoryNode.crawl(CategoryNode.CrawlType.CHILDREN, count)) {
							text.append(node).append('\n');
						}
					} else if (method.startsWith("n")) {
						int count = Integer.parseInt(method.substring(1));
						for (String node : categoryNode.crawl(CategoryNode.CrawlType.NEIGHBORS, count)) {
							text.append(node).append('\n');
						}
					} else {
						throw new RuntimeException(method);
					}

					title.text = text.toString();

					printDocument(out, title);
				}
			}
		}
	}

	private static void printDocument(PrintWriter out, Document doc) {
		out.println("<DOC>");
		out.println("<DOCNO>" + doc.name + "</DOCNO>");
		out.println("<TEXT>");
		out.println(doc.text);
		out.println("</TEXT>");
		out.println("</DOC>");
	}
}
