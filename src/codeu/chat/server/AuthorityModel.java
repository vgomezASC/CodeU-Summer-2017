/* Original interface by Yuhang Liao, 
 * annotation and imports by Sarah Abowitz. 
 * 
 * Yuhang, you originally came up with the idea for this class so
 * please replace this comment with what this interface does so devs
 * know. ~ S
 */

package codeu.chat.server;

import codeu.chat.common.ConversationHeader;
import codeu.chat.util.Uuid;

public interface AuthorityModel
{
  public void changeAuthority(Uuid conversation, Uuid targetUser, byte authorityByte);

  public boolean isMember(ConversationHeader conversation,Uuid targetUser);
  public boolean isMember(Uuid conversation,Uuid targetUser);
  public boolean isOwner(ConversationHeader conversation,Uuid targetUser);
  public boolean isOwner(Uuid conversation,Uuid targetUser);
  public boolean isCreator(ConversationHeader conversation,Uuid targetUser);
  public boolean isCreator(Uuid conversation,Uuid targetUser);
}
