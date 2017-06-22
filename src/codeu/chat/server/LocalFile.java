package codeu.chat.server;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;
import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;

public class LocalFile
{
    private final static Logger.Log LOG = Logger.newLog(LocalFile.class);

    public static final String MESSAGE_FILE_NAME = "/msgDat.sav";
    public static final String USER_FILE_NAME = "/usrDat.sav";
    public static final String CONVERSATION_FILE_NAME = "/cvrsDat.sav";

    //Instance varibles for saving the current data of server.
    private final LinkedHashSet<User> users;
    private final LinkedHashSet<ConversationHeader> conversationHeaders;
    private final LinkedHashSet<Message> messages;

    private final File file;

    private boolean hasUserModified = false;//It indicates if there is a new data should be handled.
    private boolean hasMessageModified = false;
    private boolean hasConversationModified = false;

    //false: The instance is not initialized. All new data will not mark the status as modified.
    //true: All new data been added to this instance should mark the status as modified.
    private boolean isInitialized = false;

    private final Serializer<Collection<Message>> localMessages = Serializers.collection(Message.SERIALIZER);
    private final Serializer<Collection<ConversationHeader>> localConversationHeaders = Serializers.collection(ConversationHeader.SERIALIZER);
    private final Serializer<Collection<User>> localUsers = Serializers.collection(User.SERIALIZER);

    private final File userFile;
    private final File conversationFile;
    private final File messageFile;
    public LocalFile (File file)
    {
        this.file = file;
        users = new LinkedHashSet<>();
        conversationHeaders= new LinkedHashSet<>();
        messages= new LinkedHashSet<>();

        userFile = new File(file.getPath() + USER_FILE_NAME);
        conversationFile = new File(file.getPath() + CONVERSATION_FILE_NAME);
        messageFile = new File(file.getPath() + MESSAGE_FILE_NAME);
        try
        {
            if(!userFile.exists())
            {
                userFile.createNewFile();
            }      
            if(!conversationFile.exists())
            {
                conversationFile.createNewFile();
            }
            if(!messageFile.exists())
            {
                messageFile.createNewFile();
            }
        }
        catch(IOException exception)
        {
            LOG.error("Failed to create new file!");
            exception.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Get a copy of users
     * 
     * @return  LinkedHashSet<User> Current users from this instance
     */
    public LinkedHashSet<User> getCopyOfUsers()
    {
        hasUserModified = false;
        return new LinkedHashSet<>(users);
    }
    /**
     * Get a copy of conversations
     * 
     * @return  LinkedHashSet<ConversationHeader> Current conversations from this instance
     */
    public LinkedHashSet<ConversationHeader> getCopyOfConversationHeaders()
    {
        hasConversationModified = false;
        return new LinkedHashSet<>(conversationHeaders);
    }
    /**
     * Get a copy of messages
     * 
     * @return  LinkedHashSet<Message> Current messages from this instance
     */
    public LinkedHashSet<Message> getCopyOfMessages()
    {
        hasMessageModified = false;
        return new LinkedHashSet<>(messages);
    }
    /**
     * Add a new user to the instance
     * 
     * @param   User    New user
     */
    public void addUser(User user)
    {
        if(users.contains(user))
        {
            return;
        }
        users.add(user);
        if(isInitialized)
        {
            hasUserModified = true;
        }
    }
    /**
     * Add a new conversation to the instance.
     * 
     * @param ConversationHeader New conversation
     */
    public void addConversationHeader(ConversationHeader header)
    {
        if(conversationHeaders.contains(header))
        {
            return;
        }
        conversationHeaders.add(header);//If repetition happens, hasMofified should be false still.
        if(isInitialized)
        {
            hasConversationModified = true;
        }
    }
    /**
     * Add a new message to the instance.
     * 
     * @param Message New message
     */
    public void addMessage(Message message)
    {
       if(messages.contains(message))
       {
           return;
       }
       messages.add(message);//If repetition happens, hasMofified should be false still.
       if(isInitialized)
       {
            hasMessageModified = true;
       }
    }
    /**
     * Get current path.
     * 
     * @return String Path of this instance
     */
    public String getPath()
    {
        return file.getPath();
    }

    /**
     * The initialization of this instance is complete.
     * Then all new data been added to this instance should mark the status as modified.
     */
    public void finishInitialization()
    {
        isInitialized = true;
    }

    /**
   * Save user data
   * @throws IOException
   */
  private FileOutputStream saveUsers() throws IOException
  {
    FileOutputStream userStream = null;
    try
    {
      userStream = new FileOutputStream(userFile);
      localUsers.write(userStream, users);
    }
    catch(FileNotFoundException exception)
    {
      System.out.println("ERROR:Unacceptable file path");
      exception.printStackTrace();
      throw exception;
    }
    catch(IOException exception)
    {
      System.out.println("ERROR:Failed to get ConversationHeaderStream!");
      exception.printStackTrace();
      throw exception;
    }
    return userStream;
  }
  /**
   * Save conversation data
   * @throws IOException
   */
  private FileOutputStream saveConversationHeaders() throws IOException
  {
    FileOutputStream conversationStream = null;
    try
    {
      conversationStream = new FileOutputStream(conversationFile);
      localConversationHeaders.write(conversationStream, conversationHeaders);
    }
    catch (FileNotFoundException exception)
    {
      System.out.println("ERROR:Unacceptable file path");
      exception.printStackTrace();
      throw exception;
    }
    catch(IOException exception)
    {
      System.out.println("ERROR:Failed to get ConversationHeaderStream!");
      exception.printStackTrace();
      throw exception;
    }
    return conversationStream;
  }
  /**
   * Save message data
   * @throws IOException
   */
  private FileOutputStream saveMessages() throws IOException
  {
    FileOutputStream messageStream = null;
    try
    {
      messageStream = new FileOutputStream(messageFile);
      localMessages.write(messageStream, messages);
    }
    catch (FileNotFoundException exception)
    {
      System.out.println("ERROR:Unacceptable file path");
      exception.printStackTrace();
      throw exception;
    }
    catch (IOException exception)
    {
      System.out.println("ERROR:Failed to get ConversationHeaderStream!");
      exception.printStackTrace();
      throw exception;
    }
    return messageStream;
  }
  /**
   * Save all data
   * @throws IOException
   */
  public void saveData() throws IOException
  {
    try
    {
      if(hasConversationModified)
      {
        saveConversationHeaders();
        LOG.info("Conversation data Saved!");
        hasConversationModified = false;
      }
      if(hasMessageModified)
      {
        saveMessages();
        LOG.info("Message data Saved!");
        hasMessageModified = false;
      }
      if(hasUserModified)
      {
        saveUsers();
        LOG.info("User data Saved!");
        hasUserModified = false;
      }
    }
    catch(IOException exception)
    {
      System.out.println("ERROR:Failed to save data!");
      exception.printStackTrace();
      throw exception;
    }
  }
}