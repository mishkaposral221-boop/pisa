package rich.util.config.impl.consolelogger;

public class Logger {
   private static final String RESET = "\u001b[0m";
   private static final String GREEN_BG = "\u001b[42m";
   private static final String RED_BG = "\u001b[41m";
   private static final String BLACK = "\u001b[30m";
   private static final String WHITE = "\u001b[97m";
   private static final String BOLD = "\u001b[1m";

   public static void success(String var0) {
      System.out.println("\u001b[42m\u001b[30m\u001b[1m " + var0 + " \u001b[0m");
   }

   public static void error(String var0) {
      System.out.println("\u001b[41m\u001b[97m\u001b[1m " + var0 + " \u001b[0m");
   }

   public static void info(String var0) {
      System.out.println("\u001b[44m\u001b[97m\u001b[1m " + var0 + " \u001b[0m");
   }
}
