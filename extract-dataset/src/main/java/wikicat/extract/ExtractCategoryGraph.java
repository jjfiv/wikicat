package wikicat.extract;

import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.IO;
import wikicat.extract.util.StrUtil;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author jfoley
 */
public class ExtractCategoryGraph {
  public static String cleanCategory(String input) {
    return StrUtil.takeBefore(StrUtil.removeFront(input, "Category:"), "|").trim().replace(' ', '_');
  }

  public static void main(String[] args) throws IOException {
    Parameters argp = Parameters.parseArgs(args);
    String input = argp.get("input", "data/graph.tsv.gz");

    try (PrintWriter output = IO.printWriter(argp.get("output", "data/catgraph.tsv.gz"))) {
      IO.forEachLine(IO.fileReader(input), new IO.StringFunctor() {
        @Override
        public void process(String input) {
          String[] col = input.split("\t");
          if(col.length != 2) return;
          String child = col[0];
          String parent = col[1];


          if(child.startsWith("Category:") && parent.startsWith("Category:")) {
            //System.err.printf("%s -> %s\n", parent, child);
            output.print(cleanCategory(child));
            output.print('\t');
            output.println(cleanCategory(parent));
          }
        }
      });
    }
  }
}
