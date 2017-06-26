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
 
public class Mergesort {
  private ArrayList<Time> times;
  private ArrayList<Time> timeHelper;
  private ArrayList<Bookmark> bookmarks;
  private ArrayList<Bookmark> bookmarkHelper;
  private int number;
  
  public Mergesort() {}
  
  public ArrayList<Bookmark> sort(ArrayList<Time> values, ArrayList<Bookmark> marks){
    times = values;
    bookmarks = marks;
    number = values.size();
    timeHelper = new ArrayList<Time>(number);
    bookmarkHelper = new ArrayList<Bookmark>(number);
    
    mergesort(0, number-1);
    reverse();
    return bookmarkHelper;
  }
  
  private void mergesort(int low, int high){
    if (low < high){
      int middle = low + (high-low)/2;
      mergesort(low, middle);
      mergesort(middle+1, high);
      merge(low, middle, high);
    }
  }
  
  private void merge(int low, int middle, int high){
    for(int i = low; i <= high; i++){
      timeHelper.set(i, times.get(i));
      bookmarkHelper.set(i, bookmarks.get(i));
    }
    
    int i = low;
    int j = middle + 1;
    int k = low;
    
    while(i <= middle && j <= high){
      if(timeHelper.get(i).compareTo(timeHelper.get(j)) < 0){
        times.set(k, timeHelper.get(i));
        bookmarks.set(k, bookmarkHelper.get(i));
        i++;
      } else {
        times.set(k, timeHelper.get(j));
        bookmarks.set(k, bookmarkHelper.get(j));
        j++;
      }
      k++;
    }
    
    while(i <= middle){
      times.set(k, timeHelper.get(i));
      bookmarks.set(k, bookmarkHelper.get(i));
      k++;
      i++;
    }
  }
  
  private void reverse() {
    bookmarkHelper.clear();
    
    for(int i = number-1; i>=0; i--){
     bookmarkHelper.add(bookmarks.get(i));
    }
  }
}
 