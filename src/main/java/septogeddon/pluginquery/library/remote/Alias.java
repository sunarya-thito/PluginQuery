package septogeddon.pluginquery.library.remote;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Alias for Method Name
 * @author Thito Yalasatria Sunarya
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Alias {

    String value();

}
