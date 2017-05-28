public final class ServerInfo {
    public final Time startTime;
    public ServerInfo () {
        this.startTime = Time.now();
    }
    public ServerInfo (Time startTime) {
        this.startTime = startTime;
    }
    
private static final ServerInfo info = new ServerInfo();
    

if (type == NetworkCode.SERVER_INFO_REQUEST) {
  Serializers.INTEGER.write(out, NetworkCode.SERVER_INFO_RESPONSE);
  Uuid.SERIALIZER.write(out, info.version);
} else if …

public static final SERVER_INFO_REQUEST = 31;
public static final SERVER_INFO_RESPONSE = 32;
