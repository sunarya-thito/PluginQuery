package septogeddon.pluginquery.library.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/***
 * 
 * @author USER
 *
 */
public class TypeHint implements Externalizable {

	private static final long serialVersionUID = 1L;
	private ArrayList<String> interfaces = new ArrayList<>();
	public TypeHint() {}
	public TypeHint(Class<?> cl) {
		List<Class<?>> classes = new ArrayList<>();
		collectRelatedClasses(cl, classes);
		for (Class<?> clx : classes) {
			interfaces.add(clx.getName());
		}
	}
	
	public List<String> getRelatedClasses() {
		return interfaces;
	}
	
	private static void collectRelatedClasses(Class<?> cl, List<Class<?>> cls) {
		if (cl == null || cls.contains(cl)) return;
		cls.add(cl);
		collectRelatedClasses(cl.getSuperclass(), cls);
		Class<?>[] interf = cl.getInterfaces();
		if (interf != null) {
			for (int i = 0; i < interf.length; i++) {
				collectRelatedClasses(interf[i], cls);
			}
		}
	}
	
	public List<Class<?>> findAvailableRelatedClasses(ClassRegistry registry){
		List<Class<?>> classes = new ArrayList<>();
		for (String s : interfaces) {
			try {
				Class<?> cls = registry.getClass(s);
				if (!cls.isInterface()) continue;
				classes.add(cls);
			} catch (ClassNotFoundException e) {
			}
		}
		return classes;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(interfaces);
	}
	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		interfaces = (ArrayList<String>)in.readObject();
	}
	
}
