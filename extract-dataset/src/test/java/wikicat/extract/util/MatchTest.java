package wikicat.extract.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class MatchTest {

  @Test
  public void testFind() throws Exception {
    String test = "foo the bar";
    assertEquals(
        Arrays.asList("foo", "bar"),
        Match.find(test, Pattern.compile("(\\w+) the (\\w+)")));
  }
}