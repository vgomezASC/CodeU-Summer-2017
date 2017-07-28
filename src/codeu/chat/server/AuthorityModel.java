/* Original interface by Yuhang Liao, 
 * annotation and imports by Sarah Abowitz.
 * 
 * Hey idk if the new v of this is worth it
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
