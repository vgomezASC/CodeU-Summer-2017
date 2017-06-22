package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;

public final class ServerInfo {
  // dunno if we need this but I wrote a serializer for ServerInfo
  public static final Serializer<ServerInfo> SERIALIZER = new Serializer<ServerInfo>() {

    @Override
    public void write(OutputStream out, ServerInfo value) throws IOException {

      Uuid.SERIALIZER.write(out, value.version);
    }

    @Override
    public ServerInfo read(InputStream in) throws IOException {

      ServerInfo info = new ServerInfo();
      return info;

    }
  };

  private final static String SERVER_VERSION = "1.0.0";
  public final Uuid version;
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