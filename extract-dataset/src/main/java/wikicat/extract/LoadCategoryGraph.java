package wikicat.extract;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.IO;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jfoley
 */
public class LoadCategoryGraph {
  public static class CategoryNode {
    public String name;
    public Set<CategoryNode> children;
    public Set<CategoryNode> parents;
    public Set<String> relevantPages;
    private int id;

    /** Constructor */
    public CategoryNode(String name) {
      this.id = -1;
      this.name = name;
      this.parents = new HashSet<>();
      this.children = new HashSet<>();
      this.relevantPages = new HashSet<>();
    }

    /** Get descendants, loosely; i.e. crawl using only the child relation */
    public Set<CategoryNode> descendants(int maximum) {
      Set<CategoryNode> visited = new HashSet<>();
      Set<CategoryNode> frontier = new HashSet<>();
      frontier.addAll(children);
      for (CategoryNode node : frontier) {
        // consider outgoing links of this frontier element:
        for (CategoryNode child : node.children) {
          if(visited.contains(child)) continue;
          frontier.add(child);
        }
        // mark this node as done.
        visited.add(node);
        if(visited.size() + frontier.size() > maximum) {
          visited.addAll(frontier);
          return visited;
        }
      }

      return visited;
    }

    /** Get ancestors, loosely; i.e. crawl using only the parent relation */
    public Set<CategoryNode> ancestors(int maximum) {
      Set<CategoryNode> visited = new HashSet<>();
      Set<CategoryNode> frontier = new HashSet<>();
      frontier.addAll(parents);
      for (CategoryNode node : frontier) {
        // consider outgoing links of this frontier element:
        for (CategoryNode parent : node.parents) {
          if(visited.contains(parent)) continue;
          frontier.add(parent);
        }
        // mark this node as done.
        visited.add(node);
        if(visited.size() + frontier.size() > maximum) {
          visited.addAll(frontier);
          return visited;
        }
      }

      return visited;
    }

    /** Get neighbors, loosely; i.e. crawl */
    public Set<CategoryNode> neighbors(int maximum) {
      Set<CategoryNode> visited = new HashSet<>();
      Set<CategoryNode> frontier = new HashSet<>();
      frontier.addAll(parents);
      frontier.addAll(children);
      for (CategoryNode node : frontier) {
        // consider outgoing links of this frontier element:
        for (CategoryNode neighbor : node.parents) {
          if(visited.contains(neighbor)) continue;
          frontier.add(neighbor);
        }
        for (CategoryNode neighbor : node.children) {
          if(visited.contains(neighbor)) continue;
          frontier.add(neighbor);
        }
        // mark this node as done.
        visited.add(node);
        if(visited.size() + frontier.size() > maximum) {
          visited.addAll(frontier);
          return visited;
        }
      }

      return visited;
    }


    public int hashCode() {
      return name.hashCode();
    }

    public boolean equals(Object o) {
      return this == o || ((o instanceof CategoryNode) && this.name.equals(((CategoryNode) o).name));
    }

    public void setId(int id) {
      this.id = id;
    }
  }

  public static void main(String[] args) throws IOException {
    Parameters argp = Parameters.parseArgs(args);
    String input = argp.get("input", "data/catgraph.tsv.gz");

    final HashMap<String, CategoryNode> nodes = new HashMap<>();

    IO.forEachLine(IO.fileReader(input), new IO.StringFunctor() {
      @Override
      public void process(String input) {
        String[] col = input.split("\t");
        if (col.length != 2) return;
        String child = ExtractCategoryGraph.cleanCategory(col[0]);
        String parent = ExtractCategoryGraph.cleanCategory(col[1]);

        CategoryNode pn = nodes.get(parent);
        if (pn == null) {
          pn = new CategoryNode(parent);
          nodes.put(parent, pn);
        }
        CategoryNode cn = nodes.get(child);
        if (cn == null) {
          cn = new CategoryNode(child);
          nodes.put(child, cn);
        }

        pn.children.add(cn);
        cn.parents.add(pn);
      }
    });


    // Find and attach "page" data.
    IO.forEachLine(IO.fileReader(argp.get("fullGraphInput", "data/graph.tsv.gz")), new IO.StringFunctor() {
      @Override
      public void process(String input) {
        String[] col = input.split("\t");
        if(col.length != 2) return;
        String child = col[0];
        String parent = col[1];

        String cat;
        String page;
        if(child.startsWith("Category:")) {
          cat = child;
          page = parent;
        } else if (parent.startsWith("Category:")) {
          cat = parent;
          page = child;
        } else {
          return;
        }

        String catName = ExtractCategoryGraph.cleanCategory(cat);
        CategoryNode node = nodes.get(catName);
        if(node == null) {
          node = new CategoryNode(catName);
          nodes.put(catName, node);
        }
        node.relevantPages.add(ExtractCategoryGraph.cleanCategory(page));
      }
    });

    // Now find all roots directly:
    int identifier = 0;
    HashSet<String> roots = new HashSet<>();
    for (CategoryNode node : nodes.values()) {
      node.setId(identifier++);
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

    /*
    childCountFreq.forEachEntry(new TIntIntProcedure() {
      @Override
      public boolean execute(int numChildren, int freq) {
        System.out.printf("%d %d\n", numChildren, freq);
        return true;
      }
    });
    */

  }
}
