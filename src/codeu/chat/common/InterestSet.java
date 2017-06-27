/**
 * A collection of all conversations followed by a user, and the users they follow. 
 *
 * This class is unfinished and a tad messy as a result, so if you have questions/feedback
 * that would definitely accelerate things.
 * 
 * @author (Sarah Abowitz) 
 * @version (V1.0.0 | 6.10.17)
 */
 
  package codeu.chat.common;
 
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.OutputStream;
  import java.util.HashSet;
 
  import codeu.chat.client.core.ConversationContext;
  import codeu.chat.client.core.MessageContext;
  import codeu.chat.common.User;
  import codeu.chat.server.Model;
  import codeu.chat.util.Serializer;
  import codeu.chat.util.Serializers;
 
  public final class InterestSet{
    public static final Serializer<InterestSet> SERIALIZER = new Serializer<InterestSet>() {

      @Override
      public void write(OutputStream out, InterestSet value) throws IOException {

        Serializers.collection(User.SERIALIZER).write(out, value.users);
        Serializers.collection(Bookmark.SERIALIZER).write(out, value.bookmarks);
        
      }

      @Override
      public InterestSet read(InputStream in) throws IOException {

        InterestSet result = new InterestSet();
        
        result.users.addAll(Serializers.collection(User.SERIALIZER).read(in));
        result.bookmarks.addAll(Serializers.collection(Bookmark.SERIALIZER).read(in));
        return result;
      }
   }; 
  
   public HashSet<User> users; 
   public HashSet<Bookmark> bookmarks; 
   
   public InterestSet(){
   	 users = new HashSet<User>();
 	 bookmarks = new HashSet<Bookmark>(); 	
   }
   
   /** Adds a Bookmark for a new conversation to bookmarks.
    * @param c the ConversationContext we've just started tracking */
   public void addBookmark(ConversationContext c){
     bookmarks.add(new Bookmark(c));
   }
  
  @Override 
  public String toString() {
    String result = "Users\n";
    for(User u : users){
      result += " "+u.name;
    }
    result += "\nBookmarks\n";
    for(Bookmark b : bookmarks){
      result += "\n"+b.conversation.title+": ";
      if (b.bookmark != null){
        result += " "+b.bookmark.content+"\n";
      } else {
        result += "<NULL MARKER>";
      }
    }
    return result;
  }
 }