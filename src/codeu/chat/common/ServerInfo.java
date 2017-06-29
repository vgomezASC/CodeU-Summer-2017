package codeu.chat.common;


import java.io.IOException;
import codeu.chat.util.Time;

import codeu.chat.util.Uuid;
public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";
    
  public final Time startTime;
  public final Uuid version;

  public ServerInfo(){
      this.startTime = Time.now();
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

  public ServerInfo(Uuid version, Time startTime) {
    this.version = version;
    this.startTime = startTime;
  }
  
  public String upTime() {
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime.inMs();
    return ""+totalTime + " ms"; 
  }
}
