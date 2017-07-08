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
 *    
 */


package codeu.chat.server;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class ConversationAccessServerTest {

  private Model model;
  private BasicController controller;

  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model);
  }

  @Test
  public void testAddUser() {

    final User user = controller.newUser("user");

    assertFalse(
        "Check that user has a valid reference",
        user == null);
    
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
  public void testAddMessage() {

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

    final Message message = controller.newMessage(
        user.id,
        conversation.id,
        "Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message == null);
  }
  
  @Test
  public void testGetAndUpdateAccess {
    final User creator = controller.newUser("creator");
    final User user = controller.newUser("user");
     
    ConversationHeader conversation = controller.newConversation("chat", creator.id);
    HashMap<Uuid, byte> accessMap = controller.getPermissionMap(conversation);
    
    byte memberByte; // Before testing, change this line to the equivalent of 001.
    
    accessMap.put(user.id, memberByte);
    controller.updatePermissionMap(conversation, accessMap);
    
    assertFalse(
        "Check that the allowances can be updated",
        controller.getPermissionMap(conversation).size()!= 1);
    
  }
  
  @Test
  public void testAccessSecurity{
    final User creator = controller.newUser("creator"); 
    final User owner = controller.newUser("owner");
    final User member = controller.newUser("member");
    final User troll = controller.newUser("troll");
    
    ConversationHeader conversation = controller.newConversation("conversation", creator.id);
    
    byte creatorByte; // Before testing, change this line to the equivalent of 111.
    HashMap<Uuid, byte> accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that the creator's byte is 111",
        !accessMap.get(creator.id).equals(c));
    
    byte ownerByte; // Before testing, change this line to the equivalent of 011.
    accessMap.put(owner.id, ownerByte);
    byte trollByte; // Before testing, change this line to the equivalent of 000.
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
    
    byte memberByte; // Before testing, change this line to the equivalent of 001.
    
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
        
    controller.ban(creatorByte, creator.id, conversation);
    controller.ban(ownerByte, creator.id, conversation);
    controller.ban(memberByte, owner.id, conversation);
    controller.ban(trollByte, owner.id, conversation);
    
    assertFalse(
        "Check that illegal cases of banning do nothing",
        !accessMap.equals(controller.getPermissionMap(conversation)));
    
     User badOwner1 = controller.newUser("badOwner1");
    accessMap.put(badOwner1, ownerByte);
    controller.update(conversation, accessMap);
    controller.ban(creatorByte, badOwner1.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can ban an owner",
        !accessMap.get(badOwner1.id).equals(trollByte));
    
    controller.ban(creatorByte, member.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can ban a member",
        !accessMap.get(member.id).equals(trollByte)); 
    
    User badOwner2 = controller.newUser("badOwner2");
    accessMap.put(badOwner2, ownerByte);
    controller.update(conversation, accessMap);
    controller.ban(ownerByte, badOwner2.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can ban other owners",
        !accessMap.get(badOwner2.id).equals(trollByte));
    
    User badMember = controller.newUser("badMember");
    accessMap.put(badMember.id, memberByte);
    controller.update(conversation, accessMap);
    controller.ban(creatorByte, badMember.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that an owner can ban members",
        !accessMap.get(badMember.id).equals(trollByte));

    controller.addMember(creatorByte, owner.id, conversation);
    controller.addMember(ownerByte, creator.id, conversation);
    controller.addMember(ownerByte, member.id, conversation);
    controller.addMember(memberByte, troll.id, conversation);
    controller.addMember(trollByte, member.id, conversation);
    
    assertFalse(
        "Check that illegal cases of addMember do nothing",
        !accessMap.equals(controller.getPermissionMap(conversation)));
    
    User newcomer1 = controller.newUser("newcomer1");
    controller.addMember(creatorByte, newcomer1.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can add a newcomer not in chat to their chat",
        !accessMap.get(newcomer1.id).equals(memberByte));
    
    controller.addMember(creatorByte, badOwner1.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can add a banned member back to their chat",
        !accessMap.get(badOwner1.id).equals(memberByte));
        
    User newcomer2 = controller.newUser("newcomer2");
    controller.addMember(ownerByte, newcomer2.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can add a newcomer not in chat to their chat",
        !accessMap.get(newcomer2.id).equals(memberByte));
    
    controller.addMember(ownerByte, badOwner2.id, conversation);
    accessMap = controller.getPermissionMap(conversation);
    
    assertFalse(
        "Check that a creator can add a banned member back to their chat",
        !accessMap.get(badOwner2.id).equals(memberByte));
    
  }
}