package wikicat.extract.experiments;

import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.Constants;
import wikicat.extract.catgraph.LoadCategoryGraph;
import wikicat.extract.util.IO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public class ExperimentHarness {
	public static class PageQuery {
		public final String title;
		public final String qid;

		public PageQuery(String qid, String pageTitle) {
			this.title = pageTitle;
			this.qid = qid;
		}

		/** String qid for Galago. */
		public String qid() {
			return qid;
		}

	}
	public static List<List<PageQuery>> loadQueryPages(String splitDefFile) throws IOException {
		// As object:
		List<PageQuery> asQueries = new ArrayList<>();
		for (String line : IO.slurpLines(splitDefFile)) {
			String[] qRaw = line.split("\t");
			asQueries.add(new PageQuery(qRaw[0], qRaw[1]));
		}

		// Now split up:
		List<List<PageQuery>> bySplit = new ArrayList<>();
		for (int i = 0; i < Constants.NumSplits; i++) {
			List<PageQuery> curSplit = new ArrayList<>();
			for (PageQuery query : asQueries.subList(Constants.SplitSize * i, Constants.SplitSize * (i + 1))) {
				if(query.title.startsWith("Category:")) continue;
				curSplit.add(query);
			}
			bySplit.add(curSplit);
		}

		return bySplit;
	}

	/** Run experiment for this split with list of query pages appropriately. */
	public static void runExperiment(Parameters argp, Experiment experiment) throws IOException {
		String outputFileName = argp.getString("output");
		int splitId = (int) argp.getLong("split");
		List<List<PageQuery>> queriesBySplit = loadQueryPages(argp.get("query-split-defs", "list.txt"));

		// Get queries to run and run them:
		List<PageQuery> pagesInSplit = queriesBySplit.get(splitId);
		Map<String, List<ScoredDocument>> results = experiment.run(splitId, pagesInSplit);

		// write results to output file:
		try (PrintWriter output = IO.printWriter(outputFileName)) {
			for (Map.Entry<String, List<ScoredDocument>> kv : results.entrySet()) {
				String qid = kv.getKey();
				for (ScoredDocument sdoc : kv.getValue()) {
					sdoc.documentName = LoadCategoryGraph.cleanCategory(sdoc.documentName).replaceAll("\\s+", "_");
					output.println(sdoc.toTRECformat(qid));
				}
			}
		}

	}

	public static void run(Parameters argp) throws Exception {
		switch(argp.get("experiment", "direct")) {
			case "direct":
				runExperiment(argp, new RankCategoriesDirectly(argp));
			default:
				throw new RuntimeException(argp.getString("experiment"));
		}
	}

	public static void main(String[] args) throws Exception {
		run(Parameters.parseArgs(args));
	}
}
