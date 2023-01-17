package com.fortidast;
import java.text.MessageFormat;
import static java.util.ResourceBundle.getBundle;
/**.
 * Expose message resources
 */
final class MessageManager {

    /**
     * Object to handle error messages.
     */
    private static java.util.ResourceBundle fortipentestBundle = getBundle("Messages");
    /**.
     * contructor
     */
    private MessageManager() {

    }
    /**
     * @param key gets key as a parameter
     * @param args values thats get replaced in the key
     * @return returns value based on the key provided.
     */
    static String getString(String key, Object... args) {
        String message = fortipentestBundle.getString(key);

        if (args != null && args.length > 0) {
            return MessageFormat.format(message, args);
        }
        return message;
    }
}