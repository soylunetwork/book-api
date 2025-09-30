package network.soylu.book_api.book;

import network.soylu.book_api.BookAPI;
import network.soylu.book_api.BookApiException;
import network.soylu.book_api.book.BookBuilder;
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
 * Represents an immutable book with title, author, and pages for use in Minecraft.
 *
 * @author sheduxdev
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
     * @param title the book title, can be null
     * @param author the book author, can be null
     * @param pages the book pages, cannot be null
     * @param signed whether the book is signed
     * @throws NullPointerException if pages is null
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
     * @return the book title, or null if not set
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Gets the book author.
     *
     * @return the book author, or null if not set
     */
    @Nullable
    public String getAuthor() {
        return author;
    }

    /**
     * Gets an immutable copy of the book pages.
     *
     * @return an unmodifiable list of pages
     */
    @NotNull
    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }

    /**
     * Gets the content of a specific page by index.
     *
     * @param index the 0-based page index
     * @return the page content
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @NotNull
    public String getPage(int index) {
        return pages.get(index);
    }

    /**
     * Gets the number of pages in the book.
     *
     * @return the page count
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
     * @return true if the book has a non-empty title, false otherwise
     */
    public boolean hasTitle() {
        return title != null && !title.isEmpty();
    }

    /**
     * Checks if the book has an author.
     *
     * @return true if the book has a non-empty author, false otherwise
     */
    public boolean hasAuthor() {
        return author != null && !author.isEmpty();
    }

    /**
     * Checks if the book has pages.
     *
     * @return true if the book has at least one page, false otherwise
     */
    public boolean hasPages() {
        return !pages.isEmpty();
    }

    /**
     * Converts this book to a Minecraft ItemStack.
     *
     * @return the ItemStack representation of the book
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
     * Creates a BookBuilder initialized with this book's properties.
     *
     * @return a new BookBuilder instance
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

    /**
     * Checks if this book is equal to another object.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
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

    /**
     * Computes the hash code for this book.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, author, pages, signed);
    }

    /**
     * Returns a string representation of this book.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", pages=" + pages.size() +
                ", signed=" + signed +
                '}';
    }

    /**
     * Creates a Book instance from a Minecraft ItemStack.
     *
     * @param itemStack the ItemStack to convert
     * @return the Book instance
     * @throws BookApiException if the ItemStack is not a book
     */
    @NotNull
    public static Book fromItemStack(@NotNull ItemStack itemStack) {
        if (!isBook(itemStack)) {
            throw new BookApiException("ItemStack is not a book!");
        }

        BookMeta meta = (BookMeta) itemStack.getItemMeta();
        BookBuilder builder = BookAPI.builder();

        if (meta != null) {
            if (meta.hasTitle()) {
                builder.title(meta.getTitle());
            }

            if (meta.hasAuthor()) {
                builder.author(meta.getAuthor());
            }

            if (meta.hasPages()) {
                for (String page : meta.getPages()) {
                    builder.addPage(page);
                }
            }
        }

        return builder.build();
    }

    /**
     * Checks if an ItemStack is a book (writable or written).
     *
     * @param itemStack the ItemStack to check
     * @return true if the ItemStack is a book, false otherwise
     */
    public static boolean isBook(@NotNull ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof BookMeta;
    }
}