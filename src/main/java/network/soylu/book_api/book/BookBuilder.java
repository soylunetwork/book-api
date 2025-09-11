package network.soylu.book_api.book;

import network.soylu.book_api.util.BookUtils;
import network.soylu.book_api.util.MiniMessageSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class BookBuilder {

    private String title;
    private String author;
    private final List<String> pages;
    private boolean signed;
    private boolean useMiniMessage;
    private MiniMessageSerializer miniMessageSerializer;

    public BookBuilder() {
        this.pages = new ArrayList<>();
        this.signed = false;
        this.useMiniMessage = false;
    }

    @NotNull
    public BookBuilder title(@Nullable String title) {
        this.title = title;
        return this;
    }

    @NotNull
    public BookBuilder author(@Nullable String author) {
        this.author = author;
        return this;
    }

    @NotNull
    public BookBuilder withMiniMessage() {
        this.useMiniMessage = true;
        if (this.miniMessageSerializer == null) {
            this.miniMessageSerializer = new MiniMessageSerializer();
        }
        return this;
    }

    @NotNull
    public BookBuilder withoutMiniMessage() {
        this.useMiniMessage = false;
        return this;
    }

    @NotNull
    public BookBuilder addPage(@NotNull String page) {
        if (pages.size() >= BookUtils.MAX_PAGES) {
            throw new IllegalStateException("Cannot add more than " + BookUtils.MAX_PAGES + " pages to a book");
        }

        String processedPage = processPageContent(page);
        this.pages.add(processedPage);
        return this;
    }

    @NotNull
    public BookBuilder addMiniMessagePage(@NotNull String miniMessagePage) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String jsonPage = miniMessageSerializer.miniMessageToJson(miniMessagePage);
        return addPage(jsonPage);
    }

    @NotNull
    public BookBuilder addClickablePage(@NotNull String text, @NotNull String command) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String clickableText = miniMessageSerializer.createClickableCommand(text, command);
        return addMiniMessagePage(clickableText);
    }

    @NotNull
    public BookBuilder addHoverPage(@NotNull String text, @NotNull String hoverText) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String hoverTextFormatted = miniMessageSerializer.createHoverText(text, hoverText);
        return addMiniMessagePage(hoverTextFormatted);
    }

    @NotNull
    public BookBuilder addInteractivePage(@NotNull String text, @NotNull String clickAction,
                                          @NotNull String clickValue, @NotNull String hoverText) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String interactiveText = miniMessageSerializer.createInteractiveText(text, clickAction, clickValue, hoverText);
        return addMiniMessagePage(interactiveText);
    }

    @NotNull
    public BookBuilder addGradientPage(@NotNull String text, @NotNull String startColor, @NotNull String endColor) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String gradientText = miniMessageSerializer.createGradientText(text, startColor, endColor);
        return addMiniMessagePage(gradientText);
    }

    @NotNull
    public BookBuilder addRainbowPage(@NotNull String text) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        String rainbowText = miniMessageSerializer.createRainbowText(text);
        return addMiniMessagePage(rainbowText);
    }

    @NotNull
    public BookBuilder addPages(@NotNull String... pages) {
        return addPages(Arrays.asList(pages));
    }

    @NotNull
    public BookBuilder addPages(@NotNull List<String> pages) {
        for (String page : pages) {
            addPage(page);
        }
        return this;
    }

    @NotNull
    public BookBuilder pages(@NotNull String... pages) {
        return pages(Arrays.asList(pages));
    }

    @NotNull
    public BookBuilder pages(@NotNull List<String> pages) {
        this.pages.clear();
        return addPages(pages);
    }

    @NotNull
    public BookBuilder insertPage(int index, @NotNull String page) {
        if (pages.size() >= BookUtils.MAX_PAGES) {
            throw new IllegalStateException("Cannot add more than " + BookUtils.MAX_PAGES + " pages to a book");
        }

        String processedPage = processPageContent(page);
        this.pages.add(index, processedPage);
        return this;
    }

    @NotNull
    public BookBuilder removePage(int index) {
        this.pages.remove(index);
        return this;
    }

    @NotNull
    public BookBuilder clearPages() {
        this.pages.clear();
        return this;
    }

    @NotNull
    public BookBuilder signed(boolean signed) {
        this.signed = signed;
        return this;
    }

    @NotNull
    public BookBuilder signed() {
        return signed(true);
    }

    @NotNull
    @SuppressWarnings("UnusedReturnValue")
    public BookBuilder addBlankPage() {
        return addPage("");
    }

    @NotNull
    public BookBuilder addBlankPages(int count) {
        for (int i = 0; i < count; i++) {
            addBlankPage();
        }
        return this;
    }

    @NotNull
    public BookBuilder addWrappedPage(@NotNull String text) {
        String wrappedText = BookUtils.wrapText(text, BookUtils.MAX_LINE_LENGTH, BookUtils.MAX_LINES_PER_PAGE);
        return addPage(wrappedText);
    }

    @NotNull
    public BookBuilder addText(@NotNull String text) {
        List<String> splitPages = BookUtils.splitTextIntoPages(text);
        return addPages(splitPages);
    }

    @NotNull
    public BookBuilder addMenuPage(@NotNull String title, @NotNull String... options) {
        if (miniMessageSerializer == null) {
            miniMessageSerializer = new MiniMessageSerializer();
        }

        StringBuilder menuPage = new StringBuilder();

        menuPage.append("<bold><gold>").append(title).append("</gold></bold>\n\n");

        for (String option : options) {
            String[] parts = option.split(":", 2);
            if (parts.length == 2) {
                String displayText = parts[0];
                String command = parts[1];

                menuPage.append("<click:run_command:/").append(command).append(">")
                        .append("<hover:show_text:'Click to execute: /").append(command).append("'>")
                        .append("<aqua>• ").append(displayText).append("</aqua>")
                        .append("</hover></click>\n");
            } else {
                menuPage.append("<gray>• ").append(option).append("</gray>\n");
            }
        }

        return addMiniMessagePage(menuPage.toString());
    }

    public int getPageCount() {
        return pages.size();
    }

    public boolean hasPages() {
        return !pages.isEmpty();
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean isMiniMessageEnabled() {
        return useMiniMessage;
    }

    @NotNull
    public Book build() {
        return new Book(title, author, new ArrayList<>(pages), signed);
    }

    @NotNull
    public BookBuilder copy() {
        BookBuilder copy = new BookBuilder()
                .title(title)
                .author(author)
                .signed(signed);

        if (useMiniMessage) {
            copy.withMiniMessage();
        }

        copy.pages.addAll(this.pages);
        return copy;
    }

    private String processPageContent(@NotNull String content) {
        if (useMiniMessage && miniMessageSerializer != null) {
            return miniMessageSerializer.miniMessageToJson(content);
        } else {
            return BookUtils.validateAndTruncatePage(content);
        }
    }
}