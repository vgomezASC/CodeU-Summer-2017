package codeu.chat.common;

import java.io.IOException;

import codeu.chat.util.Uuid;
public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public final Uuid version;
  public ServerInfo() {
      Uuid temp;
      try {
        temp = Uuid.parse(SERVER_VERSION);
      } catch (IOException e) {
          temp = new Uuid(0);
          System.out.println("There is a fatal error when I parse version data, please contact Yuhang Liao to fix that!");
          System.out.println("Also don't forget tell the careless Yuhang Liao this exception happed in Line 15 in ServerInfo.java'");
      }
      this.version = temp;
  }

  public ServerInfo(Uuid version) {
    this.version = version;
  }
}