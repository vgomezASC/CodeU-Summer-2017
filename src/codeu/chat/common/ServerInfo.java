package codeu.chat.common;

import java.io.IOException;
<<<<<<< HEAD
import codeu.chat.util.Uuid;
public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";
  public final Uuid version;

=======

import codeu.chat.util.Uuid;
public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public final Uuid version;
>>>>>>> e0a24a36d31b1c14a086576bcf5588f941a822c9
  public ServerInfo(){
      Uuid temp = null;
      try {
        temp = Uuid.parse(SERVER_VERSION);
      } catch (IOException e) {
          System.out.println("ERROR: Cannot parse the server infomation. All variables in this instance will be null this time.");
          e.printStackTrace();
      }
      finally
      {
        this.version = temp;
      }
  }

  public ServerInfo(Uuid version) {
    this.version = version;
  }
}