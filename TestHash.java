import at.favre.lib.crypto.bcrypt.BCrypt;
public class TestHash {
    public static void main(String[] args) {
        System.out.println(BCrypt.withDefaults().hashToString(10, "password123".toCharArray()));
    }
}
