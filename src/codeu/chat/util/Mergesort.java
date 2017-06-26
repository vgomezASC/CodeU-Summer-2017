/**
 * A static class that mergesorts an ArrayList of Bookmarks. 
 * 
 * @author (Sarah Abowitz) 
 * @version (V1.0.6 | 6.25.17)
 */
 
package codeu.chat.util;
 
import codeu.chat.common.Bookmark;
import codeu.chat.util.Time;
 
import java.util.ArrayList;
import java.util.Collections;
 
public class Mergesort {
  private ArrayList<Time> times;
  private ArrayList<Time> timeHelper;
  private ArrayList<Bookmark> bookmarks;
  private ArrayList<Bookmark> properSequence;
  private int number;
  
  public Mergesort() {}
  
  public ArrayList<Bookmark> sort(ArrayList<Time> values, ArrayList<Bookmark> marks){
    times = values;
    bookmarks = marks;
    number = values.size();
    timeHelper = new ArrayList<Time>(number);
    for(int i = 0; i <= number; i++){
      timeHelper.set(i,values.get(i));
    }
    properSequence = new ArrayList<Bookmark>(number);
    
    Collections.sort(times);
    orderBookmarks();
    //reverse();
    return properSequence;
  }
  
  private void orderBookmarks(){
    for(int i = 0; i<= number; i++){
      int j = timeHelper.indexOf(times.get(i));
      properSequence.add(bookmarks.get(j));
    }
  }
    
  private void reverse() {
    properSequence.clear();
    
    for(int i = number-1; i>=0; i--){
     properSequence.add(bookmarks.get(i));
    }
  }
}
 