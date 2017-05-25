/**
 * Tokenizes the user input so the app can parse it. 
 * 
 * @author (Sarah Abowitz) 
 * @version (V1.0.0 | 5.20.17)
 */

package codeu.chat.util;

import java.io.IOException;
import java.lang.StringBuilder;

public final class Tokenizer {
  private StringBuilder token; // builds current token
  private String source; // where all tokens come from
  private int at; // char position in source, so to speak

  /**
   * No-args constructor for objects of class Tokenizer
   */
  public Tokenizer(){
    token = new StringBuilder();
    at = 0;
  }

  /**
   * Constructor for objects of class Tokenizer that initializes source as a given string
   * @param source The string that source is initialized as. 
   */
  public Tokenizer(String source) { 
    this.source = source;
    token = new StringBuilder(source);
    at = 0;
  }
  
  /**
   * Forms the next string token from source.
   * @throws IOException if there's no string content there.
   * @return The resulting token.
   */  
  public String next() throws IOException {    
    String result = "";
    while (remaining() > 0 && Character.isWhitespace(peek())) {
      read();  // ignore the result because we already know that it is a whitespace character
    }
    if (remaining() <= 0) {
      return null;
    } else if (peek() == '"') {
      result = readWithQuotes();
    } else {
      result = readWithNoQuotes();
    }
      return result;
  }
  
  /**
   * Accesses current character before advancing at.
   * @throws IOException if at >= source.length()
   * @return The current character.
   */    
  private char read() throws IOException {
    final char c = peek();
    at += 1;
    return c;
  }
  
  /**
   * Constructs a string token from the string up until the first instance of whitespace
   * or string's end.
   *
   * @throws IOException if there's no string content there.
   * @return The next string token.
   */    
  private String readWithNoQuotes() throws IOException {
    token.setLength(0);  // clear the token
    while (remaining() > 0 && !Character.isWhitespace(peek())) {
      token.append(read());
    }
      return token.toString();
  }
  
  /**
   * Constructs a string token from all characters between the first two quotes in a string.
   * @throws IOException if the string read has no opening quote.
   * @return The next string token in the quotes.
   */    
  private String readWithQuotes() throws IOException {
    token.setLength(0);  // clear the token
    if (read() != '"') {
      throw new IOException("Strings must start with opening quote");
    }
    while (peek() != '"') {
      token.append(read());      
    }
    read(); // read the closing the quote that allowed us to exit the loop
    return token.toString();
  }
  
  /**
   * Accesses the character at at's position in source.
   * @throws IOException if at >= source.length()
   * @return The resulting character.
   */    
  private char peek() throws IOException {
    if (at < source.length()) {
      return source.charAt(at);
    } else {
      throw new IOException(); 
    }
      
  }
  
  /**
   * Calculates length of the substring of source starting at index at.
   * @return The result of this calculation.
   */    
  private int remaining() {return source.length() - at; }
    
}
