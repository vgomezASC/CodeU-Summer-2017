// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 *  Note: The following names of methods in this tester are tentative. They are just  
 *  placeholders (if you know a better name for a method, tell me!) and will mirror the 
 *  corresponding methods as we implement them, so let me know when you push a new commit
 *  that changes the names of what I have listed here. ~ Sarah Abowitz
 *
 *  Replaceable parts: None, they should all be replaced by now...
 *          
 */


package codeu.chat.server;
import java.util.HashMap;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.common.BasicController;
import codeu.chat.common.SinglesView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class ConversationAccessServerTest {

  private Model model;
  private BasicController controller;
  private SinglesView rawView;
  private byte creatorByte = 0b111; // Before testing, this must be the equivalent of 111.
  private byte ownerByte = 0b011; // Before testing, this must be the equivalent of 011.
  private byte memberByte = 0b001; // Before testing, this must be the equivalent of 001.
  private byte trollByte = 0b000; // Before testing, this must be the equivalent of 000.
  
  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model);
    rawView = new View(model);			
  }

  @Test
  public void testAddConversation() {

    final User user = controller.newUser("user");

    assertNotNull(
        "Check that user has a valid reference",
        user);

    final ConversationHeader conversation = controller.newConversation(
        "conversation",
        user.id);

    assertNotNull(
        "Check that conversation has a valid reference",
        conversation);    
        
    HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
    HashMap<Uuid, Byte> rightMap = new HashMap<Uuid, Byte>();
    rightMap.put(user.id, creatorByte);

    assertNotNull(
        "Check that conversation has an access map",
        accessMap);
    
    assertEquals(
    	"Check that the accessMap is correctly stored",
    	accessMap,rightMap);
  }
  
  @Test
  public void testGetAndUpdateAccess(){
    final User creator = controller.newUser("creator");
    final User user = controller.newUser("user");
     
    ConversationHeader conversation = controller.newConversation("chat", creator.id);
    HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
    HashMap<Uuid, Byte> rightMap = new HashMap<Uuid, Byte>();
    
    byte memberByte = 0b001; // Before testing, this must be the equivalent of 001.
    
    model.changeAuthority(conversation.id, user.id, memberByte);
    rightMap.put(conversation.owner, creatorByte);
    rightMap.put(user.id, memberByte);
    
    assertEquals(
        "Check that the accessMap is correctly stored",
        rightMap, model.getPermissionMap(conversation));
  }
  
  @Test
  public void mainPermissionMapAccessTest(){
	User p1 = controller.newUser("p1");
	ConversationHeader chatA = controller.newConversation("chatA", p1.id);
	  
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(chatA);

	assertTrue(
		"Check that the correct map was accessed",
		accessMap.containsKey(p1.id));
	  
	assertEquals(
		"Check that the correct value was stored in the map",
		accessMap.get(p1.id).byteValue(),creatorByte); 
	}
  
  @Test
  public void secureCreatorByteTest(){
	ConversationHeader conversation = spawnTestConversation();
    HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
  
    assertEquals(
        "Check that the creator's byte is 111",
        accessMap.get(conversation.owner).byteValue(),creatorByte);
    
    assertTrue(
        "Check that creator can access chat",
        model.isMember(conversation, conversation.owner));
  }
  
  @Test
  public void secureOwnerByteTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User owner = spawnOwner(conversation, "owner");	
	
	assertTrue(
	    "Check that owners can access chat",
	    model.isMember(conversation, owner.id));  
  }
  
  @Test
  public void secureMemberByteTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User member = controller.newUser("member");
	assertTrue(
		"Check automatic membership for newcomers to chat",
		model.isMember(conversation, member.id));
		    		    
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	assertEquals(
		"Check that newcomer byte is 001",
		accessMap.get(member.id).byteValue(),memberByte);
  }
  
  @Test
  public void secureTrollByteTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User troll = spawnTroll(conversation);
    
    assertFalse(
        "Check that trolls cannot access chat",
        model.isMember(conversation, troll.id));
  }
  
  @Test
  public void noIllegalOwnersAddedTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	
	controller.authorityModificationRequest(conversation.id, users[0], conversation.owner, "o");
	controller.authorityModificationRequest(conversation.id, conversation.owner, conversation.owner, "o");
	controller.authorityModificationRequest(conversation.id, users[1], users[0], "o");
	controller.authorityModificationRequest(conversation.id, users[1], users[1], "o");
	controller.authorityModificationRequest(conversation.id, users[1], users[2], "o");
	
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
		"Check that illegal cases of addOwner do nothing",
		accessMap, model.getPermissionMap(conversation));  
  }
  
  @Test
  public void properDemotionTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	
	controller.authorityModificationRequest(conversation.id, conversation.owner, conversation.owner, "m");
	controller.authorityModificationRequest(conversation.id, users[1], conversation.owner, "m");
	controller.authorityModificationRequest(conversation.id, users[0], users[1], "m");
	controller.authorityModificationRequest(conversation.id, users[0], users[0], "m");
	controller.authorityModificationRequest(conversation.id, users[0], users[2], "m");
	
	HashMap<Uuid,Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
		"Check that illegal cases of demoteOwner do nothing",
		accessMap, model.getPermissionMap(conversation));
	
	controller.authorityModificationRequest(conversation.id, users[0], conversation.owner, "m");
    accessMap = model.getPermissionMap(conversation);
    
    assertEquals(
        "Check that only creators demote owners to members",
        accessMap.get(users[0]).byteValue(), memberByte); 
    
    controller.authorityModificationRequest(conversation.id, users[0], conversation.owner, "o");
    
    assertEquals(
        "Check that only creators make members become owners",
        accessMap.get(users[0]).byteValue(), ownerByte);
  }
  
  @Test
  public void noIllegalBansTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	User newcomer = controller.newUser("newcomer");
    
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	controller.authorityModificationRequest(conversation.id, newcomer.id, users[2], "b");
	controller.authorityModificationRequest(conversation.id, conversation.owner, conversation.owner, "b");    
	controller.authorityModificationRequest(conversation.id, conversation.owner, users[0], "b");
	controller.authorityModificationRequest(conversation.id, users[0], users[1], "b");
	controller.authorityModificationRequest(conversation.id, users[0], users[2], "b");
    
    assertEquals(
        "Check that illegal cases of banning do nothing",
        accessMap, model.getPermissionMap(conversation));
  }
  
  @Test
  public void creatorOwnerBanAddTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User badOwner = spawnOwner(conversation, "owner");
	
	controller.authorityModificationRequest(conversation.id, badOwner.id, conversation.owner, "b");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);  
	
	assertEquals(
	    "Check that a creator can ban an owner",
	    accessMap.get(badOwner.id).byteValue(), trollByte);
	    
	assertFalse(
	    "Check that banned owner #1 cannot read/write messages",
	    model.isMember(conversation, badOwner.id));
	
	controller.authorityModificationRequest(conversation.id, badOwner.id, conversation.owner, "m");
	accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that a creator can add a banned member back to their chat",
	    accessMap.get(badOwner.id).byteValue(), memberByte);
	        
	assertTrue(
	    "Check that added member #1 can join conversation",
	    model.isMember(conversation, badOwner.id));	
  }
  
  @Test
  public void creatorBansMemberTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badMember = spawnMember(conversation);
	
	controller.authorityModificationRequest(conversation.id, badMember.id, conversation.owner, "b");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that a creator can ban a member",
	    accessMap.get(badMember.id).byteValue(), trollByte); 
	    
	assertFalse(
	    "Check that banned member #1 cannot read/write messages",
	    model.isMember(conversation, badMember.id));
  }
  
  @Test
  public void creatorBansNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badNewcomer = controller.newUser("badNewcomer");
	
	controller.authorityModificationRequest(conversation.id, badNewcomer.id, conversation.owner, "b");
    HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
    
    assertEquals(
        "Check that an owner can ban newcomers",
        accessMap.get(badNewcomer.id).byteValue(),trollByte);
        
    assertFalse(
        "Check that banned newcomer #1 cannot join conversation",
        model.isMember(conversation, badNewcomer.id));
  }
  
  @Test
  public void ownerBanAddTest(){
	ConversationHeader conversation = spawnTestConversation();
	User goodOwner = spawnOwner(conversation, "goodOwner");
	User badOwner = spawnOwner(conversation, "badOwner");
	
	controller.authorityModificationRequest(conversation.id, badOwner.id, goodOwner.id, "b");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that an owner can ban other owners",
	    accessMap.get(badOwner.id).byteValue(), trollByte);
	    
	assertFalse(
	    "Check that banned owner #2 cannot read/write messages",
	    model.isMember(conversation, badOwner.id));
	
	controller.authorityModificationRequest(conversation.id, badOwner.id, goodOwner.id, "m");
	accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that an owner can add a banned member back to their chat",
	    accessMap.get(badOwner.id).byteValue(), memberByte);
	    
	assertTrue(
	    "Check that added member #2 can join conversation",
	    model.isMember(conversation, badOwner.id));
  }
  
  @Test
  public void ownerBansMemberTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	
	controller.authorityModificationRequest(conversation.id, users[1], users[0], "b");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that an owner can ban members",
	    accessMap.get(users[1]).byteValue(), trollByte);

	assertFalse(
	    "Check that banned member #2 cannot read/write messages",
	    model.isMember(conversation, users[1]));
  }
  
  @Test
  public void ownerBansNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badNewcomer = controller.newUser("newcomer");
	User owner = spawnOwner(conversation, "owner");
	
	controller.authorityModificationRequest(conversation.id, badNewcomer.id, owner.id, "b");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that an owner can ban newcomers",
	    accessMap.get(badNewcomer.id).byteValue(), trollByte);
	    
	assertFalse(
	    "Check that banned newcomer #1 cannot join conversation",
	    model.isMember(conversation, badNewcomer.id));
  }
  
  @Test
  public void noIllegalMembersAddedTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	controller.authorityModificationRequest(conversation.id, users[0], conversation.owner, "m");
	controller.authorityModificationRequest(conversation.id, conversation.owner, users[0], "m");
	controller.authorityModificationRequest(conversation.id, users[1], users[0], "m");
	controller.authorityModificationRequest(conversation.id, users[2], users[1], "m");
	controller.authorityModificationRequest(conversation.id, users[1], users[2], "m");
    
    assertEquals(
        "Check that illegal cases of addMember do nothing",
        accessMap, model.getPermissionMap(conversation));
  }
  
  @Test
  public void creatorAddsNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User newcomer = controller.newUser("newcomer");
	controller.authorityModificationRequest(conversation.id, newcomer.id, conversation.owner, "m");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	
	assertEquals(
	    "Check that a creator can add a newcomer not in chat to their chat",
	    accessMap.get(newcomer.id).byteValue(), memberByte);
	    
	assertTrue(
	    "Check that added newcomer #1 can join conversation",
	    model.isMember(conversation, newcomer.id));
  }
  
  @Test
  public void ownerAddsNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User newcomer = controller.newUser("newcomer");
	controller.authorityModificationRequest(conversation.id, newcomer.id, conversation.owner, "m");
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
		
	assertEquals(
		"Check that a creator can add a newcomer not in chat to their chat",
		accessMap.get(newcomer.id).byteValue(), memberByte);
		    
	assertTrue(
		"Check that added newcomer #2 can join conversation",
		model.isMember(conversation, newcomer.id));  
  }
  
  // The following tests simulate a more realistic situation where different users have
  // different roles in the conversations they contribute to.
    
  @Test
  public void tandemRoleStorageTest() {
	ConversationHeader chatA = spawnTestConversation();
	User p2 = spawnMember(chatA);
	User p3 = spawnOwner(chatA, "p3");
		
	ConversationHeader chatB = controller.newConversation("chatB", p2.id);
	controller.authorityModificationRequest(chatB.id, p3.id, p2.id, "b");
		
	HashMap<Uuid, Byte> rightMap = new HashMap<Uuid, Byte>();
	rightMap.put(chatA.owner, creatorByte);
	rightMap.put(p2.id, memberByte);
	rightMap.put(p3.id, ownerByte);
		
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(chatA);
		
	assertEquals(accessMap, rightMap);
		
	rightMap.clear();
	rightMap.put(p2.id, creatorByte);
	rightMap.put(p3.id, trollByte);
		
	accessMap = model.getPermissionMap(chatB);
		
	assertEquals(accessMap, rightMap); 
  }
  
  @Test
  public void earlyBanReadWriteTest() {
	ConversationHeader conversation = spawnTestConversation();
	User troll = spawnMember(conversation);
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	controller.authorityModificationRequest(conversation.id, troll.id, conversation.owner, "b");
	
	final Message messageFailed = controller.newMessage(troll.id, conversation.id,
	    "BANNED USER ERROR: P1's message shouldn't be seen in chatC");
	    
	final Message message1 = controller.newMessage(conversation.owner, conversation.id,
	    "beep boop I love soup");
	    
	assertNull(
	    "Check that conversation so far started with a member",
	    rawView.findMessage(message1.previous));
  }
  
  @Test
  public void nestledBanReadWriteTest() {
	ConversationHeader conversation = spawnTestConversation();
	User troll = spawnMember(conversation);  
		
	final Message message1 = controller.newMessage(conversation.owner, conversation.id,
		"beep boop I love soup");
		
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	controller.authorityModificationRequest(conversation.id, troll.id, conversation.owner, "b");
		
	final Message messageFailed = controller.newMessage(troll.id, conversation.id,
		"BANNED USER ERROR: P1's message shouldn't be seen in chatC");
	
	final Message message2 = controller.newMessage(conversation.owner, conversation.id,
		"anyway, that's all, signing off now");
	
	assertEquals(
		"Check that conversation contains no troll messages",
		rawView.findMessage(message1.next), message2);  
  }
  
  @Test
  public void finalBanReadWriteTest() {
	ConversationHeader conversation = spawnTestConversation();
	User troll = spawnMember(conversation);  
	
	final Message message1 = controller.newMessage(conversation.owner, conversation.id,
		"beep boop I love soup");
	
	HashMap<Uuid, Byte> accessMap = model.getPermissionMap(conversation);
	controller.authorityModificationRequest(conversation.id, troll.id, conversation.owner, "b");
	
	final Message messageFailed = controller.newMessage(troll.id, conversation.id,
		"BANNED USER ERROR: P1's message shouldn't be seen in chatC");
	
	assertNull(
		"Check that conversation ended with a member",
		rawView.findMessage(message1.next));
  }
  
  private ConversationHeader spawnTestConversation(){
	User creator = controller.newUser("creator");
	return controller.newConversation("conversation", creator.id);
  }
  
  private User spawnOwner(ConversationHeader conversation, String str){
	User owner = controller.newUser(str);
	model.changeAuthority(conversation.id, owner.id, ownerByte);
	return owner;
  }
  
  private User spawnMember(ConversationHeader conversation){
	User member = controller.newUser("member");
	model.changeAuthority(conversation.id, member.id, memberByte);
	return member;
  }
  
  private User spawnTroll(ConversationHeader conversation){
	User troll = controller.newUser("troll");
	model.changeAuthority(conversation.id, troll.id, trollByte);
	return troll;
  }
  
  private Uuid[] spawnRest(ConversationHeader conversation){
	User owner = spawnOwner(conversation, "owner");
	User member = spawnMember(conversation);
	User troll = spawnTroll(conversation);
	Uuid[] result = {owner.id, member.id, troll.id};
	return result;
  }
  
}