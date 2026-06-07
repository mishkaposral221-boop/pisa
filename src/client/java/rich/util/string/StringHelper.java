package rich.util.string;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class StringHelper {
   public static String randomString(int var0) {
      return IntStream.range(0, var0).mapToObj(var0x -> String.valueOf((char)new Random().nextInt(97, 123))).collect(Collectors.joining());
   }

   public static String getBindName(int var0) {
      return var0 < 0
         ? "N/A"
         : PlayerInteractionHelper.getKeyType(var0)
            .createFromCode(var0)
            .getTranslationKey()
            .replace("key.keyboard.", "")
            .replace("key.mouse.", "mouse ")
            .replace(".", " ")
            .toUpperCase();
   }

   public static String getUserRole() {
      return switch ("DEVELOPER") {
         case "Разработчик" -> "Developer";
         case "Администратор" -> "Admin";
         default -> "User";
      };
   }

   public static String getDuration(int var0) {
      int var1 = var0 / 60;
      String var2 = String.format("%02d", var0 % 60);
      return var1 + ":" + var2;
   }

   private StringHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
