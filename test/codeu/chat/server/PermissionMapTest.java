/**
 * Tests the HashMap<ConversationHeader, HashMap<Uuid, byte>> way of 
 * storing user permissions in Model. 
 * 
 * If you're seeing this, this is a tester class exploring storing
 * an instantial set of user permissions. As of right now it's un-
 * finished, but if anyone wants to trade for/take this task, or work
 * with me on it, please let me know! ~ Sarah Abowitz 
 * 
 * @author (Sarah Abowitz) 
 * @version (CA V1.0.4 | 7.15.17)
 */

package codeu.chat.server;

import java.util.HashMap;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public class PermissionMapTest {
	
	private Model model;
	private BasicController controller;

	@Before
	public void doBefore() {
	  model = new Model();
	  controller = new Controller(Uuid.NULL, model);
	}
	
	@Test
	public void mainPermissionMapAccessTest(){
	  User p1 = controller.newUser("p1");
	  ConversationHeader chatA = controller.newConversation("chatA", p1.id);
	  
	  HashMap<Uuid, Byte> accessMap = model.getPermissionMap(chatA);

	  assertFalse(
		  "Check that the correct map was accessed",
		  !accessMap.containsKey(p1.id));
	}

}

