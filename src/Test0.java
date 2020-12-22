import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.*;
import org.apache.commons.collections.LRUMap;

import java.io.*;

public class Test0 {
    public static void main(String[] args) throws Exception{
        LRUMap lruMap = new LRUMap();
        byte[] ser = serialize(lruMap);
        FileOutputStream fileOutputStream = new FileOutputStream("emp.ser");
        fileOutputStream.write(ser);
        fileOutputStream.close();
    }
    public static byte[] serialize(Object data) throws IOException {
        MessageBody body = new MessageBody();
        body.setData(data);

        ActionMessage message = new ActionMessage();
        message.addBody(body);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AmfMessageSerializer serializer = new AmfMessageSerializer();
        serializer.initialize(SerializationContext.getSerializationContext(), out, null);
        serializer.writeMessage(message);

        return out.toByteArray();
    }

    public static void deserialize(byte[] amf) throws ClassNotFoundException, IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(amf);

        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        deserializer.initialize(SerializationContext.getSerializationContext(), in, null);
        deserializer.readMessage(new ActionMessage(), new ActionContext());
    }
}
