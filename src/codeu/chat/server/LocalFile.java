package codeu.chat.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.HashMap;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Uuid;

public class LocalFile implements Serializable
{
    private static final long serialVersionUID = 1L;
    //Instance varibles for saving the current data of server.
    private final HashMap<Uuid,User> users;
    private final HashMap<Uuid,ConversationHeader> conversationHeaders;
    private final HashMap<Uuid,Message> messages;

    private final File file;

    private static final Logger.Log LOG = Logger.newLog(LocalFile.class);

    private boolean hasModified = false;//It indicates if there is a new data should be handled.
    public LocalFile(File file)
    {
        this.file = file;
        LocalFile newClass;
        HashMap<Uuid,User>tempUsers = new HashMap<>();
        HashMap<Uuid,ConversationHeader>tempConversationHeaders= new HashMap<>();
        HashMap<Uuid,Message>tempMessages= new HashMap<>();
        try
        {
            //Deserialization
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
		    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            //Read the information to current instance
		    newClass = (LocalFile)objectInputStream.readObject();
            tempUsers = newClass.users;
            tempConversationHeaders = newClass.conversationHeaders;
            tempMessages = newClass.messages;
            hasModified = false;

            fileInputStream.close();
            objectInputStream.close();
        }
        catch(Exception exception)
        {
            System.out.println("ERROR:Unable to read data");
            exception.printStackTrace();
        }
        finally
        {
            //Write temporary information to the instance varibles
            users = tempUsers;
            conversationHeaders = tempConversationHeaders;
            messages = tempMessages;
        }
    }
    /**
     * Save the data into local file.
     * 
     * @exception   Failed to save data.
     */
    public void saveData() throws IOException
    {
        if(!hasModified)//Only save the data when the data is updated.
        {
            return;
        }
        hasModified = false;
        if(!file.exists())
        {
            file.createNewFile();
        }
        //Serialization
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileOutputStream fileOutputStream = new FileOutputStream(randomAccessFile.getFD());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(this); 

        LOG.info("Data saved!");

        fileOutputStream.close();
        objectOutputStream.close();
    }
    /**
     * Get a copy of users
     * 
     * @return  ArrayList<User> Current users from this instance
     */
    public ArrayList<User> getCopyOfUsers()
    {
        return new ArrayList<>(users.values());
    }
    /**
     * Get a copy of conversations
     * 
     * @return  ArrayList<ConversationHeader> Current conversations from this instance
     */
    public ArrayList<ConversationHeader> getCopyOfConversationHeaders()
    {
        return new ArrayList<>(conversationHeaders.values());
    }
    /**
     * Get a copy of messages
     * 
     * @return  ArrayList<Message> Current messages from this instance
     */
    public ArrayList<Message> getCopyOfMessages()
    {
        return new ArrayList<>(messages.values());
    }
    /**
     * Add a new user to the instance
     * 
     * @param   User    New user
     */
    public void addUser(User user)
    {
        if(users.containsKey(user.id))//If repetition happens, hasMofified should be false still.
        {
            return;
        }
        users.put(user.id, user);
        hasModified = true;
    }
    /**
     * Add a new conversation to the instance.
     * 
     * @param ConversationHeader New conversation
     */
    public void addConversationHeader(ConversationHeader header)
    {
        if(conversationHeaders.containsKey(header.id))
        {
            return;
        }
        conversationHeaders.put(header.id, header);//If repetition happens, hasMofified should be false still.
        hasModified = true;
    }
    /**
     * Add a new message to the instance.
     * 
     * @param Message New message
     */
    public void addMessage(Message message)
    {
        if(messages.containsKey(message.id))
        {
            return;
        }
        messages.put(message.id, message);//If repetition happens, hasMofified should be false still.
        hasModified = true;
    }
}