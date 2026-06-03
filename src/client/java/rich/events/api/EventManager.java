package rich.events.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import rich.events.api.events.Event;
import rich.events.api.events.EventStoppable;
import rich.events.api.types.Priority;

public final class EventManager {
   private static final Map<Class<? extends Event>, List<EventManager.MethodData>> REGISTRY_MAP = new HashMap<>();

   public static void register(Object var0) {
      for (Method var4 : var0.getClass().getDeclaredMethods()) {
         if (!isMethodBad(var4)) {
            register(var4, var0);
         }
      }
   }

   public static void register(Object var0, Class<? extends Event> var1) {
      for (Method var5 : var0.getClass().getDeclaredMethods()) {
         if (!isMethodBad(var5, var1)) {
            register(var5, var0);
         }
      }
   }

   public static void unregister(Object var0) {
      for (List<MethodData> var2 : REGISTRY_MAP.values()) {
         var2.removeIf(var1 -> var1.source().equals(var0));
      }

      cleanMap(true);
   }

   public static void unregister(Object var0, Class<? extends Event> var1) {
      if (REGISTRY_MAP.containsKey(var1)) {
         REGISTRY_MAP.get(var1).removeIf(var1x -> var1x.source().equals(var0));
         cleanMap(true);
      }
   }

   private static void register(Method var0, Object var1) {
      Class var2 = var0.getParameterTypes()[0];
      EventManager.MethodData var3 = new EventManager.MethodData(var1, var0, var0.getAnnotation(EventHandler.class).value());
      if (!var3.target().canAccess(var3.source())) {
         var3.target().setAccessible(true);
      }

      if (REGISTRY_MAP.containsKey(var2)) {
         if (!REGISTRY_MAP.get(var2).contains(var3)) {
            REGISTRY_MAP.get(var2).add(var3);
            sortListValue(var2);
         }
      } else {
         REGISTRY_MAP.put(var2, new CopyOnWriteArrayList<>());
         REGISTRY_MAP.get(var2).add(var3);
      }
   }

   public static void removeEntry(Class<? extends Event> var0) {
      REGISTRY_MAP.entrySet().removeIf(var1 -> var1.getKey().equals(var0));
   }

   public static void cleanMap(boolean var0) {
      if (var0) {
         REGISTRY_MAP.entrySet().removeIf(var0x -> var0x.getValue().isEmpty());
      } else {
         REGISTRY_MAP.clear();
      }
   }

   private static void sortListValue(Class<? extends Event> var0) {
      CopyOnWriteArrayList<MethodData> var1 = new CopyOnWriteArrayList<>();

      for (byte var5 : Priority.VALUE_ARRAY) {
         for (EventManager.MethodData var7 : REGISTRY_MAP.get(var0)) {
            if (var7.priority() == var5) {
               var1.add(var7);
            }
         }
      }

      REGISTRY_MAP.put(var0, var1);
   }

   private static boolean isMethodBad(Method var0) {
      return var0.getParameterTypes().length != 1 || !var0.isAnnotationPresent(EventHandler.class);
   }

   private static boolean isMethodBad(Method var0, Class<? extends Event> var1) {
      return isMethodBad(var0) || !var0.getParameterTypes()[0].equals(var1);
   }

   public static Event callEvent(Event var0) {
      List<MethodData> var1 = REGISTRY_MAP.get(var0.getClass());
      if (var1 != null) {
         if (var0 instanceof EventStoppable var2) {
            for (EventManager.MethodData var4 : var1) {
               invoke(var4, var0);
               if (var2.isStopped()) {
                  break;
               }
            }
         } else {
            for (EventManager.MethodData var8 : var1) {
               try {
                  invoke(var8, var0);
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
            }
         }
      }

      return var0;
   }

   private static void invoke(EventManager.MethodData var0, Event var1) {
      try {
         var0.target().invoke(var0.source(), var1);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException var3) {
         if (Boolean.getBoolean("rich.debug.events")) {
            System.err.println("Event invocation failed: " + var3.getClass().getSimpleName());
         }
      }
   }

   private record MethodData(Object source, Method target, byte priority) {
   }
}
