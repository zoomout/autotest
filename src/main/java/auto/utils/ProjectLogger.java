package auto.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectLogger {

    public static Logger getLogger(final String className) {
        return LoggerFactory.getLogger(className);
    }
}
