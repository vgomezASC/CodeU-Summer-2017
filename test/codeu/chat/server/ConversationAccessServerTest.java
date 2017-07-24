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
 *  Replaceable parts:
 *  - codeu.chat.server.Controller.getPermissionMap(ConversationHeader conversation)
 *  - codeu.chat.server.Controller.updatePermissionMap(Uuid id, HashMap<Uuid, byte> accessMap)
 *  - codeu.chat.server.Controller.checkMembership(Uuid id, ConversationHeader conversation)
 *  - ...server.Controller.demoteOwner(byte access, Uuid target, ConversationHeader conversation)
 *  - ...server.Controller.addOwner(byte access, Uuid target, ConversationHeader conversation)
 *  - ...server.Controller.ban(byte access, Uuid target, ConversationHeader conversation)
 *  - ...server.Controller.addMember(byte access, Uuid target, ConversationHeader conversation)
 *       
 */


package codeu.chat.server;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.common.BasicController;
import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class ConversationAccessServerTest {

  private Model model;
  private BasicController controller;
  private BasicView view;
  private byte creatorByte = 0b111; // Before testing, this must be the equivalent of 111.
  private byte ownerByte = 0b011; // Before testing, this must be the equivalent of 011.
  private byte memberByte = 0b001; // Before testing, this must be the equivalent of 001.
  private byte trollByte = 0b000; // Before testing, this must be the equivalent of 000.
  
  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model);
    view = new View(model);			
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

    assertFalse(
        "Check that conversation has a valid reference",
        conversation == null);    
        
    assertNotNull(
         "Check that user has a conversation access map",
          controller.getPermissionMap(conversation));
  }
  
  @Test
  public void testGetAndUpdateAccess(){
    final User creator = controller.newUser("creator");
    final User user = controller.newUser("user");
     
    ConversationHeader conversation = controller.newConversation("chat", creator.id);
    HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
    
    byte memberByte = 0b001; // Before testing, this must be the equivalent of 001.
    
    accessMap.put(user.id, memberByte);
    controller.updatePermissionMap(conversation, accessMap);
    
    assertTrue(
        "Check that the allowances can be updated",
        controller.getPermissionMap(conversation).size()== 1);
    
  }
  
  @Test
  public void secureCreatorByteTest(){
	ConversationHeader conversation = spawnTestConversation();
    HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
  
    assertTrue(
        "Check that the creator's byte is 111",
        accessMap.get(creator.id).byteValue()==creatorByte);
    
    assertTrue(
        "Check that creator can access chat",
        controller.checkMembership(creator.id, conversation));
  }
  
  @Test
  public void secureOwnerByteTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User owner = spawnOwner(conversation);	
	
	assertTrue(
	    "Check that owners can access chat",
	    controller.checkMembership(owner.id, conversation));  
  }
  
  @Test
  public void secureMemberByteTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User member = controller.newUser("member");
	assertTrue(
		"Check automatic membership for newcomers to chat",
		controller.checkMembership(member.id, conversation));
		    	
	assertTrue(
		"Check that members can access chat",
		controller.checkMembership(member.id, conversation));
		    
	accessMap = controller.getPermissionMap(conversation);
	assertFalse(
		"Check that newcomer byte is 001",
		accessMap.get(member.id).byteValue()==memberByte);
  }
  
  @Test
  public void secureTrollByteTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User troll = spawnTroll(conversation);
    
    assertFalse(
        "Check that trolls cannot access chat",
        controller.checkMembership(troll.id, conversation));
  }
  
  @Test
  public void noIllegalOwnersAddedTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	  
	controller.addOwner(creatorByte, users[0], conversation);
	controller.addOwner(creatorByte, conversation.owner, conversation);
	controller.addOwner(ownerByte, users[1], conversation);
	controller.addOwner(memberByte,users[1], conversation);
	controller.addOwner(trollByte, users[1], conversation);
	  
	assertTrue(
		"Check that illegal cases of addOwner do nothing",
		accessMap.equals(controller.getPermissionMap(conversation)));  
  }
  
  @Test
  public void properDemotionTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
		
	controller.demoteOwner(creatorByte, conversation.owner, conversation);
	controller.demoteOwner(creatorByte, users[1], conversation);
	controller.demoteOwner(memberByte,users[0], conversation);
	controller.demoteOwner(ownerByte, users[0], conversation);
	controller.demoteOwner(trollByte, users[0], conversation);
	  
	assertTrue(
		"Check that illegal cases of demoteOwner do nothing",
		accessMap.equals(controller.getPermissionMap(conversation)));
	
	controller.demoteOwner(creatorByte, users[0], conversation);
    HashMap<Uuid,Byte> accessMap = controller.getPermissionMap(conversation);
    
    assertTrue(
        "Check that only creators demote owners to members",
        accessMap.get(users[0]).byteValue()==memberByte); 
    
    controller.addOwner(creatorByte, users[0], conversation);
    
    assertTrue(
        "Check that only creators make members become owners",
        accessMap.get(users[0]).byteValue()==ownerByte);
  }
  
  @Test
  public void noIllegalBansTest(){
	ConversationHeader conversation = spawnTestConversation();
	User owner = spawnOwner(conversation);
	User newcomer = controller.newUser("newcomer");
    
    controller.ban(trollByte, newcomer.id, conversation);    
    controller.ban(creatorByte, conversation.owner, conversation);
    controller.ban(ownerByte, conversation.owner, conversation);
    controller.ban(memberByte,owner.id, conversation);
    controller.ban(trollByte, owner.id, conversation);
    controller.ban(creatorByte, conversation.owner, conversation);
    
    assertTrue(
        "Check that illegal cases of banning do nothing",
        accessMap.equals(controller.getPermissionMap(conversation)));
  }
  
  @Test
  public void creatorOwnerBanAddTest(){
	ConversationHeader conversation = spawnTestConversation();
	final User badOwner = spawnOwner(conversation);
	
	controller.ban(creatorByte, badOwner.id, conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);  
	
	assertTrue(
	    "Check that a creator can ban an owner",
	    accessMap.get(badOwner.id).byteValue()==trollByte);
	    
	assertFalse(
	    "Check that banned owner #1 cannot read/write messages",
	    controller.checkMembership(badOwner.id, conversation));
	
	controller.addMember(creatorByte, badOwner.id, conversation);
	accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that a creator can add a banned member back to their chat",
	    accessMap.get(badOwner.id).byteValue()==memberByte);
	        
	assertTrue(
	    "Check that added member #1 can join conversation",
	    controller.checkMembership(badOwner1.id, conversation));	
  }
  
  @Test
  public void creatorBansMemberTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badMember = spawnMember(conversation);
	  
	controller.ban(creatorByte, badMember.id, conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that a creator can ban a member",
	    accessMap.get(member.id).byteValue()==trollByte); 
	    
	assertFalse(
	    "Check that banned member #1 cannot read/write messages",
	    controller.checkMembership(member.id, conversation));
  }
  
  @Test
  public void creatorBansNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badNewcomer = controller.newUser("badNewcomer");
	  
	controller.ban(creatorByte, badNewcomer.id, conversation);
    HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
    
    assertTrue(
        "Check that an owner can ban newcomers",
        accessMap.get(badNewcomer.id).byteValue()==trollByte);
        
    assertFalse(
        "Check that banned newcomer #1 cannot join conversation",
        controller.checkMembership(badNewcomer.id, conversation));
  }
  
  @Test
  public void ownerBanAddTest(){
	ConversationHeader conversation = spawnTestConversation();
	User owner = spawnOwner(conversation);
	
	controller.ban(ownerByte, owner.id, conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that an owner can ban other owners",
	    accessMap.get(owner.id).byteValue()==trollByte);
	    
	assertFalse(
	    "Check that banned owner #2 cannot read/write messages",
	    controller.checkMembership(owner.id, conversation));
	
	controller.addMember(ownerByte, owner.id, conversation);
	accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that an owner can add a banned member back to their chat",
	    accessMap.get(owner.id).byteValue()==memberByte);
	    
	assertTrue(
	    "Check that added member #2 can join conversation",
	    controller.checkMembership(owner.id, conversation));
  }
  
  @Test
  public void ownerBansMemberTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badMember = spawnMember(conversation);
	
	controller.ban(ownerByte, badMember.id, conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that an owner can ban members",
	    accessMap.get(badMember.id).byteValue()==trollByte);

	assertFalse(
	    "Check that banned member #2 cannot read/write messages",
	    controller.checkMembership(badMember.id, conversation));
  }
  
  @Test
  public void ownerBansNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User badNewcomer = controller.newUser("newcomer");
	
	controller.ban(ownerByte, badNewcomer.id, conversation); 
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that an owner can ban newcomers",
	    accessMap.get(badNewcomer.id).byteValue()==trollByte);
	    
	assertFalse(
	    "Check that banned newcomer #1 cannot join conversation",
	    controller.checkMembership(badNewcomer.id, conversation));
  }
  
  @Test
  public void noIllegalMembersAddedTest(){
	ConversationHeader conversation = spawnTestConversation();
	Uuid[] users = spawnRest(conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	
	controller.addMember(creatorByte, users[0], conversation);
    controller.addMember(ownerByte, conversation.owner, conversation);
    controller.addMember(ownerByte, users[1], conversation);
    controller.addMember(memberByte, users[2], conversation);
    controller.addMember(trollByte, users[1], conversation);
    
    assertTrue(
        "Check that illegal cases of addMember do nothing",
        accessMap.equals(controller.getPermissionMap(conversation)));
  }
  
  @Test
  public void creatorAddsNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User newcomer = controller.newUser("newcomer");
	controller.addMember(creatorByte, newcomer.id, conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	
	assertTrue(
	    "Check that a creator can add a newcomer not in chat to their chat",
	    accessMap.get(newcomer.id).byteValue()==memberByte);
	    
	assertTrue(
	    "Check that added newcomer #1 can join conversation",
	    controller.checkMembership(newcomer.id, conversation));
  }
  
  @Test
  public void ownerAddsNewcomerTest(){
	ConversationHeader conversation = spawnTestConversation();
	User newcomer = controller.newUser("newcomer");
	controller.addMember(ownerByte, newcomer.id, conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
		
	assertTrue(
		"Check that a creator can add a newcomer not in chat to their chat",
		accessMap.get(newcomer.id).byteValue()==memberByte);
		    
	assertTrue(
		"Check that added newcomer #2 can join conversation",
		controller.checkMembership(newcomer.id, conversation));  
  }
  
  // The following tests simulate a more realistic situation where different users have
  // different roles in the conversations they contribute to.
  
  @Test
  public void byteReferenceCompatibilityTest() {
	ConversationHeader conversation = spawnTestConversation();
	User owner = controller.newUser("owner");
	
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	controller.addOwner(accessMap.get(conversation.owner),owner,conversation);
	accessMap = controller.getPermissionMap(conversation);

	assertTrue(
		"Check that .addOwner and similar methods work with indirect byte references",
		accessMap.get(owner.id).byteValue()==ownerByte);
  }
  
  @Test
  public void tandemRoleStorageTest() {
	ConversationHeader chatA = spawnTestConversation();
	User p2 = spawnMember(chatA);
	User p3 = spawnOwner(chatA);
		
	ConversationHeader chatB = controller.newConversation("chatB", creator.id);
	controller.ban(creatorByte, p3.id, chatB);
		
	HashMap<Uuid, Byte> rightMap = new HashMap<Uuid, Byte>();
	rightMap.put(chatA.id, creatorByte);
	rightMap.put(p2.id, memberByte);
	rightMap.put(p3.id, ownerByte);
		
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(chatA);
		
	assertTrue(accessMap.equals(rightMap));
		
	rightMap.clear();
	rightMap.put(p2.id, creatorByte);
	rightMap.put(p3.id, trollByte);
		
	accessMap = controller.getPermissionMap(chatB);
		
	assertTrue(accessMap.equals(rightMap)); 
  }
  
  @Test
  public void earlyBanReadWriteTest() {
	ConversationHeader conversation = spawnTestConversation();
	User troll = spawnMember(conversation);
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	controller.ban(accessMap.get(conversation.owner),troll.id, conversation);
	
	final Message messageFailed = controller.newMessage(troll.id, conversation.id,
	    "BANNED USER ERROR: P1's message shouldn't be seen in chatC");
	    
	final Message message1 = controller.newMessage(conversation.owner, conversation.id,
	    "beep boop I love soup");
	    
	assertFalse(
	    "Check that conversation so far started with a member",
	    view.findMessage(message1.previous).equals(messageFailed));
  }
  
  @Test
  public void nestledBanReadWriteTest() {
	ConversationHeader conversation = spawnTestConversation();
	User troll = spawnMember(conversation);  
		
	final Message message1 = controller.newMessage(conversation.owner, conversation.id,
		"beep boop I love soup");
		
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	controller.ban(accessMap.get(conversation.owner),troll.id, conversation);
		
	final Message messageFailed = controller.newMessage(troll.id, conversation.id,
		"BANNED USER ERROR: P1's message shouldn't be seen in chatC");
	
	final Message message2 = controller.newMessage(conversation.owner, conversation.id,
		"anyway, that's all, signing off now");
	
	assertTrue(
		"Check that conversation contains no troll messages",
		view.findMessage(message1.next).equals(message2));  
  }
  
  @Test
  public void finalBanReadWriteTest() {
	ConversationHeader conversation = spawnTestConversation();
	User troll = spawnMember(conversation);  
	
	final Message message1 = controller.newMessage(conversation.owner, conversation.id,
		"beep boop I love soup");
	
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	controller.ban(accessMap.get(conversation.owner),troll.id, conversation);
	
	final Message messageFailed = controller.newMessage(troll.id, conversation.id,
		"BANNED USER ERROR: P1's message shouldn't be seen in chatC");
	
	assertNull(
		"Check that conversation ended with a member",
		view.findMessage(message1.next));
  }
  
  private ConversationHeader spawnTestConversation(){
	User creator = controller.newUser("creator");
	return controller.newConversation("conversation", creator.id);
  }
  
  private User spawnOwner(ConversationHeader conversation){
	User owner = controller.newUser("owner");
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	accessMap.put(owner.id, ownerByte);
	controller.updatePermissionMap(conversation, accessMap);
	return owner;
  }
  
  private User spawnMember(ConversationHeader conversation){
	User member = controller.newUser("member");
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	accessMap.put(member.id, memberByte);
	controller.updatePermissionMap(conversation, accessMap);
	return member;
  }
  
  private User spawnTroll(ConversationHeader conversation){
	User troll = controller.newUser("troll");
	HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(conversation);
	accessMap.put(troll.id, trollByte);
	controller.updatePermissionMap(conversation, accessMap);
	return troll;
  }
  
  private Uuid[] spawnRest(ConversationHeader conversation){
	User owner = spawnOwner(conversation);
	User member = spawnMember(conversation);
	User troll = spawnTroll(conversation);
	Uuid[] result = {owner.id, member.id, troll.id};
	return result;
  }
  
}