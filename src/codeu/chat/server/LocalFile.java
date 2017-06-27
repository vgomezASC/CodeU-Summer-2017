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

        try(FileInputStream userInputStream = new FileInputStream(userFile);
            FileInputStream conversationInputStream = new FileInputStream(conversationFile);
            FileInputStream messageInputStream = new FileInputStream(messageFile);)
        {
            if(userInputStream.available() > 0)
            {
                Collection<User> userData = localUsers.read(userInputStream);
                for (User item : userData)
                {
                    users.add(item);
                }
            }
            
            if(conversationInputStream.available() > 0)
            {
                Collection<ConversationHeader> conversationData =  localConversationHeaders.read(conversationInputStream);
                for (ConversationHeader item : conversationData)
                {
                    conversationHeaders.add(item);
                }
            }

            if(messageInputStream.available() > 0)
            {
                Collection<Message> messageData = localMessages.read(messageInputStream);
                for(Message item : messageData)
                {
                    messages.add(item);
                }
            } 
        }
        catch (IOException exception)
        {
        System.out.println("ERROR: Failed to read local data!");
        exception.printStackTrace();
        throw new RuntimeException("ERROR: Program will be terminated!"); 
        }
    }

    /**
     * Get users
     * 
     * @return  LinkedHashSet<User> Current users from this instance
     */
    public LinkedHashSet<User> getUsers()
    {
        return new LinkedHashSet<User>(users);
    }
    /**
     * Get conversations
     * 
     * @return  LinkedHashSet<ConversationHeader> Current conversations from this instance
     */
    public LinkedHashSet<ConversationHeader> getConversationHeaders()
    {
        return new LinkedHashSet<ConversationHeader>(conversationHeaders);
    }
    /**
     * Get messages
     * 
     * @return  LinkedHashSet<Message> Current messages from this instance
     */
    public LinkedHashSet<Message> getMessages()
    {
        return new LinkedHashSet<Message>(messages);
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
        hasUserModified = true;
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
        hasConversationModified = true;
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
       hasMessageModified = true;
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
   * Save user data
   * @throws IOException
   */
  private void saveUsers() throws IOException
  {
    try(FileOutputStream userStream = new FileOutputStream(userFile))
    {
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
  }
  /**
   * Save conversation data
   * @throws IOException
   */
  private void saveConversationHeaders() throws IOException
  {
    try(FileOutputStream conversationStream = new FileOutputStream(conversationFile))
    {
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
  }
  /**
   * Save message data
   * @throws IOException
   */
  private void saveMessages() throws IOException
  {
    try(FileOutputStream messageStream = new FileOutputStream(messageFile))
    {
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