package ascii_art.img_to_char;

import image.Image;

import java.awt.*;
import java.util.HashMap;

/**
 * A class that handles all related brightness matches of a given image to a set of chars.
 */
public class BrightnessImgCharMatcher {
    private final Image img;
    private final String font;
    private final HashMap<Image, Double> cache = new HashMap<>();

    /**
     * Constructor
     *
     * @param img  the given image.
     * @param font the given font of the chars to calculate brightness in.
     */
    public BrightnessImgCharMatcher(Image img, String font) {
        this.img = img;
        this.font = font;
    }


    /**
     * choosing a set of chars with different brightness level to represent different pixels color and
     * brightness in the image.
     *
     * @param numCharsInRow how many char will be printed in a row of the pixel art.
     * @param charSet       a set of char to choose from.
     * @return a 2D array of the selected chars.
     */
    public char[][] chooseChars(int numCharsInRow, Character[] charSet) {
        double[] charsBrightnessLevel = getCharsBrightnessLevel(charSet);
        linearStretch(charsBrightnessLevel);
        return convertImageToAscii(numCharsInRow, img, charSet, charsBrightnessLevel);
    }

    /*
        performing a linear stretch over a given brightness level chars.
     */
    private void linearStretch(double[] charsBrightnessLevel) {
        double maxBrightness = getMaxBrightness(charsBrightnessLevel);
        double minBrightness = getMinBrightness(charsBrightnessLevel);
        if (maxBrightness <= minBrightness) return;
        for (int i = 0; i < charsBrightnessLevel.length; i++) {
            double level = charsBrightnessLevel[i];
            charsBrightnessLevel[i] = (level - minBrightness) / (maxBrightness - minBrightness);
        }
    }

    /*
        getting tha maximum brightness level in an array.
     */
    private double getMaxBrightness(double[] charsBrightnessLevel) {
        double maxBrightness = Double.MIN_VALUE;
        for (double level : charsBrightnessLevel) {
            if (level > maxBrightness)
                maxBrightness = level;
        }
        return maxBrightness;
    }

    /*
        getting tha minimum brightness level in an array.
     */
    private double getMinBrightness(double[] charsBrightnessLevel) {
        double minBrightness = Double.MAX_VALUE;
        for (double level : charsBrightnessLevel) {
            if (level < minBrightness)
                minBrightness = level;
        }
        return minBrightness;
    }

    /*
        given a set of chars, returning an array with brightness levels of those chars.
     */
    private double[] getCharsBrightnessLevel(Character[] charSet) {
        double[] brightnessLevel = new double[charSet.length];
        for (int i = 0; i < brightnessLevel.length; i++) {
            boolean[][] img = CharRenderer.getImg(charSet[i], 16, font);
            int totalTrues = 0;
            for (boolean[] row : img) {
                for (boolean col : row) {
                    if (col)
                        totalTrues++;
                }
            }
            brightnessLevel[i] = totalTrues / (16f * 16);
        }
        return brightnessLevel;
    }

    /*
        given an image, returning the average brightness of the pixels in the image.
     */
    private double getImageAverageBrightness(Image img) {
        if (cache.containsKey(img))
            return cache.get(img);
        if (img == null) return -1;
        double average = 0;
        double numOfPixels = 0;
        for (Color pixel : img.pixels()) {
            double greyPixel = pixel.getRed() * 0.2126 + pixel.getGreen() * 0.7152 + pixel.getBlue() * 0.0722;
            average += greyPixel / (255);
            numOfPixels++;
        }
        cache.put(img, average / numOfPixels);
        return cache.get(img);
    }

    /*
        building the given image of out of the given chars and their brightness level.
        making sure there are no more than numCharsInRow chars in a row.
     */
    private char[][] convertImageToAscii(int numCharsInRow, Image img,
                                         Character[] charSet, double[] charsBrightnessLevel) {
        if (img == null) return null;
        int pixels = img.getWidth() / numCharsInRow;
        char[][] asciiArt = new char[img.getHeight() / pixels][img.getWidth() / pixels];
        int row = 0, col = 0;
        for (Image subImage : img.squareSubImagesOfSize(pixels)) {
            double subImageBrightnessLevel = getImageAverageBrightness(subImage);
            asciiArt[row][col] =
                    getClosestCharBrightnessWise(subImageBrightnessLevel, charSet, charsBrightnessLevel);
            col++;
            if (col == img.getWidth() / pixels) {
                row++;
                col = 0;
            }
        }
        return asciiArt;
    }

    /*
        given an image (or a pixel) returning the char with the closest brightness level to the brightness
        level of that image out of all other given chars.
     */
    private char getClosestCharBrightnessWise(double subImageBrightnessLevel, Character[] charSet,
                                              double[] charsBrightnessLevel) {
        double delta = Double.MAX_VALUE;
        int deltaCharIndex = 0;
        for (int i = 0; i < charsBrightnessLevel.length; i++) {
            if (Math.abs(subImageBrightnessLevel - charsBrightnessLevel[i]) < delta) {
                delta = Math.abs(subImageBrightnessLevel - charsBrightnessLevel[i]);
                deltaCharIndex = i;
            }
        }
        return charSet[deltaCharIndex];
    }
}
