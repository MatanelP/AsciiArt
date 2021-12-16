package ascii_art;

import java.util.HashMap;

public class Algorithms {
    public static int findDuplicate(int[] numList) {
        int turtle = numList[0];
        int rabbit = numList[0];
        do {
            turtle = numList[turtle];
            rabbit = numList[numList[rabbit]];
        } while (turtle != rabbit);
        turtle = numList[0];
        while (turtle != rabbit) {
            turtle = numList[turtle];
            rabbit = numList[rabbit];
        }
        return turtle;
    }

    public static int uniqueMorseRepresentations(String[] words) {
        String[] morse = new String[]
                {".-", "-...", "-.-.", "-..", ".", "..-.", "--.",
                        "....", "..", ".---", "-.-", ".-..", "--", "-.",
                        "---", ".--.", "--.-", ".-.", "...", "-", "..-",
                        "...-", ".--", "-..-", "-.--", "--.."};
        HashMap<String, String> wordToMorse = new HashMap<>();
        for (String word : words) {
            StringBuilder wordMorse = new StringBuilder();
            for (char letter : word.toCharArray())
                wordMorse.append(morse[letter - 97]);
            if (!wordToMorse.containsValue(wordMorse.toString()))
                wordToMorse.put(word, wordMorse.toString());
        }
        return wordToMorse.size();
    }
}
