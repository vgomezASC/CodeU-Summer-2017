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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.server.LocalFile;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  private final Serializer<Collection<Message>> localMessages = Serializers.collection(Message.SERIALIZER);
  private final Serializer<Collection<ConversationHeader>> localConversationHeaders = Serializers.collection(ConversationHeader.SERIALIZER);
  private final Serializer<Collection<User>> localUsers = Serializers.collection(User.SERIALIZER);

  private final LocalFile localFile;

  private final File userFile;
  private final File conversationFile;
  private final File messageFile;

  private boolean isInitialized = false;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
    this.localFile = new LocalFile(new File("."));
    this.userFile = new File(localFile.getPath() + LocalFile.USER_FILE_NAME);
    this.conversationFile = new File(localFile.getPath() + LocalFile.CONVERSATION_FILE_NAME);
    this.messageFile = new File(localFile.getPath() + LocalFile.MESSAGE_FILE_NAME);
  }
  //New constructor, which can get the local file information.
  public Controller(Uuid serverId, Model model,LocalFile localFile) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
    
    this.localFile = localFile;//The path is assigned by server.

    userFile = new File(localFile.getPath() + LocalFile.USER_FILE_NAME);
    conversationFile = new File(localFile.getPath() + LocalFile.CONVERSATION_FILE_NAME);
    messageFile = new File(localFile.getPath() + LocalFile.MESSAGE_FILE_NAME);
    try
    {
      FileInputStream userInputStream = new FileInputStream(userFile);
      if(userInputStream.available() > 0)
      {
       Collection<User> userData = localUsers.read(userInputStream);
       for (User item : userData)
       {
         this.newUser(item.id, item.name, item.creation);
         localFile.addUser(item);
       }
      }
      
      FileInputStream conversationInputStream = new FileInputStream(conversationFile);
      if(conversationInputStream.available() > 0)
      {
        Collection<ConversationHeader> conversationData =  localConversationHeaders.read(conversationInputStream);
        for (ConversationHeader item : conversationData)
        {
          this.newConversation(item.id, item.title, item.owner, item.creation);
          localFile.addConversationHeader(item);
        }
      }

      FileInputStream messageInputStream = new FileInputStream(messageFile);
      if(messageInputStream.available() > 0)
      {
          Collection<Message> messageData = localMessages.read(messageInputStream);
          for(Message item : messageData)
          {
            this.newMessage(item.id, item.author, item.conversation, item.content, item.creation);
            localFile.addMessage(item);
          }
      } 
    }
    catch (IOException exception)
    {
      System.out.println("ERROR: Failed to read local data!");
      exception.printStackTrace();
      throw new RuntimeException("ERROR: Program will be terminated!"); 
    }
    localFile.finishInitialization();
    isInitialized = true;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final ConversationPayload foundConversation = model.conversationPayloadById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body,conversation);
      model.add(message);
      if(isInitialized)
      {
        localFile.addMessage(message);
        LOG.info("Message added: %s", message.id);
      }
      else
      {
        //If it is initializing, messages should be read from local file not added a new record.
        LOG.info("Message read from local file: %s", message.id);
      }

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime);
      model.add(user);
      if(isInitialized)
      {
        localFile.addUser(user);
        LOG.info(
            "newUser success (user.id=%s user.name=%s user.time=%s)",
            id,
            name,
            creationTime);
      }
      else
      {
        //If it is initializing, users should be read from local file not added a new record.
        LOG.info(
            "User is read from local file successfully. (user.id=%s user.name=%s user.time=%s)",
            id,
            name,
            creationTime);
      }

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  @Override
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title);
      model.add(conversation);
      if(isInitialized)
      {
        localFile.addConversationHeader(conversation);
        LOG.info("Conversation added: " + id);
      }
      else
      {
        //If it is initializing, conversations should be read from local file not added a new record.
        LOG.info("Conversation read from local file", id);
      }
    }

    return conversation;
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }
}
