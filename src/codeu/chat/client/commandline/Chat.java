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

package codeu.chat.client.commandline;

import java.io.IOException;
import java.util.HashSet;
import java.util.Stack;

import java.util.ArrayList;
import java.util.List;

import codeu.chat.client.core.Context;
import codeu.chat.client.core.ConversationContext;
import codeu.chat.client.core.MessageContext;
import codeu.chat.client.core.UserContext;
import codeu.chat.common.Bookmark;
import codeu.chat.common.InterestSet;
import codeu.chat.common.ServerInfo;
import codeu.chat.common.User;
import codeu.chat.util.Sort;
import codeu.chat.util.Time;
import codeu.chat.util.Tokenizer;
import codeu.chat.util.Uuid;

public final class Chat {

  // PANELS
  //
  // We are going to use a stack of panels to track where in the application
  // we are. The command will always be routed to the panel at the top of the
  // stack. When a command wants to go to another panel, it will add a new
  // panel to the top of the stack. When a command wants to go to the previous
  // panel all it needs to do is pop the top panel.
  private final Stack<Panel> panels = new Stack<>();
  private Context context; // made global because more than one Panel type uses this.

  public Chat(Context context) {
  	this.context = context;
    this.panels.push(createRootPanel(context));
  }

  // HANDLE COMMAND
  //
  // Take a single line of input and parse a command from it. If the system
  // is willing to take another command, the function will return true. If
  // the system wants to exit, the function will return false.
  //
  public boolean handleCommand(String line) {

    final List<String> args = new ArrayList<>();
    final Tokenizer tokenizer = new Tokenizer(line);
    
    try {
      for (String token = tokenizer.next(); token != null; token = tokenizer.next()) {
        args.add(token);
      }
    } catch (IOException e){ 
      System.out.println("ERROR! IOException caught.");
      e.printStackTrace();
    }
    if (args.size() > 0){
    final String command = args.get(0);    
    args.remove(0);

    // Because "exit" and "back" are applicable to every panel, handle
    // those commands here to avoid having to implement them for each
    // panel.

    if ("exit".equals(command)) {
      // The user does not want to process any more commands
      return false;
    }

    // Do not allow the root panel to be removed.
    if ("back".equals(command) && panels.size() > 1) {
      panels.pop();
      return true;
    }

    if (panels.peek().handleCommand(command, args)) {
      // the command was handled
      return true;
    }

    // If we get to here it means that the command was not correctly handled
    // so we should let the user know. Still return true as we want to continue
    // processing future commands.
    System.out.println("ERROR: Unsupported command");
    return true;
    }
    return true;
  }

