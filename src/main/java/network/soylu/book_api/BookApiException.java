package network.soylu.book_api;

/**
 * Unchecked exception thrown when the Book-API encounters an unrecoverable problem.
 *
 * <p>Typical causes:</p>
 * <ul>
 *   <li>Attempting to create a second singleton {@link BookAPI} instance.</li>
 *   <li>Failure while reflecting into NMS / CraftBukkit classes.</li>
 *   <li>Providing an ItemStack that is not a book where one is required.</li>
 * </ul>
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class BookApiException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message (potentially {@code null})
     */
    public BookApiException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message (potentially {@code null})
     * @param cause the cause (potentially {@code null})
     */
    public BookApiException(String message, Throwable cause) {
        super(message, cause);
    }
}