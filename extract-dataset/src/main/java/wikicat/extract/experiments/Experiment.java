package wikicat.extract.experiments;

import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.tokenize.Tokenizer;
import org.lemurproject.galago.utility.Parameters;

import java.util.List;
import java.util.Map;

/**
 * @author jfoley.
 */
public abstract class Experiment {
	protected final Tokenizer tokenizer;
	protected final Parameters argp;

	public Experiment(Parameters argp) {
		this.argp = argp;
		this.tokenizer = Tokenizer.create(argp);
	}
	public abstract Map<String, List<ScoredDocument>> run(int splitId, List<ExperimentHarness.PageQuery> pagesInSplit);


	public List<String> tokenizePageTitle(String input) {
		return tokenizer.tokenize(input.replaceAll("_", " ").toLowerCase()).terms;
	}
}
