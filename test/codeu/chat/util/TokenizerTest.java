/**
 * Tests Tokenizer in both the quote and no-quote modes.
 * 
 * @author (Sarah Abowitz) 
 * @version (V1.0.0 | 5.20.17)
 */

package codeu.chat.util;

import static org.junit.Assert.*; 

import java.io.IOException;
import org.junit.Test; 
   
public final class TokenizerTest {
  
  /** A tokenization test with quotes. */
  @Test
  public void testWithQuotes() throws IOException {
    final Tokenizer tokenizer = new Tokenizer("\"hello world\" \"how are you\"");
    assertEquals(tokenizer.next(), "hello world");
    assertEquals(tokenizer.next(), "how are you");
    assertEquals(tokenizer.next(), null);
  }
  
  /** A tokenization test with no quotes. */	
  @Test
  public void testWithNoQuotes() throws IOException {
    final Tokenizer tokenizer = new Tokenizer("hello world how are you");
    assertEquals(tokenizer.next(), "hello");
    assertEquals(tokenizer.next(), "world");
    assertEquals(tokenizer.next(), "how");
    assertEquals(tokenizer.next(), "are");
    assertEquals(tokenizer.next(), "you");
    assertEquals(tokenizer.next(), null);
  }
  
}
