package wikicat.extract;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import wikicat.extract.util.IO;
import wikicat.extract.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author jfoley
 */
public class CollectCategoryFrequencies {
  public static void main(String[] args) throws IOException {
    TObjectIntHashMap<String> categoryFrequencies = new TObjectIntHashMap<>();
    int total = 0;
    try (
        BufferedReader reader = IO.fileReader("data/graph.tsv.gz")
    ) {
      while(true) {
        if(++total % 10000 == 0) {
          System.err.println(total);
        }
        String line = reader.readLine();
        if(line == null) break;
        String[] cols = line.split("\t");
        if(cols.length < 2) continue;
        String page = cols[0];
        String categoryMarkup = cols[1];
        String categoryLabel = categoryMarkup;
        String anchorText = categoryMarkup.replaceAll("_", " ");
        if(categoryMarkup.contains("|")) {
          anchorText = StrUtil.takeAfter(categoryMarkup, "|");
          categoryLabel = StrUtil.takeBefore(categoryMarkup, "|");
        }

        categoryFrequencies.adjustOrPutValue(categoryLabel, 1, 1);
      }
    }

    try (final PrintWriter out = IO.printWriter("category_frequencies.tsv")) {
      categoryFrequencies.forEachEntry(new TObjectIntProcedure<String>() {
        @Override
        public boolean execute(String s, int i) {
          out.printf("%s\t%d\n", s, i);
          return true;
        }
      });
    }
  }
}
