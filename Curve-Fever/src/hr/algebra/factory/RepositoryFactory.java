package hr.algebra.factory;

import hr.algebra.repository.Repository;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 *
 * @author efurkev
 */
public class RepositoryFactory {

    public static Repository getRepository(String type) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Objects.requireNonNull(type);
        return (Repository) Class.forName(type).getDeclaredConstructor().newInstance();
    }
}
