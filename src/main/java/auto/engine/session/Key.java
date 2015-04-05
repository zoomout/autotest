package auto.engine.session;


/**
 * Enumeration for test session keys.
 */
public enum Key {
    BROWSER_FIRST_OPEN("BROWSER_FIRST_OPEN"),
    COOKIES_SET("COOKIES_SET"),
    TEXT_VISIBILITY("TEXT_VISIBILITY");

    private final String text;

    /**
     * Provides a string representation of given enumeration.
     *
     * @param text string of text to attach to Key enum.
     */

    private Key(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
