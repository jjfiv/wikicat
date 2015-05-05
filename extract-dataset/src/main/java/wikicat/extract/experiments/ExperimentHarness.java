package wikicat.extract.experiments;

import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.Constants;
import wikicat.extract.util.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			bySplit.add(asQueries.subList(Constants.SplitSize * i, Constants.SplitSize * (i+1)));
		}

		return bySplit;
	}

	/** Run experiment for each split with list of query pages appropriately. */
	public static void runExperiment(Parameters argp, Experiment experiment) throws IOException {
		List<List<PageQuery>> queriesBySplit = loadQueryPages(argp.get("query-split-defs", "list.txt"));
		for (int splitId = 0; splitId < queriesBySplit.size(); splitId++) {
			List<PageQuery> pagesInSplit = queriesBySplit.get(splitId);
			experiment.run(splitId, pagesInSplit);
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
