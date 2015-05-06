package wikicat.extract.experiments;

import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Results;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.utility.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Map<String, List<ScoredDocument>> run(int splitId, List<ExperimentHarness.PageQuery> pagesInSplit) {
		Map<String, List<ScoredDocument>> runsByQuery = new HashMap<>();

		for (ExperimentHarness.PageQuery q : pagesInSplit) {
			List<String> terms = tokenizePageTitle(q.title);
			System.err.println(argp.getLong("split")+" "+q.qid() + " " + terms);
			if(terms.isEmpty()) {
				continue;
			}
			Node queryNode = new Node(argp.getString("cat-galago-op"));
			for (String term : terms) {
				queryNode.addChild(Node.Text(term));
			}
			Results results = retrieval.transformAndExecuteQuery(queryNode);
			runsByQuery.put(q.qid(), results.scoredDocuments);
		}
		return runsByQuery;
	}

}
