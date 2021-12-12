package ascii_art;

import ascii_art.img_to_char.BrightnessImgCharMatcher;
import ascii_output.AsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * a class that holds an image.
 * given specific user commands from a shell base communication, renders that image.
 */
public class Shell {
    private static final String CMD_EXIT = "exit";
    public static final String SHELL_PROMPT = ">>> ";
    private static final String FONT_NAME = "Courier New";
    private static final String OUTPUT_FILENAME = "out.html";
    private static final String INITIAL_CHARS_RANGE = "0-9";
    /*    commands     */
    public static final String CHARS_CMD = "chars";
    public static final String ADD_CMD = "add";
    public static final String REMOVE_CMD = "remove";
    public static final String RES_CMD = "res";
    public static final String CONSOLE_CMD = "console";
    public static final String RENDER_CMD = "render";
    /*    commands specification     */
    public static final String ADD_ALL = "all";
    public static final String ADD_SPACE = "space";
    public static final String RES_UP = "up";
    public static final String RES_DOWN = "down";
    /*    massages     */
    public static final String WIDTH_SET_MSG = "Width set to ";
    public static final String MAXIMAL_RESOLUTION_ERR_MSG = "You're using the maximal resolution";
    public static final String MINIMAL_RESOLUTION_ERR_MSG = "You're using the minimal resolution";
    public static final String RES_INPUT_ERR_MSG = "Bad input for resolution change. USAGE: \"res up/down\"";
    public static final String ADD_INPUT_ERR_MSG = "Bad input for adding chars. USAGE:" +
            " \"add all/space/range/single char\"";
    public static final String SHELL_INPUT_ERR_MSG = "BAD INPUT: USAGE:" +
            " \"chars/add/remove/res/console/render\"";
    public static final String REMOVE_INPUT_ERR_MSG = "Bad input for removing chars. USAGE:" +
            " \"remove all/space/range/single char\"";
    public static final String REGEX_PATTERN = "%s|(%s|%s) ((.-.)|%s|%s)|%s|%s|(%s (%s|%s))";

    private final BrightnessImgCharMatcher charMatcher;
    private final AsciiOutput output;
    private static final int MIN_PIXELS_PER_CHAR = 2;
    private final Set<Character> charSet = new HashSet<>();
    private static final int INITIAL_CHARS_IN_ROW = 64;
    private final int minCharsInRow;
    private final int maxCharsInRow;
    private int charsInRow;
    private boolean isConsolePrint;

    /**
     * Constructor
     *
     * @param img the current image to render
     */
    public Shell(Image img) {
        addChars(INITIAL_CHARS_RANGE);
        minCharsInRow = Math.max(1, img.getWidth() / img.getHeight());
        maxCharsInRow = img.getWidth() / MIN_PIXELS_PER_CHAR;
        charsInRow = Math.max(Math.min(INITIAL_CHARS_IN_ROW, maxCharsInRow), minCharsInRow);
        charMatcher = new BrightnessImgCharMatcher(img, FONT_NAME);
        output = new HtmlAsciiOutput(OUTPUT_FILENAME, FONT_NAME);
        isConsolePrint = false;
    }

