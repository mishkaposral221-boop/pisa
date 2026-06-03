package rich.util.repository.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import rich.util.config.impl.staff.StaffConfig;

public final class StaffUtils {
   private static final List<Staff> staffList = new ArrayList<>();

   public static void addStaff(String var0) {
      if (!isStaff(var0)) {
         staffList.add(new Staff(var0));
      }
   }

   public static void addStaffAndSave(String var0) {
      addStaff(var0);
      StaffConfig.getInstance().save();
   }

   public static void removeStaff(String var0) {
      staffList.removeIf(var1 -> var1.getName().equalsIgnoreCase(var0));
   }

   public static void removeStaffAndSave(String var0) {
      removeStaff(var0);
      StaffConfig.getInstance().save();
   }

   public static boolean isStaff(Entity var0) {
      return var0 instanceof PlayerEntity var1 ? isStaff(var1.getName().getString()) : false;
   }

   public static boolean isStaff(String var0) {
      return staffList.stream().anyMatch(var1 -> var1.getName().equalsIgnoreCase(var0));
   }

   public static void clear() {
      staffList.clear();
   }

   public static void clearAndSave() {
      clear();
      StaffConfig.getInstance().save();
   }

   public static List<String> getStaffNames() {
      return staffList.stream().map(Staff::getName).collect(Collectors.toList());
   }

   public static int size() {
      return staffList.size();
   }

   public static void setStaff(List<String> var0) {
      staffList.clear();

      for (String var2 : var0) {
         staffList.add(new Staff(var2));
      }
   }

   private StaffUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static List<Staff> getStaffList() {
      return staffList;
   }
}
