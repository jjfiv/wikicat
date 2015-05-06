package wikicat.extract.catgraph;

import java.util.*;

/**
 * @author jfoley.
 */
public class CategoryNode {
	private final HashMap<String, CategoryNode> nodes;
	public String name;
	public Set<String> children;
	public Set<String> parents;
	public Set<String> relevantPages;

	/**
   * Constructor
   */
	public CategoryNode(HashMap<String, CategoryNode> nodes, String name) {
		this.nodes = nodes;
		this.name = name;
		this.parents = new HashSet<>();
		this.children = new HashSet<>();
		this.relevantPages = new HashSet<>();
	}

	public Iterable<String> crawl(CrawlType what, int count) {
		CategoryCrawler crawler = new CategoryCrawler(nodes, this, count, what);
		while(!crawler.isDone()) {
			crawler.processSingleStep();
		}
		return crawler.results();
	}


	public enum CrawlType {
		PARENTS,
		CHILDREN,
		NEIGHBORS,
	}
	public static class CategoryCrawler {
		private final Map<String, CategoryNode> nodes;
		private final CrawlType type;
		private final int limit;
		Set<String> visited;
		Set<String> frontier;

		public CategoryCrawler(Map<String, CategoryNode> nodes, CategoryNode start, int limit, CrawlType type) {
			this.nodes = nodes;
			this.limit = limit;
			this.type = type;

			visited = new HashSet<>();
			frontier = new HashSet<>();
			frontier.add(start.name);
		}

		public int foundCount() {
			return frontier.size() + visited.size();
		}

		public boolean isDone() {
			return frontier.isEmpty() || foundCount() >= limit;
		}

		public void processSingleStep() {
			if(frontier.isEmpty()) return;

			String next = frontier.iterator().next();
			frontier.remove(next);
			visited.add(next);

			CategoryNode current = nodes.get(next);
			for (String neighbor : current.getNeighbors(type)) {
				addToFrontier(neighbor);
			}
		}

		public List<String> results() {
			List<String> total = new ArrayList<>();
			total.addAll(visited);
			total.addAll(frontier);
			return new ArrayList<>(total.subList(0, Math.min(limit, total.size())));
		}

		public void addToFrontier(String key) {
			if(visited.contains(key)) {
				return;
			}
			frontier.add(key);
		}

	}

	private Collection<String> getNeighbors(CrawlType type) {
		ArrayList<String> output = new ArrayList<>();
		switch (type) {

			case PARENTS:
				output.addAll(parents);
				break;
			case CHILDREN:
				output.addAll(children);
				break;
			case NEIGHBORS:
				output.addAll(parents);
				output.addAll(children);
				break;
		}
		return output;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object o) {
		return this == o || ((o instanceof CategoryNode) && this.name.equals(((CategoryNode) o).name));
	}
}
