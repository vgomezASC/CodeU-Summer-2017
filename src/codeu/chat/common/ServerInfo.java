package codeu.chat.common;

import java.io.IOException;

import codeu.chat.util.Uuid;
public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public final Uuid version;
  public ServerInfo() throws IOException {
      Uuid temp;
      try {
        temp = Uuid.parse(SERVER_VERSION);
      } catch (IOException e) {
          String line1 = "There is a fatal error when I parse version data, please contact Yuhang Liao to fix that!";
          String line2 = "Also don't forget tell the careless Yuhang Liao this exception happed in Line 15 in ServerInfo.java'";
          throw new IOException(line1 + "\n" + line2, e);
      }
      this.version = temp;
  }

  public ServerInfo(Uuid version) {
    this.version = version;
  }
}