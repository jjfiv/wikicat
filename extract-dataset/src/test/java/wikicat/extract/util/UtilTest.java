package wikicat.extract.util;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class UtilTest {

  @Test
  public void testPopLast() throws Exception {
    ArrayList<Integer> data = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

    assertEquals(5, (int) Util.popLast(data));
    assertEquals(4, (int) Util.popLast(data));
    assertEquals(3, (int) Util.popLast(data));
    assertEquals(2, (int) Util.popLast(data));
    assertEquals(1, (int) Util.popLast(data));
    assertNull(Util.popLast(data));
    assertNull(Util.popLast(data));
  }

  @Test
  public void testIntersection() {
    Set<Integer> isect = Util.intersection(Arrays.asList(1, 2, 3), Arrays.asList(3, 4, 5));

    assertEquals(1, isect.size());
    assertEquals(3, (int) Util.first(isect));
  }

  @Test
  public void testUnion() {
    Set<Integer> result = Util.union(Arrays.asList(1, 2, 3), Arrays.asList(3, 4, 5));

    assertEquals(5, result.size());
    assertTrue(result.contains(1));
    assertTrue(result.contains(2));
    assertTrue(result.contains(3));
    assertTrue(result.contains(4));
    assertTrue(result.contains(5));
  }

  @Test
  public void testBatched() {
    List<Integer> raw = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    List<List<Integer>> batches = Util.batched(raw, 2);
    assertEquals(4,batches.size());
    assertEquals(2,batches.get(0).size());
    assertEquals(2,batches.get(1).size());
    assertEquals(2,batches.get(2).size());
    assertEquals(1,batches.get(3).size());
  }

  @Test
  public void testSliding() {
    List<String> terms = Arrays.asList("Every", "Good", "Boy", "Deserves", "Fudge");
    List<List<String>> bigrams = Util.sliding(terms, 2);

    assertEquals(4, bigrams.size());
    assertEquals(Arrays.asList("Every", "Good"), bigrams.get(0));
    assertEquals(Arrays.asList("Good", "Boy"), bigrams.get(1));
    assertEquals(Arrays.asList("Boy", "Deserves"), bigrams.get(2));
    assertEquals(Arrays.asList("Deserves", "Fudge"), bigrams.get(3));

    List<String> single = Collections.singletonList("Every");
    List<List<String>> none = Util.sliding(single, 2);
    assertEquals(0, none.size());

    List<String> two = Arrays.asList("A", "B");
    List<List<String>> one = Util.sliding(two, 2);
    assertEquals(1, one.size());
  }

}