  // CREATE ROOT PANEL
  //
  // Create a panel for the root of the application. Root in this context means
  // the first panel and the only panel that should always be at the bottom of
  // the panels stack.
  //
  // The root panel is for commands that require no specific contextual information.
  // This is before a user has signed in. Most commands handled by the root panel
  // will be user selection focused.
  //
  private Panel createRootPanel(final Context context) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command to print a list of all commands and their description when
    // the user for "help" while on the root panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("ROOT MODE");
        System.out.println("  u-list");
        System.out.println("    List all users.");
        System.out.println("  u-add <name>");
        System.out.println("    Add a new user with the given name.");
        System.out.println("  u-sign-in <name>");
        System.out.println("    Sign in as the user with the given name.");
        System.out.println("  info");
        System.out.println("    Get session information.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");       
      }
    });

    // U-LIST (user list)
    //
    // Add a command to print all users registered on the server when the user
    // enters "u-list" while on the root panel.
    //
    panel.register("u-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (final UserContext user : context.allUsers()) {
          System.out.format(
              "USER %s (UUID:%s)\n",
              user.user.name,
              user.user.id);
        }
      }
    });

    // U-ADD (add user)
    //
    // Add a command to add and sign-in as a new user when the user enters
    // "u-add" while on the root panel.
    //
    panel.register("u-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
      	if (args.size()<1) {
      	  System.out.println("ERROR: Missing <username>");
      	} else {
          final String name = args.get(0);
          args.remove(0);
          if (name.length() > 0) {
            if (findUser(name) != null) {
              System.out.println("ERROR: Username already taken");
            } else if (context.create(name) == null) {
              System.out.println("ERROR: Failed to create new user");
            }
          } else {
            System.out.println("ERROR: Missing <username>");
          }
        }
      }
    });

    // U-SIGN-IN (sign in user)
    //
    // Add a command to sign-in as a user when the user enters "u-sign-in"
    // while on the root panel.
    //
    panel.register("u-sign-in", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        if (args.size()<1) {
      	  System.out.println("ERROR: Missing <username>");
      	} else {
          final String name = args.get(0);
          args.remove(0);
          if (name.length() > 0) {
            final UserContext user = findUserLogin(name);
            if (user == null) {
              System.out.format("ERROR: Failed to sign in as '%s'\n", name);
            } else {
              panels.push(createUserPanel(user));
            }
          } else {
            System.out.println("ERROR: Missing <username>");
          }
        }
      }

      // Find the first user with the given name and return a user context
      // for that user. If no user is found, the function will return null.
      private UserContext findUserLogin(String name) {
        for (final UserContext user : context.allUsers()) {
          if (user.user.name.equals(name)) {
            return user;
          }
        }
        return null;
      }
    });
    
    // info (Server info)
    //
    // Get some infomation from server; it should be version info currently
    //
    panel.register("info", new Panel.Command() {
        @Override
        public void invoke(List<String> args) {
          final ServerInfo info = context.getInfo();
          if (info == null) {
            // Communicate error to user - the server did not send us a valid
            // info object.
            new IOException("ERROR: ServerInfo cannot be read.").printStackTrace();
          } else {
            //Print server info
            System.out.println("Version:" + info.version);
            System.out.println("UpTime:" + info.upTime()); 
          }
        }
    });
    
    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }

  private Panel createUserPanel(final UserContext user) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command that will print a list of all commands and their
    // descriptions when the user enters "help" while on the user panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println("  c-list");
        System.out.println("    List all conversations that the current user can interact with.");
        System.out.println("  c-add <title>");
        System.out.println("    Add a new conversation with the given title and join it as the current user.");
        System.out.println("  c-join <title>");
        System.out.println("    Join the conversation as the current user.");
        System.out.println("  status-update");
        System.out.println("    Get updates on the interests you're following.");
        System.out.println("  info");
        System.out.println("    Display all info for the current user");
        System.out.println("  back");
        System.out.println("    Go back to ROOT MODE.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");
      }
    });

    // C-LIST (list conversations)
    //
    // Add a command that will print all conversations when the user enters
    // "c-list" while on the user panel.
    //
    panel.register("c-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (final ConversationContext conversation : user.conversations()) {
          System.out.format(
              "CONVERSATION %s (UUID:%s)\n",
              conversation.conversation.title,
              conversation.conversation.id);
        }
      }
    });

    // C-ADD (add conversation)
    //
    // Add a command that will create and join a new conversation when the user
    // enters "c-add" while on the user panel.
    //
    panel.register("c-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        if (args.size()<1) {
      	  System.out.println("ERROR: Missing <title>");
      	} else {
          final String name = args.get(0);
          args.remove(0);
          if (name.length() > 0) {
            if (findConversation(name, user) != null){
              System.out.println("ERROR: Chat name already taken");
            } else {
              final ConversationContext conversation = user.start(name);
              if (conversation == null) {
                System.out.println("ERROR: Failed to create new conversation");
              } else {
                panels.push(createConversationPanel(conversation));
              }
            }
          } else {
            System.out.println("ERROR: Missing <title>");
          }
        }
      }
    });

    // C-JOIN (join conversation)
    //
    // Add a command that will joing a conversation when the user enters
    // "c-join" while on the user panel.
    //
    panel.register("c-join", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        if (args.size()<1) {
      	  System.out.println("ERROR: Missing <title>");
      	} else {
          final String name = args.get(0);
          args.remove(0);
          if (name.length() > 0) {
            final ConversationContext conversation = find(name);
            if (conversation == null) {
              System.out.format("ERROR: No conversation with name '%s'\n", name);
            } else {
              panels.push(createConversationPanel(conversation));
            }
          } else {
            System.out.println("ERROR: Missing <title>");
          }
        }
      }

      // Find the first conversation with the given name and return its context.
      // If no conversation has the given name, this will return null.
      private ConversationContext find(String title) {
        for (final ConversationContext conversation : user.conversations()) {
          if (title.equals(conversation.conversation.title)) {
            return conversation;
          }
        }
        return null;
      }
    });
	
	// STATUS-UPDATE (retrieve new interesting messages)
	//
	// While on the user panel, add a command to retrieve new messages from users and
	// conversations the current user is following. This will generate an interest panel.
	panel.register("status-update", new Panel.Command(){
	  @Override
	  public void invoke(List<String> args){
	    panels.push(createInterestPanel(user));
	  }
	});
	
    // INFO
    //
    // Add a command that will print info about the current context when the
    // user enters "info" while on the user panel.
    //
    panel.register("info", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("User Info:");
        System.out.format("  Name : %s\n", user.user.name);
        System.out.format("  Id   : UUID:%s\n", user.user.id);
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }
  
  private Panel createConversationPanel(final ConversationContext conversation) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command that will print all the commands and their descriptions
    // when the user enters "help" while on the conversation panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println("  m-list");
        System.out.println("    List all messages in the current conversation.");
        System.out.println("  m-add <message>");
        System.out.println("    Add a new message to the current conversation as the current user.");
        System.out.println("  m-auth <username> <authority>");
        System.out.println("    Change user rank. o: Owner m: Member b: Banned");
        System.out.println("    Only the creator and owners can do this!");
        System.out.println("  info");
        System.out.println("    Display all info about the current conversation.");
        System.out.println("  back");
        System.out.println("    Go back to USER MODE.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");
      }
    });

    // M-LIST (list messages)
    //
    // Add a command to print all messages in the current conversation when the
    // user enters "m-list" while on the conversation panel.
    //
    panel.register("m-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("--- start of conversation ---");
        for (MessageContext message = conversation.firstMessage();
                            message != null;
                            message = message.next()) {
          System.out.println();
          System.out.format("USER : %s\n", findUsername(message.message.author));
          System.out.format("SENT : %s\n", message.message.creation);
          System.out.println();
          System.out.println(message.message.content);
          System.out.println();
        }
        System.out.println("---  end of conversation  ---");
      }
      
    });
    

    // M-ADD (add message)
    //
    // Add a command to add a new message to the current conversation when the
    // user enters "m-add" while on the conversation panel.
    //
    panel.register("m-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {      
        if (args.size()<1) {
      	  System.out.println("ERROR: Messages must contain text");
      	} else {
      	  String message = args.get(0);
          args.remove(0);
      	  for (String s: args){
            message += " "+s;
          }
        
          if (message.length() > 0) {
          	conversation.add(message);
          } else {
            System.out.println("ERROR: Messages must contain text");
          }
        }
      }
      
    });
    
    panel.register("m-auth", new Panel.Command(){
      @Override
      public void invoke(List<String> args){
    	if (args.size() < 2){
    	  System.out.println("ERROR: Command doesn't follow the format. Please use 'help' for more information.");
    	} else {
    	  String user = args.get(0);
    	  String para = args.get(1);
    	  if(findUser(user) == null){
    		System.out.println("ERROR: No such user.");
    	  } else if(!para.equals("o") && !para.equals("m") && !para.equals("b")){
    		System.out.println("ERROR: Parameter '" + para + "' is unacceptable! Type 'help' for more info.");
    	  } else {
    		conversation.changeAuthority(findUser(user).id, para);
    	  }
    	}
      }
    });
      
    // INFO
    //
    // Add a command to print info about the current conversation when the user
    // enters "info" while on the conversation panel.
    //
    panel.register("info", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("Conversation Info:");
        System.out.format("  Title : %s\n", conversation.conversation.title);
        System.out.format("  Id    : UUID:%s\n", conversation.conversation.id);
        System.out.format("  Owner : %s\n", conversation.conversation.owner);
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }
  
  private Panel createInterestPanel(final UserContext user) {

    final Panel panel = new Panel();
    
    InterestSet interests = context.getInterestSet(user.user.id);
    int updates = 0;
    
    ArrayList<Bookmark> display = new ArrayList<Bookmark>();
    ArrayList<Time> unsorted = new ArrayList<Time>();
    
    for (User u : interests.users){
      HashSet<ConversationContext> newSet = conversationsOfUser(u,user);
      
      for(ConversationContext c : newSet){
        boolean isCopy = false;
        for(Bookmark b : interests.bookmarks){
          if(!isCopy){
            if(b.conversation.id.equals(c.conversation.id)){
          	  isCopy = true;
            }
          }
        }
        
        if(!isCopy){
          unsorted.add(c.conversation.creation);
          display.add(new Bookmark(c));
          interests.bookmarks.add(new Bookmark(c));
        }
          
        }
       }
       
    if (unsorted.size() > 0){
	  Sort sorter = new Sort();
      display = sorter.sort(unsorted, display);
    }
       
       
    for(Bookmark b : display){
    	ConversationContext c = findConversation(b.conversation.title, user);
        System.out.println("--- new conversation "+c.conversation.title+" from "+findUsername(c.conversation.owner)+" ---");
          if(c.firstMessage() != null){
            updates += displayMessages(c.firstMessage(), c,"");
              
          } else {
            System.out.println("---  go start that conversation!  ---\n");
          }
        } 
    
    ArrayList<Bookmark> mainDisplay = new ArrayList<Bookmark>();
    unsorted = new ArrayList<Time>();
    System.out.println("updates:");
		
    for (Bookmark b : interests.bookmarks){
      ConversationContext conversation = findConversation(b.conversation.title, user);
      if (!hasConversation(b, display) && conversation.lastMessage() != null && (b.bookmark == null || !conversation.lastMessage().message.equals(b.bookmark))){
	    unsorted.add(conversation.lastMessage().message.creation);
	    mainDisplay.add(b);
      }
    }  
	
    if (unsorted.size() > 0){
      Sort sorter = new Sort();
      mainDisplay = sorter.sort(unsorted, mainDisplay);
    }
	
    for (Bookmark b : mainDisplay){
      ConversationContext conversation = findConversation(b.conversation.title, user);
      MessageContext msg;

      // If the bookmark was taken before the chat was started, this display loop will 
      // replay the whole chat. Otherwise, the first message printed is the one directly
      // after the bookmark.
      if (b.bookmark == null) {
        msg = conversation.firstMessage();
      } else {
        msg = conversation.getMessage(b.bookmark.id).next();
      }
      
      String leading = "--- new from "+conversation.conversation.title+" ---";
      updates += displayMessages(msg, conversation, leading);
        
      b.bookmark = conversation.lastMessage().message;
	   
	}
	System.out.println(updates+" new messages.");
    context.updateInterests(user.user.id, interests);
    
    // HELP
    //
    // Add a command that will print a list of all commands and their
    // descriptions when the user enters "help" while on the user panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println("  s-add <title>");
        System.out.println("    Add an interest.");
        System.out.println("  s-del <title>");
        System.out.println("    Delete an interest.");
        System.out.println("  s-list");
        System.out.println("    View current interests.");
        System.out.println("  back");
        System.out.println("    Go back to ROOT MODE.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");
      }
    });
    
    // S-ADD (add interest)
    //
    // Add a command that will add a certain interest to their InterestSet when the 
    // user enters "s-add" while on the user panel.
    //
    panel.register("s-add", new Panel.Command(){
      @Override
      public void invoke(List<String> args) {
        if (args.size()<1) {
      	  System.out.println("ERROR: No user or chat given.");
      	} else {
      	  InterestSet interests = context.getInterestSet(user.user.id);
          User userResult = findUser(args.get(0));
      	  ConversationContext chatResult = findConversation(args.get(0));
      	  if(chatResult != null){
            if(!hasConversation(args.get(0), interests))   	  
      	      interests.bookmarks.add(new Bookmark(chatResult));
            context.updateInterests(user.user.id,interests);            
          } else if(userResult != null){
          	if (!hasUser(args.get(0), interests))
          	  interests.users.add(userResult);
          	
          	// is there an O(faster) way of doing this? less comparisons?
          	HashSet<ConversationContext> addSet = conversationsOfUser(userResult, user);
          	for(ConversationContext c : addSet){
          	  boolean isCopy = false;
          	  for(Bookmark b : interests.bookmarks){
          	    if(b.conversation.title.equals(c.conversation.title))
          	      isCopy = true;
          	  }
          	  if(!isCopy)
          	  interests.bookmarks.add(new Bookmark(c));
          	} 
          	context.updateInterests(user.user.id,interests);
          }else {
            System.out.println("ERROR: No valid user or chat given.");
          }
        
        }
      }
      
      private boolean hasConversation(String name, InterestSet intSet){
        for (Bookmark b : intSet.bookmarks){
          if (b.conversation.title.equals(name))
            return true;
        }
        
        return false;
      }
      
      private boolean hasUser(String name, InterestSet intSet){
        for (User u : intSet.users){
          if (u.name.equals(name))
            return true;
        }
        
        return false;
      }
      
      private ConversationContext findConversation(String name) {
        for (final ConversationContext conversation : user.conversations()) {
          if (conversation.conversation.title.equals(name)) {
            return conversation;
          }
        }
        return null;
      }
      
    });
    
    // S-DEL (delete interest)
    //
    // Add a command that will delete a certain interest from their InterestSet when the 
    // user enters "s-del" while on the user panel.
    //
    panel.register("s-del", new Panel.Command(){
      @Override
      public void invoke(List<String> args) {
        if (args.size()<1) {
      	  System.out.println("ERROR: No user or chat given.");
      	} else {
      	  InterestSet interests = context.getInterestSet(user.user.id);
      	  User userResult = findUser(args.get(0));
      	  ConversationContext chatResult = findConversation(args.get(0));
      	  if(chatResult != null){
      	    if(findBookmark(args.get(0), interests) != null)
      	      interests.bookmarks.remove(findBookmark(args.get(0), interests));
            context.updateInterests(user.user.id,interests);
          } else if(userResult != null){
          	if (hasUser(args.get(0), interests) != null)
          	  interests.users.remove(hasUser(args.get(0), interests));
          	
          	// is there an O(faster) way to do this?
          	HashSet<Bookmark> trashCan = conversationsOfUser(userResult);
          	for (Bookmark trash : trashCan){
          	  Bookmark scrap = null;
          	  for(Bookmark b : interests.bookmarks){
          	    if(b.conversation.title.equals(trash.conversation.title))
          	      scrap = b;
          	  }
          	  if(scrap != null){
          	    interests.bookmarks.remove(scrap);
          	  }
          	}
            context.updateInterests(user.user.id,interests);
          } else {
            System.out.println("ERROR: No valid user or chat given.");
          }
        
        }
      }
      
      private User hasUser(String name, InterestSet intSet){
        for (User u : intSet.users){
          if (u.name.equals(name))
            return u;
        }
        
        return null;
      }
      
      private Bookmark findBookmark(String name, InterestSet intSet){
        for (Bookmark b : intSet.bookmarks) {
          if (b.conversation.title.equals(name)) {
            return b;
          }
        }
        return null;
      }
      
      private ConversationContext findConversation(String name) {
        for (final ConversationContext conversation : user.conversations()) {
          if (conversation.conversation.title.equals(name)) {
            return conversation;
          }
        }
        return null;
      }
      
      private HashSet<Bookmark> conversationsOfUser(User trash){
        HashSet<Bookmark> trashCan = new HashSet<Bookmark>(50);
        InterestSet interests = context.getInterestSet(user.user.id);
        for (Bookmark b : interests.bookmarks) {
          boolean detected = false;
          if (b.bookmark != null && b.bookmark.author.equals(trash.id)) {
            trashCan.add(b);
            detected = true;
          }
          if (!detected && b.first != null){
            ConversationContext conversation = findConversation(b.conversation.title);
            MessageContext msg = conversation.getMessage(b.bookmark.id);
            for (MessageContext message = msg.next();
                           message != null;
                           message = message.next()){
              if(!detected && msg.message.author.equals(trash.id)){
                trashCan.add(b);
                detected = true;
              }
            }
          }
        }
        return trashCan;
      }
      
    });
    
    //
    // S-VIEW
    //    
    // Prints a list of the user's interests.
    panel.register("s-list", new Panel.Command(){
      @Override
      public void invoke(List<String> args) {
        InterestSet interestSet = context.getInterestSet(user.user.id);
        System.out.println(interestSet.toString());
      }
    });
    
      return panel;
  }
  
