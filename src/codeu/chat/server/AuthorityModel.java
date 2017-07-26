package codeu.chat.server;

import codeu.chat.common.ConversationHeader;
import codeu.chat.util.Uuid;

public interface AuthorityModel
{
  public void changeAuthority(ConversationHeader conversation, Uuid targetUser, byte authorityByte);
  public boolean isMember(ConversationHeader conversation,Uuid targetUser);
  public boolean isMember(Uuid conversation,Uuid targetUser);
  public boolean isOwner(ConversationHeader conversation,Uuid targetUser);
  public boolean isOwner(Uuid conversation,Uuid targetUser);
  public boolean isCreator(ConversationHeader conversation,Uuid targetUser);
  public boolean isCreator(Uuid conversation,Uuid targetUser);
}