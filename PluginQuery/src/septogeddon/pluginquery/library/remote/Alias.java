package septogeddon.pluginquery.library.remote;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * Alias for Method Name
 * @author Thito Yalasatria Sunarya
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Alias {

	public String value();
	
}
