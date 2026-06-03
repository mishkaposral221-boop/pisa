package rich.util.modules.autobuy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_310;
import rich.util.string.chat.ChatMessage;

public class NetworkManager {
   private static final int PORT = 25566;
   private volatile ServerSocket serverSocket;
   private volatile Socket clientSocket;
   private final CopyOnWriteArrayList<NetworkManager.ClientHandler> clients = new CopyOnWriteArrayList<>();
   private volatile PrintWriter out;
   private volatile BufferedReader in;
   private final AtomicBoolean running = new AtomicBoolean(false);
   private final AtomicBoolean connected = new AtomicBoolean(false);
   private final AtomicBoolean stopping = new AtomicBoolean(false);
   private volatile ExecutorService executor;
   private final ConcurrentLinkedQueue<BuyRequest> buyQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<String> serverSwitchQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<Boolean> pauseQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<Boolean> updateQueue = new ConcurrentLinkedQueue<>();
   private final AtomicInteger clientsInAuction = new AtomicInteger(0);

   public void startAsServer() {
      this.stop();
      this.sleep(300L);
      this.running.set(true);
      this.stopping.set(false);
      this.clients.clear();
      this.clientsInAuction.set(0);
      this.executor = Executors.newCachedThreadPool();
      this.executor.execute(() -> {
         int var1 = 0;

         while (this.running.get() && this.serverSocket == null && var1 < 5) {
            try {
               ServerSocket var2 = new ServerSocket();
               var2.setReuseAddress(true);
               var2.bind(new InetSocketAddress(25566));
               var2.setSoTimeout(1000);
               this.serverSocket = var2;
               this.msg("§a[ПОКУПАТЕЛЬ] Сервер запущен на порту 25566");
            } catch (IOException var7) {
               if (++var1 >= 5) {
                  this.msg("§c[ПОКУПАТЕЛЬ] Не удалось запустить сервер");
                  return;
               }

               this.msg("§e[ПОКУПАТЕЛЬ] Порт занят, попытка " + var1 + "/5...");
               this.sleep(1000L);
            }
         }

         while (this.running.get() && !this.stopping.get()) {
            ServerSocket var8 = this.serverSocket;
            if (var8 == null || var8.isClosed()) {
               break;
            }

            try {
               Socket var3 = var8.accept();
               var3.setTcpNoDelay(true);
               var3.setKeepAlive(true);
               var3.setSoTimeout(5000);
               NetworkManager.ClientHandler var4 = new NetworkManager.ClientHandler(var3);
               this.clients.add(var4);
               this.connected.set(true);
               this.msg("§a[ПОКУПАТЕЛЬ] Проверяющий #" + this.clients.size() + " подключился!");
               this.executor.execute(() -> this.handleClient(var4));
            } catch (SocketTimeoutException var5) {
            } catch (IOException var6) {
               if (this.running.get() && !this.stopping.get()) {
                  this.sleep(100L);
               }
            }
         }
      });
   }

   private void handleClient(NetworkManager.ClientHandler var1) {
      try {
         while (this.running.get() && !this.stopping.get() && !var1.closed) {
            String var2;
            try {
               var2 = var1.in.readLine();
            } catch (SocketTimeoutException var8) {
               continue;
            }

            if (var2 == null) {
               break;
            }

            this.processServerMessage(var2, var1);
         }
      } catch (IOException var9) {
      } finally {
         if (var1.inAuction) {
            this.clientsInAuction.decrementAndGet();
         }

         var1.close();
         this.clients.remove(var1);
         this.updateConnectedState();
         if (this.running.get() && !this.stopping.get()) {
            this.msg("§c[ПОКУПАТЕЛЬ] Проверяющий отключился. Осталось: " + this.clients.size());
         }
      }
   }

   private void updateConnectedState() {
      this.connected.set(!this.clients.isEmpty());
   }

