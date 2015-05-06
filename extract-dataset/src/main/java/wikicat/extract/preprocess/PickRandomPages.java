package wikicat.extract.preprocess;

import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.FixedSizeMinHeap;
import wikicat.extract.Constants;
import wikicat.extract.util.IO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jfoley.
 */
public class PickRandomPages {
	public static void main(String[] args) throws IOException {
		final Set<String> allPages = new HashSet<>();
		IO.forEachLine(IO.fileReader("data/graph.tsv.gz"), new IO.StringFunctor() {
			@Override
			public void process(String input) {
				String[] pages = input.split("\t");
				for (String page : pages) {
					if (page.startsWith("Category:")) continue;
					String pN = page.trim().replaceAll("\\s+", "_");
					if(pN.isEmpty()) continue;
					allPages.add(pN);
				}
			}
		});

		try (PrintWriter out = IO.printWriter("queries.txt")) {

			FixedSizeMinHeap<ScoredDocument> randPages = new FixedSizeMinHeap<>(ScoredDocument.class, Constants.NumSplits * Constants.SplitSize, new ScoredDocument.ScoredDocumentComparator());

			for (String page : allPages) {
				randPages.offer(new ScoredDocument(page, -1, Math.random()));
			}
			java.util.List<ScoredDocument> unsortedList = randPages.getUnsortedList();
			for (int i = 0; i < unsortedList.size(); i++) {
				ScoredDocument sdoc = unsortedList.get(i);
				out.printf("%d\t%s\n", i+1, sdoc.documentName);
			}
		}
	}
}
