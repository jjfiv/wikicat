package wikicat.extract;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import wikicat.extract.util.IO;
import wikicat.extract.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jfoley
 */
public class CollectCategoryFrequencies {
  public static void main(String[] args) throws IOException {
    List<TObjectIntHashMap<String>> categoryFrequenciesBySplit = new ArrayList<>();
    List<Set<String>> pagesBySplit = new ArrayList<>();

    for (int i = 0; i < Constants.NumSplits; i++) {
      categoryFrequenciesBySplit.add(new TObjectIntHashMap<String>());
      pagesBySplit.add(new HashSet<String>());
    }

    Set<String> allCategories = new HashSet<>();

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

        // Just hierarchy information
        if(page.startsWith("Category:")) {
          continue;
        }
        int split = Math.abs(page.hashCode()) % Constants.NumSplits;
        allCategories.add(categoryLabel);

        categoryFrequenciesBySplit.get(split).adjustOrPutValue(categoryLabel, 1, 1);
        pagesBySplit.get(split).add(page);
      }
    }

    for (int split = 0; split < Constants.NumSplits; split++) {
      try (final PrintWriter out = IO.printWriter(String.format("split_names_%d.txt", split))) {
        for (String s : pagesBySplit.get(split)) {
          out.println(s);
        }
      }
    }
    pagesBySplit = null;

    try (final PrintWriter out = IO.printWriter("category_frequencies.tsv")) {
      for (String cat : allCategories) {
        boolean nonzero = true;
        List<Integer> x = new ArrayList<>(Constants.NumSplits);
        for (int split = 0; split < categoryFrequenciesBySplit.size(); split++) {
          int count = categoryFrequenciesBySplit.get(split).get(cat);
          if (count == 0) {
            nonzero = false;
            break;
          }
          x.add(count);
        }
        if (nonzero) {
          // print only categories that exist in every split
          out.printf("%s", cat);
          for (int split = 0; split < categoryFrequenciesBySplit.size(); split++) {
            out.printf("\t%d", categoryFrequenciesBySplit.get(split).get(cat));
          }
          out.println();
        } else {
          // remove categories that don't exist in every split
          for (int split = 0; split < categoryFrequenciesBySplit.size(); split++) {
            categoryFrequenciesBySplit.get(split).remove(cat);
          }
        }
      }
    }

    for (int split = 0; split < categoryFrequenciesBySplit.size(); split++) {
      TObjectIntHashMap<String> categoryFrequencies = categoryFrequenciesBySplit.get(split);
      try (final PrintWriter out = IO.printWriter(String.format("category_frequencies_%d.tsv", split))) {
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
}
