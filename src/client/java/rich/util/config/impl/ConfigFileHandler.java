package rich.util.config.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import rich.util.config.impl.consolelogger.Logger;

public class ConfigFileHandler {
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   public void createDirectories() {
      try {
         Files.createDirectories(ConfigPath.getConfigDirectory());
      } catch (IOException var2) {
         Logger.error("AutoConfiguration: Failed to create directories!");
      }
   }

   public boolean write(String var1) {
      this.lock.writeLock().lock();

      try {
         Path var2 = ConfigPath.getConfigFile();
         Path var3 = var2.resolveSibling(var2.getFileName() + ".tmp");
         Files.writeString(var3, var1, StandardCharsets.UTF_8);
         Files.move(var3, var2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         return true;
      } catch (IOException var8) {
         Logger.error("AutoConfiguration: Write failed! " + var8.getMessage());
         return false;
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   public String read() {
      this.lock.readLock().lock();

      try {
         Path var1 = ConfigPath.getConfigFile();
         return !Files.exists(var1) ? null : Files.readString(var1, StandardCharsets.UTF_8);
      } catch (IOException var6) {
         Logger.error("AutoConfiguration: Read failed! " + var6.getMessage());
         return null;
      } finally {
         this.lock.readLock().unlock();
      }
   }

   public boolean exists() {
      return Files.exists(ConfigPath.getConfigFile());
   }
}
