package wikicat.extract.experiments;

import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Results;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.catgraph.LoadCategoryGraph;
import wikicat.extract.util.IO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author jfoley.
 */
public class RankCategoriesDirectly extends Experiment {
	private final LocalRetrieval retrieval;

	public RankCategoriesDirectly(Parameters argp) throws Exception {
		super(argp);
		this.retrieval = new LocalRetrieval(argp.get("cat-index", "cat.title-only.galago"));
	}

	@Override
	public void run(int splitId, List<ExperimentHarness.PageQuery> pagesInSplit) {
		String outputFileName = argp.get("output", "title-only.combine")+".split"+splitId+".trecrun";
		System.err.println("# Writing output to "+outputFileName);

		try (PrintWriter output = IO.printWriter(outputFileName)) {
			for (ExperimentHarness.PageQuery q : pagesInSplit) {
				List<String> terms = tokenizePageTitle(q.title);
				System.err.println(terms);
				Node queryNode = new Node(argp.get("cat-galago-op", "combine")); // default to unigrams
				for (String term : terms) {
					queryNode.addChild(Node.Text(term));
				}
				Results results = retrieval.transformAndExecuteQuery(queryNode);
				for (ScoredDocument sdoc : results.scoredDocuments) {
					sdoc.documentName = LoadCategoryGraph.cleanCategory(sdoc.documentName).replaceAll("\\s+", "_");
					output.println(sdoc.toTRECformat(q.qid()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
