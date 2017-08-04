package codeu.chat.server;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AuthorityBuffer
{
    public static final Serializer<AuthorityBuffer> SERIALIZER = new Serializer<AuthorityBuffer>()
    {
        @Override
        public void write(OutputStream out, AuthorityBuffer value) throws IOException
        {
            Uuid.SERIALIZER.write(out, value.conversation);
            Uuid.SERIALIZER.write(out, value.user);
            Serializers.INTEGER.write(out, Integer.valueOf(value.authorityByte));
        }
        @Override
        public AuthorityBuffer read(InputStream in) throws IOException
        {
            return new AuthorityBuffer(Uuid.SERIALIZER.read(in), 
                                       Uuid.SERIALIZER.read(in), 
                                       Serializers.INTEGER.read(in).byteValue());
        }
    };

    public final Uuid conversation;
    public final Uuid user;
    public final byte authorityByte;

    public AuthorityBuffer(Uuid conversation, Uuid user, byte authorityByte)
    {
        this.conversation = conversation;
        this.user = user;
        this.authorityByte = authorityByte;
    }
}