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

package codeu.chat.server;

import java.util.Comparator;
import java.util.HashMap;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.InterestSet;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;
import codeu.chat.util.store.StoreAccessor;

public final class Model implements AuthorityModel {

  private static final Comparator<Uuid> UUID_COMPARE = new Comparator<Uuid>() {

    @Override
    public int compare(Uuid a, Uuid b) {

      if (a == b) { return 0; }

      if (a == null && b != null) { return -1; }

      if (a != null && b == null) { return 1; }

      final int order = Integer.compare(a.id(), b.id());
      return order == 0 ? compare(a.root(), b.root()) : order;
    }
  };

  private static final Comparator<Time> TIME_COMPARE = new Comparator<Time>() {
    @Override
    public int compare(Time a, Time b) {
      return a.compareTo(b);
    }
  };
  
  private final static Logger.Log LOG = Logger.newLog(Model.class);
  
  private static final Comparator<String> STRING_COMPARE = String.CASE_INSENSITIVE_ORDER;

  private final Store<Uuid, User> userById = new Store<>(UUID_COMPARE);
  private final Store<Time, User> userByTime = new Store<>(TIME_COMPARE);
  private final Store<String, User> userByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, ConversationHeader> conversationById = new Store<>(UUID_COMPARE);
  private final Store<Time, ConversationHeader> conversationByTime = new Store<>(TIME_COMPARE);
  private final Store<String, ConversationHeader> conversationByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, ConversationPayload> conversationPayloadById = new Store<>(UUID_COMPARE);

  private final Store<Uuid, Message> messageById = new Store<>(UUID_COMPARE);
  private final Store<Time, Message> messageByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Message> messageByText = new Store<>(STRING_COMPARE);

  private HashMap<Uuid, InterestSet> interestMap = new HashMap<Uuid, InterestSet>();
  HashMap<String, HashMap<Uuid, Byte>> authority = new HashMap<String, HashMap<Uuid, Byte>>();
  
  public void add(User user) {
    userById.insert(user.id, user);
    userByTime.insert(user.creation, user);
    userByText.insert(user.name, user);
    interestMap.put(user.id, new InterestSet());
    LOG.info("NEW SIZE: "+interestMap.size());
  }

  public StoreAccessor<Uuid, User> userById() {
    return userById;
  }

  public StoreAccessor<Time, User> userByTime() {
    return userByTime;
  }

  public StoreAccessor<String, User> userByText() {
    return userByText;
  }

  public void add(ConversationHeader conversation) {
    conversationById.insert(conversation.id, conversation);
    conversationByTime.insert(conversation.creation, conversation);
    conversationByText.insert(conversation.title, conversation);
    conversationPayloadById.insert(conversation.id, new ConversationPayload(conversation.id));
    
    HashMap<Uuid, Byte> accessMap = new HashMap<Uuid, Byte>();
    byte creatorByte = 0b111;
    accessMap.put(conversation.owner, creatorByte);
    authority.put(conversation.title,accessMap);
  }

  public StoreAccessor<Uuid, ConversationHeader> conversationById() {
    return conversationById;
  }

  public StoreAccessor<Time, ConversationHeader> conversationByTime() {
    return conversationByTime;
  }

  public StoreAccessor<String, ConversationHeader> conversationByText() {
    return conversationByText;
  }

  public StoreAccessor<Uuid, ConversationPayload> conversationPayloadById() {
    return conversationPayloadById;
  }

  public void add(Message message) {
    messageById.insert(message.id, message);
    messageByTime.insert(message.creation, message);
    messageByText.insert(message.content, message);
  }

  public StoreAccessor<Uuid, Message> messageById() {
    return messageById;
  }

  public StoreAccessor<Time, Message> messageByTime() {
    return messageByTime;
  }

  public StoreAccessor<String, Message> messageByText() {
    return messageByText;
  }
  
  @Override
  public void changeAuthority(Uuid conversation, Uuid targetUser, byte authorityByte){
	StoreAccessor<Uuid, ConversationHeader> convos = this.conversationById();
	ConversationHeader chat = convos.first(conversation);
	HashMap<Uuid, Byte> accessMap = this.getPermissionMap(chat);
	accessMap.put(targetUser, authorityByte);
	authority.put(chat.title, accessMap);
  }
  
