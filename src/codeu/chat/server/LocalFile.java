package codeu.chat.server;

import java.io.File;
import java.io.IOException;
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

public class LocalFile
{
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

    public LocalFile (File file)
    {
        this.file = file;
        users = new LinkedHashSet<>();
        conversationHeaders= new LinkedHashSet<>();
        messages= new LinkedHashSet<>();
    }

    /**
     * Get a copy of users
     * 
     * @param   boolean Is the return value going to be saved? If true, the status should be updated.
     * @return  LinkedHashSet<User> Current users from this instance
     */
    public LinkedHashSet<User> getCopyOfUsers(boolean willBeSaved)
    {
        if(willBeSaved)
        {
            hasUserModified = false;
        }
        return new LinkedHashSet<>(users);
    }
    /**
     * Get a copy of conversations
     * 
     * @param   boolean Is the return value going to be saved? If true, the status should be updated.
     * @return  LinkedHashSet<ConversationHeader> Current conversations from this instance
     */
    public LinkedHashSet<ConversationHeader> getCopyOfConversationHeaders(boolean willBeSaved)
    {
        if(willBeSaved)
        {
            hasConversationModified = false;
        }
        return new LinkedHashSet<>(conversationHeaders);
    }
    /**
     * Get a copy of messages
     * 
     * @param   boolean Is the return value going to be saved? If true, the status should be updated.
     * @return  LinkedHashSet<Message> Current messages from this instance
     */
    public LinkedHashSet<Message> getCopyOfMessages(boolean willBeSaved)
    {
        if(willBeSaved)
        {
            hasMessageModified = false;
        }
        return new LinkedHashSet<>(messages);
    }
    /**
     * Add a new user to the instance
     * 
     * @param   User    New user
     */
    public void addUser(User user)
    {
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
     * Check if the User data has been modified.
     * If this instance is not initialized, it should always return false.
     * 
     * @return boolean Status of the User data
     */

    public boolean hasUserModified()
    {
        return hasUserModified;
    }

    /**
     * Check if the ConversationHeader data has been modified.
     * If this instance is not initialized, it should always return false.
     * 
     * @return boolean Status of the ConversationHeader data
     */
    public boolean hasConversationModified()
    {
        return hasConversationModified;
    }
    /**
     * Check if the Message data has been modified.
     * If this instance is not initialized, it should always return false.
     * 
     * @return boolean Status of the Message data
     */

    public boolean hasMessageModified()
    {
        return hasMessageModified;
    }
    /**
     * The initialization of this instance is complete.
     * Then all new data been added to this instance should mark the status as modified.
     */
    public void finishInitialization()
    {
        isInitialized = true;
    }
}