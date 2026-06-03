package rich.util.repository.friend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import rich.util.config.impl.friend.FriendConfig;

public final class FriendUtils {
   private static final List<Friend> friends = new ArrayList<>();

   public static void addFriend(class_1657 var0) {
      addFriend(var0.method_5477().getString());
   }

   public static void addFriend(String var0) {
      if (!isFriend(var0)) {
         friends.add(new Friend(var0));
      }
   }

   public static void addFriendAndSave(String var0) {
      addFriend(var0);
      FriendConfig.getInstance().save();
   }

   public static void removeFriend(class_1657 var0) {
      removeFriend(var0.method_5477().getString());
   }

   public static void removeFriend(String var0) {
      friends.removeIf(var1 -> var1.getName().equalsIgnoreCase(var0));
   }

   public static void removeFriendAndSave(String var0) {
      removeFriend(var0);
      FriendConfig.getInstance().save();
   }

   public static boolean isFriend(class_1297 var0) {
      return var0 instanceof class_1657 var1 ? isFriend(var1.method_5477().getString()) : false;
   }

   public static boolean isFriend(String var0) {
      return friends.stream().anyMatch(var1 -> var1.getName().equalsIgnoreCase(var0));
   }

   public static void clear() {
      friends.clear();
   }

   public static void clearAndSave() {
      clear();
      FriendConfig.getInstance().save();
   }

   public static List<String> getFriendNames() {
      return friends.stream().map(Friend::getName).collect(Collectors.toList());
   }

   public static int size() {
      return friends.size();
   }

   public static void setFriends(List<String> var0) {
      friends.clear();

      for (String var2 : var0) {
         friends.add(new Friend(var2));
      }
   }

   private FriendUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static List<Friend> getFriends() {
      return friends;
   }
}
