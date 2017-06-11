/**
 * A collection of all conversations followed by a user, and the users they follow. 
 *
 * This class is unfinished and a tad messy as a result, so if you have questions/feedback
 * that would definitely accelerate things.
 * 
 * @author (Sarah Abowitz) 
 * @version (V1.0.0 | 6.10.17)
 */
 
 package codeu.chat.client.common;
 
 import java.util.HashSet;
 
 import codeu.chat.client.core.ConversationContext;
 import codeu.chat.client.core.MessageContext;
 import codeu.chat.common.Bookmark;
 import codeu.chat.common.User;
 
 public final class InterestSet{
   public HashSet<User> users; 
   public HashSet<Bookmark> bookmarks; 
   
   // custom sizing variables for users and bookmarks
   private int u;
   private int b;
   
   // no-args constructor for the standard size of these sets	
   public InterestSet(){
   	 u = 100;
   	 b = 100;
 	 users = new HashSet<User>(100);
 	 bookmarks = new HashSet<Bookmark>(100); 	
   }
   
   // this constructor allows custom sizings for the sets	
   public InterestSet(int u, int b){
     users = new HashSet<User>(u);
 	 bookmarks = new HashSet<Bookmark>(b);
   }
   
   /** Adds a Bookmark for a new conversation to bookmarks.
    * @param c the ConversationContext we've just started tracking*/
   public void addBookmark(ConversationContext c){
     bookmarks.add(new Bookmark(c));
   }
   
   // Deletes a user from users, and all the conversations they follow in bookmarks
   // i feel like there's some O(faster) way to implement this but how
   public void delCoolUser(User u){
   	 HashSet<Bookmark> trashCan = new HashSet<Bookmark>(b);
     for (Bookmark b : bookmarks){
       boolean found = false;
       for (MessageContext message = b.conversation.firstMessage();
     	                   message != null; 
     	                   message = message.next()){
         if (message.message.author == u.id && !found){
     	   trashCan.add(b);
     	   found = true;
     	 }
       } 
     }
     
     for (Bookmark b: trashCan){
       bookmarks.remove(b);
     }
     
   }
 }