  @Override
  public boolean isMember(ConversationHeader conversation,Uuid targetUser){
    HashMap<Uuid, Byte> accessMap = this.getPermissionMap(conversation);
    if(!accessMap.containsKey(targetUser)){
  	  accessMap.put(targetUser, Controller.USER_TYPE_MEMBER);
  	  authority.put(conversation.title, accessMap);
  	  return true;
  	} else {
  	  byte user = accessMap.get(targetUser);
  	  if ((user | Controller.USER_TYPE_BANNED) == Controller.USER_TYPE_BANNED)
  	  	return false;
  	  return true;
    }
  }
   
  @Override
  public boolean isMember(Uuid conversation,Uuid targetUser){
	StoreAccessor<Uuid, ConversationHeader> convos = this.conversationById();
	ConversationHeader chat = convos.first(conversation);
	HashMap<Uuid, Byte> accessMap = this.getPermissionMap(chat);
	if(!accessMap.containsKey(targetUser)){
	  accessMap.put(targetUser, Controller.USER_TYPE_MEMBER);
	  authority.put(chat.title, accessMap);
	  return true;
	} else {
	  byte user = accessMap.get(targetUser);
	  if ((user | Controller.USER_TYPE_BANNED) == Controller.USER_TYPE_BANNED)
	  	return false;
	  return true;
    }
  }
   
  @Override
  public boolean isOwner(ConversationHeader conversation,Uuid targetUser){
	HashMap<Uuid, Byte> accessMap = this.getPermissionMap(conversation);
	if (!accessMap.containsKey(targetUser)){
	  this.isMember(conversation, targetUser);
	  return false;
	} else {
	  byte user = accessMap.get(targetUser);
	  byte expected = 0b010;
	  if((expected & user) == expected)
		return true; 
	  return false;
    }  
  }
  
  @Override
  public boolean isOwner(Uuid conversation,Uuid targetUser){
	StoreAccessor<Uuid, ConversationHeader> convos = this.conversationById();
	ConversationHeader chat = convos.first(conversation);
	HashMap<Uuid, Byte> accessMap = this.getPermissionMap(chat);
	if (!accessMap.containsKey(targetUser)){
	  this.isMember(conversation, targetUser);
	  return false;
	} else {
	  byte user = accessMap.get(targetUser);
	  byte expected = 0b010;
	  if((expected & user) == expected)
		return true; 
	  return false;
    }
  }
   
  @Override
  public boolean isCreator(ConversationHeader conversation,Uuid targetUser){
	HashMap<Uuid, Byte> accessMap = this.getPermissionMap(conversation);
	if (!accessMap.containsKey(targetUser)){
		  this.isMember(conversation, targetUser);
		  return false;
    } else {
	  byte user = accessMap.get(targetUser);
	  if((Controller.USER_TYPE_CREATOR & user) == Controller.USER_TYPE_CREATOR)
		return true;
      return false;
    }
  }
  
  @Override
  public boolean isCreator(Uuid conversation,Uuid targetUser){
	StoreAccessor<Uuid, ConversationHeader> convos = this.conversationById();
	ConversationHeader chat = convos.first(conversation);
	HashMap<Uuid, Byte> accessMap = this.getPermissionMap(chat);
	if (!accessMap.containsKey(targetUser)){
		  this.isMember(conversation, targetUser);
		  return false;
    } else {
	  byte user = accessMap.get(targetUser);
	  if((Controller.USER_TYPE_CREATOR & user) == Controller.USER_TYPE_CREATOR)
		return true;
      return false;
    }
  }
  
  public InterestSet getInterestSet(Uuid id){
    LOG.info(interestMap.get(id).toString());
    LOG.info("CURRENT: "+interestMap.size());
    return interestMap.get(id);
  }
  
  public void updateInterests(Uuid id, InterestSet intSet){
    LOG.info("BEFORE: "+interestMap.size());
    interestMap.put(id, intSet);
    LOG.info("AFTER: "+interestMap.size());
    LOG.info(interestMap.get(id).toString());
  }
  
  public HashMap<Uuid, Byte> getPermissionMap(ConversationHeader c){
	  return authority.get(c.title);
  }
  
}
