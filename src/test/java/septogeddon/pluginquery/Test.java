package septogeddon.pluginquery;

import septogeddon.pluginquery.utils.MutableString;

public class Test {
    public static void main(String[]args) {
        String instance = "";
        MutableString test = new MutableString(instance);
        test.setValue("this is another test");
        test.concat(" lmao");
        System.out.println(instance);
    }
}
