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
 
public class Sort {
  private ArrayList<Time> times;
  private ArrayList<Time> timeHelper;
  private ArrayList<Bookmark> bookmarks;
  private ArrayList<Bookmark> properSequence;
  private int number;
  
  public Sort() {}
  
  public ArrayList<Bookmark> sort(ArrayList<Time> values, ArrayList<Bookmark> marks){
    times = values;
    bookmarks = marks;
    number = values.size();
    timeHelper = new ArrayList<Time>(times);
    int j = 0;
    properSequence = new ArrayList<Bookmark>(number);
    
    Collections.sort(times);
    orderBookmarks();
    Collections.reverse(properSequence);
    return properSequence;
  }
  
  private Time getValue(int j){
    return times.get(j);
  }
  
  private void orderBookmarks(){
    for(int i = 0; i< times.size(); i++){
      orderBookmarksHelper(times.get(i));
    }
  }
  
  private void orderBookmarksHelper(Time t){
    int j = timeHelper.indexOf(t);
    properSequence.add(bookmarks.get(j));
  }
   
}
 