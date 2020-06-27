package septogeddon.pluginquery.library.remote;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * Substitute {@link #value()} with the annotated class
 * @author Thito Yalasatria Sunarya
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Substitute {

	public Class<?>[] value();
	
}