    /**
     * runs the shell and displaying a user interface to receive commands.
     */
    public void run() {
        String userInput;
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.print(SHELL_PROMPT);
            userInput = scanner.nextLine();

            switch (getCommand(userInput)) {
                case CHARS_CMD:
                    showChars();
                    break;
                case ADD_CMD:
                    addChars(userInput.substring(userInput.indexOf(" ") + 1));
                    break;
                case REMOVE_CMD:
                    removeChars(userInput.substring(userInput.indexOf(" ") + 1));
                    break;
                case RES_CMD:
                    resChange(userInput.substring(userInput.indexOf(" ") + 1));
                    break;
                case CONSOLE_CMD:
                    this.isConsolePrint = true;
                    break;
                case RENDER_CMD: //todo: what should we do if there are no chars at all?
                    render();
                    break;
                default:
                    System.out.println(SHELL_INPUT_ERR_MSG);
            }
        } while (!userInput.equals(CMD_EXIT));
    }

    /*
        using Regex to get and check the given command from the user.
     */
    private String getCommand(String userInput) {
        Matcher m = Pattern.compile(
                        String.format(REGEX_PATTERN,
                                CHARS_CMD, ADD_CMD, REMOVE_CMD, ADD_ALL, ADD_SPACE,
                                RENDER_CMD, CONSOLE_CMD, RES_CMD, RES_UP, RES_DOWN))
                .matcher(userInput);
        if (!m.matches())
            return "";
        return userInput.substring(0, userInput.contains(" ") ? userInput.indexOf(" ") : userInput.length());
    }

    /*
        printing the current chars set to the console.
     */
    private void showChars() {
        charSet.stream().sorted().forEach(c -> System.out.print(c + " "));
        System.out.println();
    }

    /*
        extracting the desired adding command given from the user.
     */
    private static char[] parseCharRange(String param) {
        char[] charsToAdd;
        if (param.length() == 1) { // only one char to add
            charsToAdd = new char[]{param.charAt(0), param.charAt(0)};
            return charsToAdd;
        }
        if (param.equals(ADD_ALL)) {
            charsToAdd = new char[]{' ', '~'};
            return charsToAdd;
        }
        if (param.equals(ADD_SPACE)) {
            charsToAdd = new char[]{' ', ' '};
            return charsToAdd;
        }
        if (param.length() == 3 && param.charAt(1) == '-') { // add range
            charsToAdd = new char[]{param.charAt(0), param.charAt(2)};
            return charsToAdd;
        }
        return null;

    }

    /*
        extracting and adding the desired chars given from the user.
     */
    private void addChars(String s) {
        char[] range = parseCharRange(s);
        if (range != null) {
            // add all range to charSet
            Stream.iterate(range[0], c -> c <= range[1], c -> (char) ((int) c + 1)).forEach(charSet::add);
            Stream.iterate(range[1], c -> c <= range[0], c -> (char) ((int) c + 1)).forEach(charSet::add);
        } else System.out.println(ADD_INPUT_ERR_MSG);
    }

    /*
        extracting and removing the desired chars given from the user.
    */
    private void removeChars(String s) {
        char[] range = parseCharRange(s);
        if (range != null) {
            // remove all range from charSet
            Stream.iterate(range[0], c -> c <= range[1], c -> (char) ((int) c + 1)).forEach(charSet::remove);
            Stream.iterate(range[1], c -> c <= range[0], c -> (char) ((int) c + 1)).forEach(charSet::remove);
        } else
            System.out.println(REMOVE_INPUT_ERR_MSG);
    }

    /*
        extracting and changing to the desired resolution given from the user.
     */
    private void resChange(String s) {
        switch (s) {
            case RES_UP:
                if (charsInRow * 2 > maxCharsInRow) {
                    System.out.println(MAXIMAL_RESOLUTION_ERR_MSG);
                    return;
                }
                charsInRow *= 2;
                break;
            case RES_DOWN:
                if (charsInRow / 2 < minCharsInRow) {
                    System.out.println(MINIMAL_RESOLUTION_ERR_MSG);
                    return;
                }
                charsInRow /= 2;
                break;
            default:
                System.out.println(RES_INPUT_ERR_MSG);
                return;
        }
        System.out.println(WIDTH_SET_MSG + charsInRow);
    }

    /*
        rendering the image according to user previous specification.
     */
    private void render() {
        char[][] charsToPrint = charMatcher.chooseChars(charsInRow, charSet.toArray(Character[]::new));
        if (isConsolePrint)
            printToConsole(charsToPrint);
        output.output(charsToPrint);

    }

    /*
        prints a given chars 2D array to the console.
     */
    private void printToConsole(char[][] charsToPrint) {
        for (char[] chars : charsToPrint) {
            for (char aChar : chars) {
                System.out.print(aChar + "");
            }
            System.out.println();
        }
    }
}
