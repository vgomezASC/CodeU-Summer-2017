package codeu.chat.common;

import java.io.IOException;

import codeu.chat.util.Uuid;
public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public final Uuid version;
  public ServerInfo(){
      Uuid temp;
      try {
        temp = Uuid.parse(SERVER_VERSION);
        this.version = temp;
      } catch (IOException e) {
          System.out.println("There is a fatal error when I parse version data, please contact Yuhang Liao to fix that!");

          System.out.println("Also don't forget tell the careless Yuhang Liao this exception happed in Line 15 in ServerInfo.java'");

          System.out.println("All server info will be null this time.");
          this.version = null;
      }
  }

  public ServerInfo(Uuid version) {
    this.version = version;
  }
}