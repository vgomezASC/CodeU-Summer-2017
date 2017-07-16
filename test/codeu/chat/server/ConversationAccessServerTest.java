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

  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model);
    view = new View(model);
  }

  @Test
  public void testAddConversation() {

    final User user = controller.newUser("user");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

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
    HashMap<Uuid, byte> accessMap = controller.getPermissionMap(conversation);
    
    byte memberByte = 0b001; // Before testing, this must be the equivalent of 001.
    
    accessMap.put(user.id, memberByte);
    controller.updatePermissionMap(conversation, accessMap);
    
    assertFalse(
        "Check that the allowances can be updated",
        controller.getPermissionMap(conversation).size()!= 1);
    
  }
  
  @Test
  public void testAccessSecurity(){
    final User creator = controller.newUser("creator"); 
    final User owner = controller.newUser("owner");
    final User member = controller.newUser("member");
    final User troll = controller.newUser("troll");
    
    ConversationHeader conversation = controller.newConversation("conversation", creator.id);
    
    byte creatorByte = 0b111; // Before testing, this must be the equivalent of 111.
    HashMap<Uuid, byte> accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that the creator's byte is 111",
        !accessMap.get(creator.id).equals(c));
    
    byte ownerByte = 0b011; // Before testing, this must be the equivalent of 011.
    accessMap.put(owner.id, ownerByte);
    byte trollByte = 0b000; // Before testing, this must be the equivalent of 000.
    accessMap.put(troll.id, trollByte);
    controller.updatePermissionMap(conversation, accessMap);
    
    assertFalse(
        "Check that creator can access chat",
        !controller.checkMembership(creator.id, conversation));
    
    assertFalse(
        "Check that owners can access chat",
        !controller.checkMembership(owner.id, conversation));
        
    assertFalse(
    	"Check automatic membership for newcomers to chat",
    	!controller.checkMembership(member.id, conversation));
    	
    assertFalse(
    	"Check that members can access chat",
    	!controller.checkMembership(member.id, conversation));
    
    byte memberByte = 0b001; // Before testing, this must be the equivalent of 001.
    
    accessMap = controller.getPermissionMap(conversation);
    assertFalse(
        "Check that newcomer byte is 001",
        accessMap.get(member.id).equals(memberByte));
    	
    assertFalse(
    	"Check that trolls cannot access chat",
    	controller.checkMembership(troll.id, conversation));
    
    controller.demoteOwner(creatorByte, creator.id, conversation);
    controller.demoteOwner(creatorByte, member.id, conversation);
    controller.demoteOwner(memberByte, owner.id, conversation);
    controller.demoteOwner(ownerByte, owner.id, conversation);
    controller.demoteOwner(trollByte, owner.id, conversation);
    
    assertFalse(
        "Check that illegal cases of demoteOwner do nothing",
        !accessMap.equals(controller.getPermissionMap(conversation)));
    
    controller.demoteOwner(creatorByte, owner.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that only creators demote owners to members",
        !accessMap.get(owner.id).equals(memberByte)); 
    
    controller.addOwner(creatorByte, owner.id, conversation);
    
    assertFalse(
        "Check that only creators make members become owners",
        !accessMap.get(owner.id).equals(ownerByte));    
            
    controller.addOwner(creatorByte, owner.id, conversation);
    controller.addOwner(creatorByte, creator.id, conversation);
    controller.addOwner(ownerByte, member.id, conversation);
    controller.addOwner(memberByte, member.id, conversation);
    controller.addOwner(trollByte, member.id, conversation);
    
    assertFalse(
        "Check that illegal cases of addOwner do nothing",
        !accessMap.equals(controller.getPermissionMap(conversation)));
    
    User instantiatedMember1 = controller.newUser("IM1");
    
    controller.ban(trollByte, instantiatedMember1.id, conversation);    
    controller.ban(creatorByte, creator.id, conversation);
    controller.ban(ownerByte, creator.id, conversation);
    controller.ban(memberByte, owner.id, conversation);
    controller.ban(trollByte, owner.id, conversation);
    
    assertFalse(
        "Check that illegal cases of banning do nothing",
        !accessMap.equals(controller.getPermissionMap(conversation)));
    
    User badOwner1 = controller.newUser("badOwner1");
    accessMap.put(badOwner1, ownerByte);
    controller.updatePermissionMap(conversation, accessMap);
    controller.ban(creatorByte, badOwner1.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can ban an owner",
        !accessMap.get(badOwner1.id).equals(trollByte));
    
    assertFalse(
         "Check that banned owner #1 cannot read/write messages",
         controller.checkMembership(badOwner1.id, conversation));
    
    controller.ban(creatorByte, member.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can ban a member",
        !accessMap.get(member.id).equals(trollByte)); 
    
    assertFalse(
         "Check that banned member #1 cannot read/write messages",
         controller.checkMembership(member.id, conversation));
    
    User newcomer1 = controller.newUser("newcomer1");
    controller.ban(creatorByte, newcomer1.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can ban newcomers",
        !accessMap.get(newcomer1.id).equals(trollByte));
    
    assertFalse(
         "Check that banned newcomer #1 cannot join conversation",
         controller.checkMembership(newcomer1.id, conversation));
    
    User badOwner2 = controller.newUser("badOwner2");
    accessMap.put(badOwner2, ownerByte);
    controller.updatePermissionMap(conversation, accessMap);
    controller.ban(ownerByte, badOwner2.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can ban other owners",
        !accessMap.get(badOwner2.id).equals(trollByte));
    
    assertFalse(
         "Check that banned owner #2 cannot read/write messages",
         controller.checkMembership(badOwner2.id, conversation));
    
    User badMember = controller.newUser("badMember");
    accessMap.put(badMember.id, memberByte);
    controller.updatePermissionMap(conversation, accessMap);
    controller.ban(creatorByte, badMember.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can ban members",
        !accessMap.get(badMember.id).equals(trollByte));

	assertFalse(
         "Check that banned member #2 cannot read/write messages",
         controller.checkMembership(badMember1.id, conversation));
	
	User newcomer2 = controller.newUser("newcomer2");
    controller.ban(creatorByte, newcomer2.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can ban newcomers",
        !accessMap.get(newcomer2.id).equals(trollByte));
    
    assertFalse(
         "Check that banned newcomer #2 cannot join conversation",
         controller.checkMembership(newcomer2.id, conversation));

    controller.addMember(creatorByte, owner.id, conversation);
    controller.addMember(ownerByte, creator.id, conversation);
    controller.addMember(ownerByte, member.id, conversation);
    controller.addMember(memberByte, troll.id, conversation);
    controller.addMember(trollByte, member.id, conversation);
    
    assertFalse(
        "Check that illegal cases of addMember do nothing",
        !accessMap.equals(controller.getPermissionMap(conversation)));
    
    User newcomer3 = controller.newUser("newcomer3");
    controller.addMember(creatorByte, newcomer3.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can add a newcomer not in chat to their chat",
        !accessMap.get(newcomer3.id).equals(memberByte));
    
    assertFalse(
         "Check that added newcomer #1 can join conversation",
         !controller.checkMembership(newcomer3.id, conversation));
        
    controller.addMember(creatorByte, badOwner1.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can add a banned member back to their chat",
        !accessMap.get(badOwner1.id).equals(memberByte));
        
    assertFalse(
         "Check that added member #1 can join conversation",
         !controller.checkMembership(badOwner1.id, conversation));
        
    User newcomer4 = controller.newUser("newcomer4");
    controller.addMember(ownerByte, newcomer4.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can add a newcomer not in chat to their chat",
        !accessMap.get(newcomer4.id).equals(memberByte));
        
    assertFalse(
         "Check that added newcomer #2 can join conversation",
         !controller.checkMembership(newcomer4.id, conversation));
    
    controller.addMember(ownerByte, badOwner2.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can add a banned member back to their chat",
        !accessMap.get(badOwner2.id).equals(memberByte));
    
    assertFalse(
         "Check that added member #2 can join conversation",
         !controller.checkMembership(badOwner2.id, conversation));
    
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
        
    HashMap<Uuid,byte> accessMap = controller.getPermissionMap(chatA);
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
    
    final Message messageB1 = controller.newMessage(p2.id, chatB.id,
        "I like Saturn");
    final Message messageB2 = controller.newMessage(p3.id, chatB.id,
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
    Message[] conversationIterators = [messageA1, messageB1, messageC1, messageD1];
    for (Message m: conversationIterators){
      String convoTitle = view.findConversation(m.conversation).title;
      while (m.next != Uuid.NULL){
        m = view.findMessage(m.next);
        if (m.content.equals(messageCFailed.content))
          System.out.println("ERROR: banned user successfully wrote to chatC!");
      };
      assertFalse("Check that "+convoTitle+"'s last message has the proper end",
      !m.content.indexOf("(END)")>= 0);
    }   
  }
  
}