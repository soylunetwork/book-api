package network.soylu.book_api.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class BookUtils {

    public static final int MAX_PAGES = 100;

    public static final int MAX_CHARACTERS_PER_PAGE = 1024;

    public static final int MAX_LINES_PER_PAGE = 14;

    public static final int MAX_LINE_LENGTH = 19;

    public static final int MAX_TITLE_LENGTH = 32;

    public static final int MAX_AUTHOR_LENGTH = 16;

    private BookUtils() {
    }

    @NotNull
    public static String validateAndTruncatePage(@NotNull String page) {

        if (page.length() > MAX_CHARACTERS_PER_PAGE) {
            return page.substring(0, MAX_CHARACTERS_PER_PAGE);
        }

        return page;
    }

    @NotNull
    public static String validateAndTruncateTitle(@NotNull String title) {

        if (title.length() > MAX_TITLE_LENGTH) {
            return title.substring(0, MAX_TITLE_LENGTH);
        }

        return title;
    }

    @NotNull
    public static String validateAndTruncateAuthor(@NotNull String author) {

        if (author.length() > MAX_AUTHOR_LENGTH) {
            return author.substring(0, MAX_AUTHOR_LENGTH);
        }

        return author;
    }

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

    public static boolean fitsInOnePage(@NotNull String text) {
        return text.length() <= MAX_CHARACTERS_PER_PAGE && estimateLines(text) <= MAX_LINES_PER_PAGE;
    }

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

    @NotNull
    public static String centerText(@NotNull String text) {
        if (text.isEmpty()) {
            return "";
        }

        if (text.length() >= MAX_LINE_LENGTH) {
            return text.substring(0, MAX_LINE_LENGTH);
        }

        int padding = (MAX_LINE_LENGTH - text.length()) / 2;

        return " ".repeat(padding) +
                text;
    }
}