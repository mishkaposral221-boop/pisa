package rich.screens.clickgui.impl.module.handler;

import org.lwjgl.glfw.GLFW;

public class ModuleBindHandler {
   public String getBindDisplayName(int var1) {
      if (var1 == -1 || var1 == -1) {
         return "";
      }

      if (var1 == 1000) {
         return "Up";
      }

      if (var1 == 1001) {
         return "Dn";
      }

      if (var1 == 1002) {
         return "M3";
      }

      String var2 = GLFW.glfwGetKeyName(var1, 0);
      if (var2 != null) {
         return var2.toUpperCase();
      }

      return switch (var1) {
         case 32 -> "Sp";
         case 256 -> "Esc";
         case 257 -> "Ent";
         case 258 -> "Tab";
         case 259 -> "Bk";
         case 260 -> "Ins";
         case 261 -> "Del";
         case 262 -> "Rt";
         case 263 -> "Lt";
         case 264 -> "Dn";
         case 265 -> "Up";
         case 266 -> "PU";
         case 267 -> "PD";
         case 268 -> "Hm";
         case 269 -> "End";
         case 280 -> "Cap";
         case 290 -> "F1";
         case 291 -> "F2";
         case 292 -> "F3";
         case 293 -> "F4";
         case 294 -> "F5";
         case 295 -> "F6";
         case 296 -> "F7";
         case 297 -> "F8";
         case 298 -> "F9";
         case 299 -> "F10";
         case 300 -> "F11";
         case 301 -> "F12";
         case 340 -> "LS";
         case 341 -> "LC";
         case 342 -> "LA";
         case 344 -> "RS";
         case 345 -> "RC";
         case 346 -> "RA";
         default -> "K" + var1;
      };
   }
}
