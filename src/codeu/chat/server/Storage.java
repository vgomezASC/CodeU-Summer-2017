package codeu.chat.storage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

import java.util.ArrayList;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;

public class Storage implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final ArrayList<User> users;
    private final ArrayList<ConversationHeader> conversationHeaders;
    private final ArrayList<Message> messages;

    private final File file;

    private static final Logger.Log LOG = Logger.newLog(Storage.class);
    public Storage(File file)
    {
        this.file = file;
        Storage newClass;
        ArrayList<User>tempUsers = new ArrayList<>();
        ArrayList<ConversationHeader>tempConversationHeaders= new ArrayList<>();
        ArrayList<Message>tempMessages= new ArrayList<>();
        try
        {
            FileInputStream fileInputStream = new FileInputStream(file);
		    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

		    newClass = (Storage)objectInputStream.readObject();
            tempUsers = newClass.getCopyOfUsers();
            tempConversationHeaders = newClass.getCopyOfConversationHeaders();
            tempMessages = newClass.getCopyOfMessages();

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
            users = tempUsers;
            conversationHeaders = tempConversationHeaders;
            messages = tempMessages;
        }
    }

    public void store() throws IOException
    {
        if(!file.exists())
        {
            file.createNewFile();
        }
       FileOutputStream fileOutputStream = new FileOutputStream(file);
	   ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

       objectOutputStream.writeObject(this); 
       LOG.info("Data saved!");

	   fileOutputStream.close();
       objectOutputStream.close();
    }

    public ArrayList<User> getCopyOfUsers()
    {
        return new ArrayList<>(users);
    }
    public ArrayList<ConversationHeader> getCopyOfConversationHeaders()
    {
        return new ArrayList<>(conversationHeaders);
    }
    public ArrayList<Message> getCopyOfMessages()
    {
        return new ArrayList<>(messages);
    }

    public void addUser(User user)
    {
        users.add(user);
    }
    public void addConversationHeader(ConversationHeader header)
    {
        conversationHeaders.add(header);
    }
    public void addMessage(Message message)
    {
        messages.add(message);
    }
}