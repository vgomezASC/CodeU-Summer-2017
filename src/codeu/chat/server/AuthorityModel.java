public interface AuthorityModel
{
  public void changeAuthority(ConversationHeader conversation, Uuid targetUser, byte authorityByte);

  public boolean isBannedUser(ConversationHeader conversation,Uuid targetUser);
  public boolean isBannedUser(Uuid conversation,Uuid targetUser);
  public boolean isUser(ConversationHeader conversation,Uuid targetUser);
  public boolean isUser(Uuid conversation,Uuid targetUser);
  public boolean isOwner(ConversationHeader conversation,Uuid targetUser);
  public boolean isOwner(Uuid conversation,Uuid targetUser);
  public boolean isCreator(ConversationHeader conversation,Uuid targetUser);
  public boolean isCreator(Uuid conversation,Uuid targetUser);
}