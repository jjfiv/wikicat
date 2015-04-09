package wikicat.extract.util;

/**
 * A -> B
 * @author jfoley.
 */
public interface TransformFn<A,B> {
	public B transform(A input);
}
