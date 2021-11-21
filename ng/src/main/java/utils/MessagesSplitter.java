package utils;

import java.util.ArrayList;
import java.util.List;

public class MessagesSplitter {

    public static List<String> getMaximumPossibleSizeSplittedMessagesList(final String input, int maximumPossibleSize) {
        final List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < input.length(); i = i + maximumPossibleSize) {
            final boolean answerLastPart = i + maximumPossibleSize > input.length();
            messagesList.add(input.substring(i, answerLastPart ? input.length() : i + maximumPossibleSize));
        }
        return messagesList;
    }
}
