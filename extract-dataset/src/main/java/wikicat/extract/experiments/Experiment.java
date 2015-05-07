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


	public static List<String> tokenizePageTitle(Tokenizer tok, String input) {
		return tok.tokenize(input.replaceAll("_", " ").toLowerCase()).terms;
	}
	public List<String> tokenizePageTitle(String input) {
		return tokenizePageTitle(tokenizer, input);
	}
}
