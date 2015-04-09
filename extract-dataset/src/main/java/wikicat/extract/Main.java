package wikicat.extract;

import org.lemurproject.galago.utility.tools.AppFunction;

/**
 * @author jfoley
 */
public class Main {
  public static void main(String[] args) throws Exception {
    AppFunction fn = new WikipediaToHTML();
    fn.run(args, System.out);
  }
}
