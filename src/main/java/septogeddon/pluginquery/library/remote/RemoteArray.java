package septogeddon.pluginquery.library.remote;

import septogeddon.pluginquery.utils.QueryUtil;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;

/**
 * Sterilize array component type into object array then convert it again into normal array
 * @author Thito Yalasatria Sunarya
 *
 */
public class RemoteArray implements Externalizable {

    private static final long serialVersionUID = 1L;
    private String componentType;
    private Object[] array;

    public RemoteArray() {
    }

    public RemoteArray(Object array) {
        QueryUtil.illegalArgument(!array.getClass().isArray(), "not an array");
        componentType = array.getClass().getComponentType().getName();
        this.array = new Object[Array.getLength(array)];
        System.arraycopy(array, 0, this.array, 0, this.array.length);
    }

    public Object findAvailableComponent(ClassRegistry registry) throws ClassNotFoundException {
        Object array = Array.newInstance(registry.getClass(componentType), this.array.length);
        System.arraycopy(this.array, 0, array, 0, this.array.length);
        return array;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(componentType);
        out.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            out.writeObject(array[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        componentType = (String) in.readObject();
        array = new Object[in.readInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = in.readObject();
        }
    }


}
