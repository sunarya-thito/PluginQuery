package septogeddon.pluginquery.library.remote;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Represent non-existing class ({@link #value()}) using annotated class
 * @author Thito Yalasatria Sunarya
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Represent {

    String[] value();

}
