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
 
 import codeu.chat.client.core.MessageContext;
 import codeu.chat.client.core.ConversationContext; 
  
 public final class Bookmark{
 	public MessageContext bookmark;
 	public ConversationContext conversation;
 	
 	public Bookmark(ConversationContext c){
 		conversation = c;
 		bookmark = c.firstMessage();
 	}
 	 	
 }