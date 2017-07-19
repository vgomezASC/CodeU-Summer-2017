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
 *  - codeu.chat.server.View.getPermissionMapAsString(ConversationHeader conversation)
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
  
  // The following test simulates a more realistic situation where different users have
  // different roles in the conversations they contribute to.
  @Test
  public void multiConversationTest(){
    User p1 = controller.newUser("p1");
    User p2 = controller.newUser("p2");
    User p3 = controller.newUser("p3");
    
    byte trollByte = 0b000;
    byte memberByte = 0b001;
    byte ownerByte = 0b011;
    byte creatorByte = 0b111;
    
    ConversationHeader chatA = controller.newConversation("chatA", p1.id);
    ConversationHeader chatB = controller.newConversation("chatB", p1.id);
    
    final Message messageA1 = controller.newMessage(p1.id, chatA.id,
        "Hello and welcome to the surfing chat. Hang ten dudes~");
    
    controller.checkMembership(p2, chatA);
    final Message messageA2 = controller.newMessage(p2.id, chatA.id,
        "cowabungaaaaaaaaaaaa");
        
    HashMap<Uuid, Byte> accessMap = controller.getPermissionMap(chatA);
    controller.addOwner(accessMap.get(p1), p2.id, chatA);
    
    controller.checkMembership(p3, chatA);
    final Message messageA3 = controller.newMessage(p3.id, chatA.id,
        "surfing suxx. landlubbers 4 lyfe (END)");
    
    accessMap = controller.getPermissionMap(chatA);    
    controller.ban(accessMap.get(p2), p3.id, chatA); 
    
    final Message messageAFailed = controller.newMessage(p3.id, chatA.id,
        "BANNED USER ERROR: P3's message shouldn't be seen in chatA");
        
    assertFalse("Check that p3 has been successfully banned from chatA",
        controller.checkMembership(p3, chatA));
                
    final Message messageB1 = controller.newMessage(p1.id, chatB.id,
        "What's up everybody this chat is about outer space.");
    
    controller.checkMembership(p2, chatB);    
    accessMap = controller.getPermissionMap(chatB);
    controller.addOwner(accessMap.get(p1), p3.id, chatB);
    
    final Message messageB2 = controller.newMessage(p2.id, chatB.id,
        "I like Saturn");
    final Message messageB3 = controller.newMessage(p3.id, chatB.id,
        "the moon is my friend (END)");
    
    ConversationHeader chatC = controller.newConversation("chatC", p2.id);
    
    accessMap = controller.getPermissionMap(chatC);
    controller.ban(accessMap.get(p2), p1.id, chatC);
    
    final Message messageCFailed1 = controller.newMessage(p1.id, chatC.id,
        "BANNED USER ERROR: P1's message shouldn't be seen in chatC");
    
    final Message messageC1 = controller.newMessage(p2.id, chatC.id,
        "beep boop I love soup");
    
    assertFalse(
        "Check that conversation so far started with a member",
        !view.findMessage(messageC1.previous).equals(messageCFailed1));
    
    accessMap = controller.getPermissionMap(chatC);
    controller.ban(accessMap.get(p2), p3.id, chatC);
    
    final Message messageCFailed2 = controller.newMessage(p3.id, chatC.id,
        "BANNED USER ERROR: P3's message shouldn't be seen in chatC");
        
    final Message messageC2 = controller.newMessage(p2.id, chatC.id,
        "anyway how are you all doing tonight? (END)");
    
    ConversationHeader chatD = controller.newConversation("chatD", p3.id);
    
    controller.checkMembership(p2, chatD);
    final Message messageD1 = controller.newMessage(p2.id, chatD.id,
        "ever notice that not every conversation begins the same way?");
    final Message messageD2 = controller.newMessage(p2.id, chatD.id,
        "anyway, don't ask me about my double-dipping crimes");
    final Message messageD3 = controller.newMessage(p3.id, chatD.id,
        "NO DOUBLE-DIPPERS IN CHAT D ON MY WATCH, PUNK (END)");
    accessMap = controller.getPermissionMap(chatD);
    controller.ban(accessMap.get(p3),p2.id, chatD);
    
    final Message messageDFailed = controller.newMessage(p2.id, chatD.id,
        "BANNED USER ERROR: P2's message shouldn't be seen in chatD");
    
    // So here's Phase 1 of how this test checks this real-life scenario:
    // We're gonna need a method like getPermissionMap except once it has the map,
    // it converts all the contents into a nice String. Once we got that under control,
    // we'll upgrade these strings to something that'll work nicely with it.
        
    String aStr, bStr, cStr, dStr;  
    
    assertFalse(
        "Check that the final state of Chat A's map is correct", 
        !aStr.equals(view.getPermissionMapAsString(chatA)));
        
    assertFalse(
        "Check that the final state of Chat B's map is correct", 
        !bStr.equals(view.getPermissionMapAsString(chatB)));
        
    assertFalse(
        "Check that the final state of Chat C's map is correct", 
        !cStr.equals(view.getPermissionMapAsString(chatC)));
        
    assertFalse(
        "Check that the final state of Chat D's map is correct", 
        !dStr.equals(view.getPermissionMapAsString(chatD)));
    
    
    // Phase 2 checks each chatlog to ensure no banned member was able to write in.
    // Every chat's ending message has (END) in it and a banned member will try to write 
    // after that.
        
    System.out.println("CHATLOGS");    
    Message[] conversationIterators = {messageA1, messageB1, messageC1, messageD1};
    for (Message m: conversationIterators){
      String convoTitle = view.findConversation(m.conversation).title;
      while (m.next != Uuid.NULL){
        m = view.findMessage(m.next);
        if (m.content.equals(messageCFailed1.content))
          System.out.println("ERROR: banned user successfully wrote to chatC!");
      };
      assertFalse("Check that "+convoTitle+"'s last message has the proper end",
      !(m.content.indexOf("(END)")>= 0));
    }   
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