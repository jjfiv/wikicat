package wikicat.extract.preprocess;

import wikicat.extract.catgraph.LoadCategoryGraph;
import wikicat.extract.experiments.ExperimentHarness;
import wikicat.extract.util.IO;
import wikicat.extract.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author jfoley.
 */
public class MakeQrelFiles {
	public static void main(String[] args) throws IOException {

		final Map<String, List<String>> categoriesForPage = new HashMap<>();
		IO.forEachLine(IO.fileReader("data/graph.tsv.gz"), new IO.StringFunctor() {
			@Override
			public void process(String input) {
				String[] col = input.split("\t");
				if(col.length != 2) return;
				// skip category->category relations.
				if(col[0].startsWith("Category:")) return;

				// keep only relations between pages and their categories.
				String page = LoadCategoryGraph.cleanCategory(col[0]);
				String cat = LoadCategoryGraph.cleanCategory(col[1]);
				Util.extendListInMap(categoriesForPage, page, cat);
			}
		});

		List<List<ExperimentHarness.PageQuery>> loadQueryPages = ExperimentHarness.loadQueryPages("list.txt");

		for (int splitId = 0; splitId < loadQueryPages.size(); splitId++) {
			try (PrintWriter out = IO.printWriter("split"+splitId+".qrel")) {
				List<ExperimentHarness.PageQuery> splitQueries = loadQueryPages.get(splitId);
				for (ExperimentHarness.PageQuery query : splitQueries) {
					String pageTitle = LoadCategoryGraph.cleanCategory(query.title);
					List<String> relCategories = categoriesForPage.get(pageTitle);
					if(relCategories == null) continue;
					for (String category : relCategories) {
						out.printf("%s 0 %s 1\n", query.qid(), category);
					}
				}
			}

		}
	}
}
