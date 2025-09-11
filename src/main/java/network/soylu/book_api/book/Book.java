package network.soylu.book_api.book;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a book with title, author, and pages.
 * This class is immutable after creation.
 *
 * @author Soylu
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Book {

    private final String title;
    private final String author;
    private final List<String> pages;
    private final boolean signed;

    /**
     * Creates a new Book instance.
     *
     * @param title The book title (can be null)
     * @param author The book author (can be null)
     * @param pages The book pages (cannot be null)
     * @param signed Whether the book is signed
     */
    public Book(@Nullable String title, @Nullable String author, @NotNull List<String> pages, boolean signed) {
        this.title = title;
        this.author = author;
        this.pages = new ArrayList<>(Objects.requireNonNull(pages, "Pages cannot be null"));
        this.signed = signed;
    }

    /**
     * Gets the book title.
     *
     * @return The book title, or null if not set
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Gets the book author.
     *
     * @return The book author, or null if not set
     */
    @Nullable
    public String getAuthor() {
        return author;
    }

    /**
     * Gets an immutable copy of the book pages.
     *
     * @return The book pages
     */
    @NotNull
    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }

    /**
     * Gets a specific page by index.
     *
     * @param index The page index (0-based)
     * @return The page content
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @NotNull
    public String getPage(int index) {
        return pages.get(index);
    }

    /**
     * Gets the number of pages in the book.
     *
     * @return The page count
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * Checks if the book is signed.
     *
     * @return true if the book is signed, false otherwise
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * Checks if the book has a title.
     *
     * @return true if the book has a title, false otherwise
     */
    public boolean hasTitle() {
        return title != null && !title.isEmpty();
    }

    /**
     * Checks if the book has an author.
     *
     * @return true if the book has an author, false otherwise
     */
    public boolean hasAuthor() {
        return author != null && !author.isEmpty();
    }

    /**
     * Checks if the book has pages.
     *
     * @return true if the book has pages, false otherwise
     */
    public boolean hasPages() {
        return !pages.isEmpty();
    }

    /**
     * Converts this book to an ItemStack.
     *
     * @return The ItemStack representation of this book
     */
    @NotNull
    public ItemStack toItemStack() {
        Material material = signed ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK;
        ItemStack itemStack = new ItemStack(material);
        BookMeta meta = (BookMeta) itemStack.getItemMeta();

        if (meta != null) {
            if (hasTitle()) {
                meta.setTitle(title);
            }

            if (hasAuthor()) {
                meta.setAuthor(author);
            }

            if (hasPages()) {
                meta.setPages(pages);
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    /**
     * Creates a builder from this book.
     *
     * @return A new BookBuilder with this book's properties
     */
    @NotNull
    public BookBuilder toBuilder() {
        BookBuilder builder = new BookBuilder()
                .title(title)
                .author(author)
                .signed(signed);

        for (String page : pages) {
            builder.addPage(page);
        }

        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;

        Book book = (Book) o;
        return signed == book.signed &&
                Objects.equals(title, book.title) &&
                Objects.equals(author, book.author) &&
                Objects.equals(pages, book.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, pages, signed);
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", pages=" + pages.size() +
                ", signed=" + signed +
                '}';
    }
}