import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

    public static void main(String[] args) {
        String s = "from-2:2#3:7#4:9#";
        String word = s.split("-")[0];
        String[] arr = s.split("-")[1].split("#");
        System.out.println(word);
        for (String value : arr) {
            System.out.println(value);
        }
    }
}
