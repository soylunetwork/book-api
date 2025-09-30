package network.soylu.book_api.book;

import network.soylu.book_api.util.BookUtils;
import network.soylu.book_api.util.MiniMessageSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder class for creating {@link Book} instances with customizable properties and pages.
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class BookBuilder {

    private String title;
    private String author;
    private final List<String> pages;
    private boolean signed;
    private boolean useMiniMessage;
    private MiniMessageSerializer miniMessageSerializer;

    /**
     * Constructs a new BookBuilder instance.
     */
    public BookBuilder() {
        this.pages = new ArrayList<>();
        this.signed = false;
        this.useMiniMessage = false;
    }

    /**
     * Sets the title of the book.
     *
     * @param title the book title, can be null
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder title(@Nullable String title) {
        this.title = title != null ? BookUtils.validateAndTruncateTitle(title) : null;
        return this;
    }

    /**
     * Sets the author of the book.
     *
     * @param author the book author, can be null
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder author(@Nullable String author) {
        this.author = author != null ? BookUtils.validateAndTruncateAuthor(author) : null;
        return this;
    }

    /**
     * Enables MiniMessage formatting for pages.
     *
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder withMiniMessage() {
        this.useMiniMessage = true;
        if (this.miniMessageSerializer == null) {
            this.miniMessageSerializer = new MiniMessageSerializer();
        }
        return this;
    }

    /**
     * Disables MiniMessage formatting for pages.
     *
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder withoutMiniMessage() {
        this.useMiniMessage = false;
        return this;
    }

    /**
     * Adds a page to the book.
     *
     * @param page the page content
     * @return this builder for chaining
     * @throws IllegalStateException if the maximum page limit is reached
     */
    @NotNull
    public BookBuilder addPage(@NotNull String page) {
        if (pages.size() >= BookUtils.MAX_PAGES) {
            throw new IllegalStateException("Cannot add more than " + BookUtils.MAX_PAGES + " pages to a book");
        }

        String processedPage = processPageContent(page);
        this.pages.add(processedPage);
        return this;
    }

    /**
     * Adds a MiniMessage-formatted page to the book.
     *
     * @param miniMessagePage the MiniMessage-formatted page content
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addMiniMessagePage(@NotNull String miniMessagePage) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String jsonPage = miniMessageSerializer.miniMessageToJson(miniMessagePage);
        return addPage(jsonPage);
    }

    /**
     * Adds a clickable page with a command.
     *
     * @param text the display text
     * @param command the command to execute
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addClickablePage(@NotNull String text, @NotNull String command) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String clickableText = miniMessageSerializer.createClickableCommand(text, command);
        return addMiniMessagePage(clickableText);
    }

    /**
     * Adds a page with hover text.
     *
     * @param text the display text
     * @param hoverText the hover text to display
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addHoverPage(@NotNull String text, @NotNull String hoverText) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String hoverTextFormatted = miniMessageSerializer.createHoverText(text, hoverText);
        return addMiniMessagePage(hoverTextFormatted);
    }

    /**
     * Adds an interactive page with click and hover actions.
     *
     * @param text the display text
     * @param clickAction the click action type
     * @param clickValue the click action value
     * @param hoverText the hover text to display
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addInteractivePage(@NotNull String text, @NotNull String clickAction,
                                          @NotNull String clickValue, @NotNull String hoverText) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String interactiveText = miniMessageSerializer.createInteractiveText(text, clickAction, clickValue, hoverText);
        return addMiniMessagePage(interactiveText);
    }

    /**
     * Adds a gradient-colored page.
     *
     * @param text the text to apply the gradient to
     * @param startColor the starting color (hex or name)
     * @param endColor the ending color (hex or name)
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addGradientPage(@NotNull String text, @NotNull String startColor, @NotNull String endColor) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String gradientText = miniMessageSerializer.createGradientText(text, startColor, endColor);
        return addMiniMessagePage(gradientText);
    }

    /**
     * Adds a rainbow-colored page.
     *
     * @param text the text to apply rainbow effect to
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addRainbowPage(@NotNull String text) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String rainbowText = miniMessageSerializer.createRainbowText(text);
        return addMiniMessagePage(rainbowText);
    }

    /**
     * Adds multiple pages to the book.
     *
     * @param pages the pages to add
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addPages(@NotNull String... pages) {
        return addPages(Arrays.asList(pages));
    }

    /**
     * Adds a list of pages to the book.
     *
     * @param pages the list of pages to add
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addPages(@NotNull List<String> pages) {
        for (String page : pages) {
            addPage(page);
        }
        return this;
    }

    /**
     * Sets the pages of the book, replacing existing ones.
     *
     * @param pages the pages to set
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder pages(@NotNull String... pages) {
        return pages(Arrays.asList(pages));
    }

    /**
     * Sets the pages of the book, replacing existing ones.
     *
     * @param pages the list of pages to set
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder pages(@NotNull List<String> pages) {
        this.pages.clear();
        return addPages(pages);
    }

    /**
     * Inserts a page at the specified index.
     *
     * @param index the 0-based index to insert at
     * @param page the page content
     * @return this builder for chaining
     * @throws IllegalStateException if the maximum page limit is reached
     */
    @NotNull
    public BookBuilder insertPage(int index, @NotNull String page) {
        if (pages.size() >= BookUtils.MAX_PAGES) {
            throw new IllegalStateException("Cannot add more than " + BookUtils.MAX_PAGES + " pages to a book");
        }

        String processedPage = processPageContent(page);
        this.pages.add(index, processedPage);
        return this;
    }

    /**
     * Removes a page at the specified index.
     *
     * @param index the 0-based index of the page to remove
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder removePage(int index) {
        this.pages.remove(index);
        return this;
    }

    /**
     * Clears all pages from the book.
     *
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder clearPages() {
        this.pages.clear();
        return this;
    }

    /**
     * Sets whether the book is signed.
     *
     * @param signed true if the book is signed, false otherwise
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder signed(boolean signed) {
        this.signed = signed;
        return this;
    }

    /**
     * Marks the book as signed.
     *
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder signed() {
        return signed(true);
    }

    /**
     * Adds a blank page to the book.
     *
     * @return this builder for chaining
     */
    @NotNull
    public BookBuilder addBlankPage() {
        return addPage("");
    }

    /**
     * Adds multiple blank pages to the book.
     *
     * @param count the number of blank pages to add
     * @return this builder for chaining
     * @throws IllegalStateException if the maximum page limit is reached
     */
    @NotNull
    public BookBuilder addBlankPages(int count) {
        for (int i = 0; i < count; i++) {
            addBlankPage();
        }
        return this;
    }

    /**
     * Builds a new Book instance with the configured properties.
     *
     * @return the constructed Book instance
     */
    @NotNull
    public Book build() {
        return new Book(title, author, pages, signed);
    }

    /**
     * Processes page content, applying MiniMessage formatting and validation if enabled.
     *
     * @param page the page content to process
     * @return the processed page content
     */
    @NotNull
    private String processPageContent(@NotNull String page) {
        String processedPage = BookUtils.validateAndTruncatePage(page);
        if (useMiniMessage && miniMessageSerializer != null) {
            processedPage = miniMessageSerializer.miniMessageToJson(processedPage);
        }
        return processedPage;
    }
}