private boolean hasConversation(Bookmark b, ArrayList<Bookmark> display){
  for (Bookmark viewed : display){
    if (viewed.conversation.title.equals(b.conversation.title))
      return true;
  }
  return false;
}

private HashSet<ConversationContext> conversationsOfUser(User friend, UserContext self){
        HashSet<ConversationContext> resultSet = new HashSet<ConversationContext>(50);
        for (final ConversationContext conversation : self.conversations()) {
          boolean detected = false;
          if (conversation.conversation.owner.equals(friend.id)) {
            resultSet.add(conversation);
            detected = true;
          }
          if (!detected && conversation.firstMessage() != null){
            for (MessageContext message = conversation.firstMessage();
                     			message != null;
                     			message = message.next()){
            
              if(!detected && message.message.author.equals(friend.id)){
                resultSet.add(conversation);
                detected = true;
              }
            }
          }
        }
        return resultSet;
      }

private int displayMessages(MessageContext msg, ConversationContext conversation, String leading) {
       int updates = 0;
       for (MessageContext message = msg;
                           message != null;
                           message = message.next()) {
         
         if(updates == 0)
           System.out.println(leading);
         System.out.println();
         System.out.format("USER : %s\n", findUsername(message.message.author));
         System.out.format("SENT : %s\n", message.message.creation);
         System.out.println();
         System.out.println(message.message.content);
         System.out.println();
         updates++;
       }
       if (updates > 0)
         System.out.println("---  end of conversation  ---\n");
      
       return updates;
}

private ConversationContext findConversation(String name, UserContext user) {
        for (final ConversationContext conversation : user.conversations()) {
          if (conversation.conversation.title.equals(name)) {
            return conversation;
          }
        }
        return null;
      }

private User findUser(String name) {
        for (final UserContext user : context.allUsers()) {
          if (user.user.name.equals(name)) {
            return user.user;
          }
        }
        return null;
      }
      
private String findUsername(Uuid author) {
        for (final UserContext user : context.allUsers()) {
          if (user.user.id.equals(author)) {
            return user.user.name;
          }
        }
        return "Anonymous";
      }
 }
