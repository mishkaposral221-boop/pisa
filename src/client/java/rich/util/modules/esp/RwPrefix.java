package rich.util.modules.esp;

public class RwPrefix {
   public static String getIconLabel(char var0) {
      return switch (var0) {
         case 'ꔀ' -> "PLAYER";
         case 'ꔁ' -> "MEDIA";
         default -> null;
         case 'ꔄ' -> "HERO";
         case 'ꔅ' -> "YT";
         case 'ꔈ' -> "TITAN";
         case 'ꔉ' -> "HELPER";
         case 'ꔒ' -> "AVENGER";
         case 'ꔓ' -> "ML.MODER";
         case 'ꔖ' -> "OVERLORD";
         case 'ꔗ' -> "MODER";
         case 'ꔠ' -> "MAGISTER";
         case 'ꔡ' -> "MODER+";
         case 'ꔤ' -> "IMPERATOR";
         case 'ꔥ' -> "ST.MODER";
         case 'ꔨ' -> "DRAGON";
         case 'ꔩ' -> "GL.MODER";
         case 'ꔲ' -> "BULL";
         case 'ꔳ' -> "ML.ADMIN";
         case 'ꔶ' -> "TIGER";
         case 'ꔷ' -> "ADMIN";
         case 'ꕀ' -> "HYDRA";
         case 'ꕄ' -> "DRACULA";
         case 'ꕅ' -> "VAMPIRE";
         case 'ꕈ' -> "COBRA";
         case 'ꕉ' -> "PEGAS";
         case 'ꕒ' -> "RABBIT";
         case 'ꕖ' -> "BUNNY";
         case 'ꕠ' -> "D.HELPER";
      };
   }

   public static boolean isIcon(char var0) {
      return getIconLabel(var0) != null
         ? true
         : var0 >= 'ꀀ' && var0 <= '꿿' || var0 >= '\ue000' && var0 <= '\uf8ff' || var0 >= 9216 && var0 <= 9279 || var0 >= 9472 && var0 <= 9599;
   }

   public static String stripFormatting(String var0) {
      if (var0 == null) {
         return "";
      }

      StringBuilder var1 = new StringBuilder();

      for (int var2 = 0; var2 < var0.length(); var2++) {
         char var3 = var0.charAt(var2);
         if (var3 == 167 && var2 + 1 < var0.length()) {
            char var4 = var0.charAt(var2 + 1);
            if (var4 == '#' && var2 + 7 < var0.length()) {
               var2 += 7;
            } else if ((var4 == 'x' || var4 == 'X') && var2 + 13 < var0.length()) {
               var2 += 13;
            } else {
               var2++;
            }
         } else {
            var1.append(var3);
         }
      }

      return var1.toString();
   }

   public static RwPrefix.ParsedName parseDisplayName(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String var1 = stripFormatting(var0);
         StringBuilder var2 = new StringBuilder();
         StringBuilder var3 = new StringBuilder();
         StringBuilder var4 = new StringBuilder();
         boolean var5 = false;
         boolean var6 = false;
         int var7 = 0;

         for (int var8 = 0; var8 < var1.length(); var8++) {
            char var9 = var1.charAt(var8);
            if (isIcon(var9)) {
               String var10 = getIconLabel(var9);
               if (var10 != null) {
                  if (var2.length() > 0) {
                     var2.append(" ");
                  }

                  var2.append(var10);
               }
            } else if (var5 || var9 != ' ' && var9 != '[' && var9 != ']') {
               if (!var5 && Character.isLetterOrDigit(var9) || var9 == '_') {
                  var5 = true;
               }

               if (var5) {
                  if (var9 == '[') {
                     var6 = true;
                     var7++;
                     var4.append(var9);
                  } else if (var9 == ']' && var6) {
                     var4.append(var9);
                     if (--var7 <= 0) {
                        var6 = false;
                     }
                  } else if (var6) {
                     var4.append(var9);
                  } else if ((var9 != ' ' || var3.length() > 0)
                     && (var9 != ' ' || var8 + 1 >= var1.length() || var1.charAt(var8 + 1) != '[')
                     && !var6
                     && var4.length() == 0) {
                     var3.append(var9);
                  }
               }
            }
         }

         String var11 = var3.toString().trim();
         if (var11.contains(" ")) {
            int var12 = var11.indexOf(32);
            String var13 = var11.substring(var12).trim();
            if (var13.startsWith("[") && var13.endsWith("]")) {
               var4 = new StringBuilder(var13);
               var11 = var11.substring(0, var12);
            }
         }

         return new RwPrefix.ParsedName(var2.toString().trim(), var11.trim(), var4.toString().trim());
      } else {
         return new RwPrefix.ParsedName("", "", "");
      }
   }

   public static class ParsedName {
      public final String prefix;
      public final String name;
      public final String clan;

      public ParsedName(String var1, String var2, String var3) {
         this.prefix = var1;
         this.name = var2;
         this.clan = var3;
      }

      public String getFullText() {
         StringBuilder var1 = new StringBuilder();
         if (!this.prefix.isEmpty()) {
            var1.append(this.prefix).append(" ");
         }

         var1.append(this.name);
         if (!this.clan.isEmpty()) {
            var1.append(" ").append(this.clan);
         }

         return var1.toString();
      }
   }
}