   public void startAsClient() {
      this.stop();
      this.sleep(300L);
      this.running.set(true);
      this.stopping.set(false);
      this.executor = Executors.newCachedThreadPool();
      this.executor.execute(() -> {
         while (this.running.get() && !this.stopping.get()) {
            if (!this.connected.get()) {
               try {
                  Socket var1 = new Socket();
                  var1.connect(new InetSocketAddress("localhost", 25566), 2000);
                  var1.setTcpNoDelay(true);
                  var1.setKeepAlive(true);
                  var1.setSoTimeout(5000);
                  this.clientSocket = var1;
                  this.out = new PrintWriter(var1.getOutputStream(), true);
                  this.in = new BufferedReader(new InputStreamReader(var1.getInputStream()));
                  this.connected.set(true);
                  this.msg("§a[ПРОВЕРЯЮЩИЙ] Подключился к покупателю!");
                  this.clientReadLoop();
               } catch (IOException var2) {
                  this.connected.set(false);
               }
            }

            this.sleep(2000L);
         }
      });
   }

   private void clientReadLoop() {
      try {
         while (this.running.get() && this.connected.get() && !this.stopping.get()) {
            BufferedReader var1 = this.in;
            if (var1 != null) {
               String var2;
               try {
                  var2 = var1.readLine();
               } catch (SocketTimeoutException var8) {
                  continue;
               }

               if (var2 != null) {
                  this.processClientMessage(var2);
                  continue;
               }
            }
            break;
         }
      } catch (IOException var9) {
      } finally {
         this.connected.set(false);
         if (this.running.get() && !this.stopping.get()) {
            this.msg("§c[ПРОВЕРЯЮЩИЙ] Соединение потеряно");
         }

         this.closeClientSocket();
      }
   }

   private void processServerMessage(String var1, NetworkManager.ClientHandler var2) {
      if (var1.startsWith("BUY:")) {
         try {
            String var3 = var1.substring(4);
            String[] var4 = var3.split("\\|\\|\\|");
            if (var4.length == 7) {
               int var5 = Integer.parseInt(var4[0]);
               String var6 = var4[1];
               String var7 = var4[2];
               int var8 = Integer.parseInt(var4[3]);
               String var9 = var4[4];
               int var10 = Integer.parseInt(var4[5]);
               int var11 = Integer.parseInt(var4[6]);
               this.buyQueue.add(new BuyRequest(var5, var6, var7, var8, var9, var10, var11));
            }
         } catch (Exception var12) {
         }
      } else if (var1.equals("ENTER_AUCTION")) {
         if (!var2.inAuction) {
            var2.inAuction = true;
            this.clientsInAuction.incrementAndGet();
         }
      } else if (var1.equals("LEAVE_AUCTION")) {
         if (var2.inAuction) {
            var2.inAuction = false;
            this.clientsInAuction.decrementAndGet();
         }
      } else if (var1.equals("PAUSE:true")) {
         this.pauseQueue.add(true);
      } else if (var1.equals("PAUSE:false")) {
         this.pauseQueue.add(false);
      }
   }

   private void processClientMessage(String var1) {
      if (var1.startsWith("SWITCH:")) {
         String var2 = var1.substring(7);
         this.serverSwitchQueue.add(var2);
      } else if (var1.equals("PAUSE:true")) {
         this.pauseQueue.add(true);
      } else if (var1.equals("PAUSE:false")) {
         this.pauseQueue.add(false);
      } else if (var1.equals("UPDATE")) {
         this.updateQueue.add(true);
      }
   }

   public void sendUpdateCommand() {
      for (NetworkManager.ClientHandler var2 : this.clients) {
         if (var2.inAuction && !var2.closed) {
            var2.send("UPDATE");
         }
      }
   }

   public boolean pollUpdateCommand() {
      return this.updateQueue.poll() != null;
   }

   public int getClientsInAuctionCount() {
      return this.clientsInAuction.get();
   }

   public void sendBuyCommand(int var1, String var2, String var3, int var4, String var5, int var6, int var7) {
      if (this.connected.get() && this.out != null) {
         try {
            this.out.println("BUY:" + var1 + "|||" + var2 + "|||" + var3 + "|||" + var4 + "|||" + var5 + "|||" + var6 + "|||" + var7);
            this.out.flush();
         } catch (Exception var9) {
         }
      }
   }

   public void sendServerSwitch(String var1) {
      for (NetworkManager.ClientHandler var3 : this.clients) {
         var3.send("SWITCH:" + var1);
      }
   }

