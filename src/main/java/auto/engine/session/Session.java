package auto.engine.session;

import auto.utils.ProjectLogger;
import net.thucydides.core.SessionMap;
import net.thucydides.core.Thucydides;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static java.lang.String.format;

/**
 * Thread-safe static storage (Thucydides stores local session for each test-thread)
 */
public class Session {

    private static Logger getLogger() {
        return ProjectLogger.getLogger("Session");
    }

    /**
     * Method will warn if multiple keys equal by ignoreCase exist in Session
     */
    public static Object get(final Key key) {
        String byKey = key.toString();
        SessionMap<Object, Object> session = Thucydides.getCurrentSession();
        Object exact = session.get(byKey);
        String warnMsg;

        if (exact == null) {
            warnMsg = format("Object was not matched by exact key [%s], and", byKey);
        } else {
            warnMsg = format("Object was matched by exact key [%s], but", byKey);
        }

        String anotherKey;
        for (Map.Entry<Object, Object> mapEntry : session.entrySet()) {
            anotherKey = (String) mapEntry.getKey();
            if (anotherKey.equalsIgnoreCase(byKey) && !anotherKey.equals(byKey)) {
                getLogger().warn(warnMsg + " similar key exist in Session [{}] \nObject by this key: {}",
                        mapEntry.getKey(), mapEntry.getValue());
            }
        }

        getLogger().info("Get object by key: {}, object: {}", byKey, exact);

        return exact;
    }

    /**
     * Get String value
     */
    public static String getS(final Key key) {
        Object value = get(key);
        return value == null ? null : value.toString();
    }

    /**
     * Get int value
     */
    public static Integer getI(final Key key) {
        Object value = get(key);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    /**
     * Get boolean value
     */
    public static Boolean getB(final Key key) {
        Object value = get(key);
        return value == null ? null : Boolean.parseBoolean(value.toString());
    }

    /**
     * Get value of type Class
     */
    public static <T extends Object> T get(final Key key, final Class<T> type) {
        Object object = get(key);
        return (object == null) ? null : (T) object;
    }

    public static void put(final Key key, final Object value) {
        getLogger().info("Put object by key: {}, object: {}", key, value);
        Thucydides.getCurrentSession().put(key.toString(), value);
    }

    public static void remove(final Key key) {
        Thucydides.getCurrentSession().remove(key.toString());
    }

    /**
     * prints whole dump of session.
     */
    public static void printSessionDump() {
        getLogger().info("\n\n             !!!           session dump            !!!\n");
        for (Map.Entry<Object, Object> sessionEntry : Thucydides.getCurrentSession().entrySet()) {
            getLogger().info("[" + sessionEntry.getKey() + "]   [" + sessionEntry.getValue() + "]");
        }
        getLogger().info("\n             !!!       end session dump            !!!\n");
    }

    public static boolean containsKey(final Key key) {
        return Thucydides.getCurrentSession().containsKey(key.toString());
    }

    public static void clear() {
        getLogger().info("Print Session DUMP before clear");
        printSessionDump();
        Thucydides.getCurrentSession().clear();
    }

    /**
     * class represents data object which can provide assignment history.
     */
    private class DataEntry {
        private transient Stack<Object> data;

        /**
         * creates a new instance with empty data set.
         */
        public DataEntry() {
            data = new Stack<Object>();
        }

        /**
         * assigns values for the object. Note that this method does not overwrite any data. It only appends to stack
         * even if this is a null value.
         *
         * @param obj - value to place.
         */
        public void set(final Object obj) {
            data.push(obj);
        }

        /**
         * Removes an object value. This method does not remove any data physically. It writes null as next value.
         */
        public void remove() {
            data.push(null);
        }

        /**
         * returns the last assigned value.
         *
         * @return object data.
         */
        public Object get() {
            return data.isEmpty() ? null : data.peek();
        }

        /**
         * returns all the assignment history as a vector of values. All null assignments are considered.
         *
         * @return a vector where the last assigned value is the first element of returned set.
         */
        @SuppressWarnings("unchecked")
        public List<Object> getDump() {
            final List<Object> dump = new ArrayList<Object>();
            final Stack<Object> tempData = (Stack<Object>) data.clone();
            while (!tempData.isEmpty()) {
                dump.add(tempData.pop());
            }
            return dump;
        }
    }

}
