package network.soylu.book_api.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling book-related text processing and validation.
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class BookUtils {

    /** Maximum number of pages allowed in a book. */
    public static final int MAX_PAGES = 100;

    /** Maximum number of characters allowed per page. */
    public static final int MAX_CHARACTERS_PER_PAGE = 1024;

    /** Maximum number of lines allowed per page. */
    public static final int MAX_LINES_PER_PAGE = 14;

    /** Maximum length of a line in characters. */
    public static final int MAX_LINE_LENGTH = 19;

    /** Maximum length of a book title. */
    public static final int MAX_TITLE_LENGTH = 32;

    /** Maximum length of a book author name. */
    public static final int MAX_AUTHOR_LENGTH = 16;

    /**
     * Private constructor to prevent instantiation.
     */
    private BookUtils() {
    }

    /**
     * Validates and truncates a page's content to the maximum allowed characters.
     *
     * @param page the page content to validate
     * @return the validated and possibly truncated page content
     */
    @NotNull
    public static String validateAndTruncatePage(@NotNull String page) {
        if (page.length() > MAX_CHARACTERS_PER_PAGE) {
            return page.substring(0, MAX_CHARACTERS_PER_PAGE);
        }
        return page;
    }

    /**
     * Validates and truncates a book title to the maximum allowed length.
     *
     * @param title the title to validate
     * @return the validated and possibly truncated title
     */
    @NotNull
    public static String validateAndTruncateTitle(@NotNull String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            return title.substring(0, MAX_TITLE_LENGTH);
        }
        return title;
    }

    /**
     * Validates and truncates a book author name to the maximum allowed length.
     *
     * @param author the author name to validate
     * @return the validated and possibly truncated author name
     */
    @NotNull
    public static String validateAndTruncateAuthor(@NotNull String author) {
        if (author.length() > MAX_AUTHOR_LENGTH) {
            return author.substring(0, MAX_AUTHOR_LENGTH);
        }
        return author;
    }

    /**
     * Wraps text to fit within specified line length and maximum lines.
     *
     * @param text the text to wrap
     * @param lineLength the maximum length of a line
     * @param maxLines the maximum number of lines
     * @return the wrapped text
     */
    @NotNull
    public static String wrapText(@NotNull String text, int lineLength, int maxLines) {
        if (text.isEmpty()) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > lineLength) {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }

                    while (word.length() > lineLength) {
                        lines.add(word.substring(0, lineLength));
                        word = word.substring(lineLength);
                    }

                    currentLine.append(word);
                } else {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        if (lines.size() > maxLines) {
            lines = lines.subList(0, maxLines);
        }

        return String.join("\n", lines);
    }

    /**
     * Splits text into multiple pages based on character and line limits.
     *
     * @param text the text to split
     * @return a list of pages
     */
    @NotNull
    public static List<String> splitTextIntoPages(@NotNull String text) {
        List<String> pages = new ArrayList<>();
        if (text.isEmpty()) {
            return pages;
        }

        String[] paragraphs = text.split("\n\n");
        StringBuilder currentPage = new StringBuilder();

        for (String paragraph : paragraphs) {
            String wrappedParagraph = wrapText(paragraph, MAX_LINE_LENGTH, Integer.MAX_VALUE);

            String testPage = !currentPage.isEmpty()
                    ? currentPage + "\n\n" + wrappedParagraph
                    : wrappedParagraph;

            if (testPage.length() > MAX_CHARACTERS_PER_PAGE ||
                    testPage.split("\n").length > MAX_LINES_PER_PAGE) {

                if (!currentPage.isEmpty()) {
                    pages.add(validateAndTruncatePage(currentPage.toString()));
                    currentPage = new StringBuilder();
                }

                if (wrappedParagraph.length() > MAX_CHARACTERS_PER_PAGE ||
                        wrappedParagraph.split("\n").length > MAX_LINES_PER_PAGE) {

                    pages.addAll(splitLargeParagraph(wrappedParagraph));
                } else {
                    currentPage.append(wrappedParagraph);
                }
            } else {
                if (!currentPage.isEmpty()) {
                    currentPage.append("\n\n");
                }
                currentPage.append(wrappedParagraph);
            }
        }

        if (!currentPage.isEmpty()) {
            pages.add(validateAndTruncatePage(currentPage.toString()));
        }

        return pages;
    }

    /**
     * Splits a large paragraph into multiple pages.
     *
     * @param paragraph the paragraph to split
     * @return a list of pages
     */
    @NotNull
    private static List<String> splitLargeParagraph(@NotNull String paragraph) {
        List<String> pages = new ArrayList<>();
        String[] lines = paragraph.split("\n");

        StringBuilder currentPage = new StringBuilder();
        int currentLines = 0;

        for (String line : lines) {
            String testPage = !currentPage.isEmpty()
                    ? currentPage + "\n" + line
                    : line;

            if (testPage.length() > MAX_CHARACTERS_PER_PAGE || currentLines >= MAX_LINES_PER_PAGE) {
                if (!currentPage.isEmpty()) {
                    pages.add(currentPage.toString());
                    currentPage = new StringBuilder();
                }

                if (line.length() > MAX_CHARACTERS_PER_PAGE) {
                    line = line.substring(0, MAX_CHARACTERS_PER_PAGE);
                }

                currentPage.append(line);
                currentLines = 1;
            } else {
                if (!currentPage.isEmpty()) {
                    currentPage.append("\n");
                }
                currentPage.append(line);
                currentLines++;
            }
        }

        if (!currentPage.isEmpty()) {
            pages.add(currentPage.toString());
        }

        return pages;
    }

    /**
     * Estimates the number of lines required for the given text.
     *
     * @param text the text to estimate
     * @return the estimated number of lines
     */
    public static int estimateLines(@NotNull String text) {
        if (text.isEmpty()) {
            return 0;
        }

        String[] lines = text.split("\n");
        int totalLines = 0;

        for (String line : lines) {
            totalLines += Math.max(1, (int) Math.ceil((double) line.length() / MAX_LINE_LENGTH));
        }

        return totalLines;
    }

    /**
     * Checks if the given text fits on a single page.
     *
     * @param text the text to check
     * @return true if the text fits on one page, false otherwise
     */
    public static boolean fitsInOnePage(@NotNull String text) {
        return text.length() <= MAX_CHARACTERS_PER_PAGE && estimateLines(text) <= MAX_LINES_PER_PAGE;
    }

    /**
     * Creates a centered page header with the specified title.
     *
     * @param title the title for the header
     * @return the formatted header string
     */
    @NotNull
    public static String createPageHeader(@NotNull String title) {
        if (title.isEmpty()) {
            return "";
        }

        int padding = Math.max(0, (MAX_LINE_LENGTH - title.length()) / 2);
        return " ".repeat(padding) +
                title + "\n" +
                "-".repeat(Math.max(0, Math.min(MAX_LINE_LENGTH, title.length() + padding * 2))) +
                "\n";
    }

    /**
     * Centers the given text within the maximum line length.
     *
     * @param text the text to center
     * @return the centered text
     */
    @NotNull
    public static String centerText(@NotNull String text) {
        if (text.isEmpty()) {
            return "";
        }

        if (text.length() >= MAX_LINE_LENGTH) {
            return text.substring(0, MAX_LINE_LENGTH);
        }

        int padding = (MAX_LINE_LENGTH - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
}