   public void sendPauseState(boolean var1) {
      String var2 = "PAUSE:" + var1;
      if (this.out != null) {
         try {
            this.out.println(var2);
            this.out.flush();
         } catch (Exception var5) {
         }
      }

      for (NetworkManager.ClientHandler var4 : this.clients) {
         var4.send(var2);
      }
   }

   public void sendEnterAuction() {
      if (this.connected.get() && this.out != null) {
         try {
            this.out.println("ENTER_AUCTION");
            this.out.flush();
         } catch (Exception var2) {
         }
      }
   }

   public void sendLeaveAuction() {
      if (this.connected.get() && this.out != null) {
         try {
            this.out.println("LEAVE_AUCTION");
            this.out.flush();
         } catch (Exception var2) {
         }
      }
   }

   public BuyRequest pollBuyRequest() {
      return this.buyQueue.poll();
   }

   public String pollServerSwitch() {
      return this.serverSwitchQueue.poll();
   }

   public Boolean pollPauseState() {
      return this.pauseQueue.poll();
   }

   public boolean isConnected() {
      return this.connected.get();
   }

   public int getConnectedClientCount() {
      return this.clients.size();
   }

   public boolean isConnectedToServer() {
      return this.connected.get() && this.clientSocket != null;
   }

   public boolean isServerRunning() {
      return this.serverSocket != null && !this.serverSocket.isClosed();
   }

   private void closeClientSocket() {
      PrintWriter var1 = this.out;
      BufferedReader var2 = this.in;
      Socket var3 = this.clientSocket;
      this.out = null;
      this.in = null;
      this.clientSocket = null;

      try {
         if (var2 != null) {
            var2.close();
         }
      } catch (Exception var7) {
      }

      try {
         if (var1 != null) {
            var1.close();
         }
      } catch (Exception var6) {
      }

      try {
         if (var3 != null) {
            var3.close();
         }
      } catch (Exception var5) {
      }
   }

   private void closeServerSocket() {
      ServerSocket var1 = this.serverSocket;
      this.serverSocket = null;
      if (var1 != null) {
         try {
            var1.close();
         } catch (Exception var3) {
         }
      }
   }

   public void stop() {
      this.stopping.set(true);
      this.running.set(false);
      this.connected.set(false);
      this.clientsInAuction.set(0);
      this.buyQueue.clear();
      this.serverSwitchQueue.clear();
      this.pauseQueue.clear();
      this.updateQueue.clear();

      for (NetworkManager.ClientHandler var2 : this.clients) {
         var2.close();
      }

      this.clients.clear();
      this.closeClientSocket();
      this.closeServerSocket();
      ExecutorService var4 = this.executor;
      this.executor = null;
      if (var4 != null) {
         var4.shutdownNow();

         try {
            var4.awaitTermination(500L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var3) {
         }
      }
   }

   private void msg(String var1) {
      class_310 var2 = class_310.method_1551();
      if (var2 != null && var2.field_1724 != null) {
         var2.execute(() -> ChatMessage.autobuymessage(var1));
      }
   }

   private void sleep(long var1) {
      try {
         Thread.sleep(var1);
      } catch (InterruptedException var4) {
      }
   }

   private static class ClientHandler {
      final Socket socket;
      final PrintWriter out;
      final BufferedReader in;
      volatile boolean inAuction = false;
      volatile boolean closed = false;

      ClientHandler(Socket var1) throws IOException {
         this.socket = var1;
         this.out = new PrintWriter(var1.getOutputStream(), true);
         this.in = new BufferedReader(new InputStreamReader(var1.getInputStream()));
      }

      void close() {
         if (!this.closed) {
            this.closed = true;

            try {
               this.in.close();
            } catch (Exception var4) {
            }

            try {
               this.out.close();
            } catch (Exception var3) {
            }

            try {
               this.socket.close();
            } catch (Exception var2) {
            }
         }
      }

      void send(String var1) {
         if (!this.closed && this.out != null) {
            try {
               this.out.println(var1);
               this.out.flush();
            } catch (Exception var3) {
               this.closed = true;
            }
         }
      }
   }
}
