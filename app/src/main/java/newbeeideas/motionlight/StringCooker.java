package newbeeideas.motionlight;

/**
 * Created by presisco on 2015/12/28.
 */
public class StringCooker {

    public static String deleteChSymbols(String original) {
        int length = original.length();
        String result = new String("");
        for (int i = 0; i < length; ++i) {
            Character.UnicodeBlock ub = Character.UnicodeBlock.of(original.charAt(i));
            if (ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
                continue;
            else
                result += String.valueOf(original.charAt(i));
        }
        return result;
    }
}
