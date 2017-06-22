/**
 * Retains the last message a user saw of a certain conversation, 
 * marking their place like a bookmark.
 *
 * This class is unfinished and a tad messy, so if you have questions/feedback, don't be
 * shy! Also right now this doesn't really have any methods but we'll add them as we go 
 * along.
 * 
 * @author (Sarah Abowitz) 
 * @version (V1.0.0 | 6.10.17)
 */
  
package codeu.chat.common;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
 import codeu.chat.common.Message;
 import codeu.chat.common.ConversationHeader;
 import codeu.chat.client.core.ConversationContext;
 import codeu.chat.util.Serializer;
 import codeu.chat.util.Serializers; 
  
 public final class Bookmark{
    public static final Serializer<Bookmark> SERIALIZER = new Serializer<Bookmark>() {

      @Override
      public void write(OutputStream out, Bookmark value) throws IOException {

        Serializers.nullable(ConversationHeader.SERIALIZER).write(out, value.conversation);
        Serializers.nullable(Message.SERIALIZER).write(out, value.bookmark);
      }

      @Override
      public Bookmark read(InputStream in) throws IOException {

        Bookmark result = new Bookmark();
        result.conversation = Serializers.nullable(ConversationHeader.SERIALIZER).read(in);  
        result.bookmark = Serializers.nullable(Message.SERIALIZER).read(in);
		return result;
      }
   };
   
    public Message first; // just keeps the first message of this conversation
 	public Message bookmark; // keeps the last message status-update has seen
 	public ConversationHeader conversation;
 	
 	public Bookmark(){
 	  bookmark = null;
 	  conversation = null;
 	}
 	
 	public Bookmark(ConversationContext c){
 		conversation = c.conversation;
 		first = c.firstMessage().message;
 		bookmark = c.lastMessage().message;
 	}
 	
 	public Bookmark(ConversationContext c, Message m){
 	    conversation = c.conversation;
 		first = c.firstMessage().message;
 		bookmark = m; 
 	}
 	 	
 }