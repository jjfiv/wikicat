package wikicat.extract.catgraph;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.IO;
import wikicat.extract.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author jfoley
 */
public class LoadCategoryGraph {
  public static String cleanCategory(String input) {
    return StrUtil.takeBefore(StrUtil.removeFront(input, "Category:"), "|").trim().replace(' ', '_');
  }

  public static Map<String, CategoryNode> load(String graphTSV) throws IOException {
    final HashMap<String, CategoryNode> nodes = new HashMap<>(10000000);

    try (BufferedReader reader = IO.fileReader(graphTSV)) {

      int edge = 0;

      while(true) {
        String line = reader.readLine();
        if(line == null) break;
        if(!line.contains("Category:")) continue;
        String[] col = line.split("\t");
        if(col.length != 2) continue;
        String child = col[0];
        String parent = col[1];
        if(++edge % 100000 == 0) {
          System.err.println(edge);
          //if(edge > 1000000) break;
        }

        // Link describes category to category link:
        if(child.startsWith("Category:") && parent.startsWith("Category:")) {
          CategoryNode pn = nodes.get(parent);
          if (pn == null) {
            pn = new CategoryNode(nodes, parent);
            nodes.put(parent, pn);
          }
          CategoryNode cn = nodes.get(child);
          if (cn == null) {
            cn = new CategoryNode(nodes, child);
            nodes.put(child, cn);
          }

          pn.children.add(cn.name);
          cn.parents.add(pn.name);
        } else {
          // One is a category, other is a page:
          String cat;
          String page;
          if(child.startsWith("Category:")) {
            cat = child;
            page = parent;
          } else if (parent.startsWith("Category:")) {
            cat = parent;
            page = child;
          } else {
            continue;
          }

          String catName = cleanCategory(cat);
          CategoryNode node = nodes.get(catName);
          if(node == null) {
            node = new CategoryNode(nodes, catName);
            nodes.put(catName, node);
          }
          node.relevantPages.add(cleanCategory(page));
        }
      }
    }

    return nodes;
  }

  public static void main(String[] args) throws IOException {
    Parameters argp = Parameters.parseArgs(args);
    String input = argp.get("input", "data/graph.tsv.gz");

    final Map<String, CategoryNode> nodes = load(input);

    // Now find all roots directly:
    HashSet<String> roots = new HashSet<>();
    for (CategoryNode node : nodes.values()) {
      if (node.parents.isEmpty()) {
        roots.add(node.name);
      }
    }

    TIntIntHashMap childCountFreq = new TIntIntHashMap();
    for (CategoryNode node : nodes.values()) {
      childCountFreq.adjustOrPutValue(node.children.size(), 1, 1);
      if (node.children.size() > 1000) {
        System.err.println(node.name);
      }
      //roots.add(node.root().name);
    }

    System.out.printf("Number of nodes: %d\n", nodes.size());
    System.out.printf("Number of Roots: %d\n", roots.size());

    TIntIntHashMap pageCountFreqs = new TIntIntHashMap();
    for (CategoryNode node : nodes.values()) {
      pageCountFreqs.adjustOrPutValue(node.relevantPages.size(), 1, 1);
    }

    pageCountFreqs.forEachEntry(new TIntIntProcedure() {
      @Override
      public boolean execute(int numChildren, int freq) {
        System.out.printf("%d %d\n", numChildren, freq);
        return true;
      }
    });

  }
}
