package rich.util.window;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.glfw.GLFWNativeWin32;

public class WindowStyle {
   public static void setDarkMode(long var0) {
      String var2 = System.getProperty("os.name", "").toLowerCase();
      if (var2.contains("win")) {
         try {
            long var3 = GLFWNativeWin32.glfwGetWin32Window(var0);
            HWND var5 = new HWND(new Pointer(var3));
            byte var6 = 20;
            Memory var7 = new Memory(4L);
            var7.setInt(0L, 1);
            WindowStyle.DwmApi.INSTANCE.DwmSetWindowAttribute(var5, var6, var7, 4);
         } catch (Exception var8) {
            System.err.println("Warning: Could not set dark mode: " + var8.getMessage());
         }
      }
   }

   public interface DwmApi extends StdCallLibrary {
      WindowStyle.DwmApi INSTANCE = (WindowStyle.DwmApi)Native.load("dwmapi", WindowStyle.DwmApi.class);

      HRESULT DwmSetWindowAttribute(HWND var1, int var2, Pointer var3, int var4);
   }
}
