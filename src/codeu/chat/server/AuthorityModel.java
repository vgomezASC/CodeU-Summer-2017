package codeu.chat.server;

import codeu.chat.common.ConversationHeader;
import codeu.chat.util.Uuid;

public interface AuthorityModel
{
  public void changeAuthority(Uuid conversation, Uuid targetUser, byte authorityByte);
  public boolean isMember(Uuid conversation,Uuid targetUser);
  public boolean isOwner(Uuid conversation,Uuid targetUser);
  public boolean isCreator(Uuid conversation,Uuid targetUser);
}