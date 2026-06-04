package rich.modules.impl.render;

import java.util.ArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

public class Nametags extends ModuleStructure {
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Display player armor info").setValue(true);

    public Nametags() {
        super("Nametags", "Display player names above heads", ModuleCategory.VISUALS);
        this.settings(this.showArmor);
    }

    public static Nametags getInstance() {
        return c.a(Nametags.class);
    }

    public boolean showArmor() {
        return this.showArmor.isValue();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        try {
            this.renderInternal(event.getDrawContext(), event.getPartialTicks());
        } catch (Exception exception) {
            // ignore
        }
    }

    private static float[] worldToScreen(double wx, double wy, double wz, Vec3d camPos, Matrix4f viewMatrix, Matrix4f projMatrix, int sw, int sh) {
        float dx = (float)(wx - camPos.x);
        float dy = (float)(wy - camPos.y);
        float dz = (float)(wz - camPos.z);
        Vector4f pos = new Vector4f(dx, dy, dz, 1.0f);
        viewMatrix.transform(pos);
        projMatrix.transform(pos);
        if (pos.w() <= 0.001f) {
            return null;
        }
        float ndcX = pos.x() / pos.w();
        float ndcY = pos.y() / pos.w();
        float screenX = (ndcX + 1.0f) * 0.5f * (float)sw;
        float screenY = (-ndcY + 1.0f) * 0.5f * (float)sh;
        return new float[]{screenX, screenY};
    }

    private void renderInternal(DrawContext guiGraphics, float partialTick) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        Quaternionf quat = new Quaternionf((Quaternionfc)mc.gameRenderer.getCamera().getRotation()).conjugate();
        Matrix4f viewMatrix = new Matrix4f().rotation((Quaternionfc)quat);
        float fov = ((Integer)mc.options.getFov().getValue()).intValue();
        Matrix4f projMatrix = mc.gameRenderer.getBasicProjectionMatrix(fov);
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        TextRenderer font = mc.textRenderer;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity.isSpectator() || !(entity instanceof PlayerEntity)) continue;
            PlayerEntity target = (PlayerEntity)entity;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > 4096.0) continue;
            double lerpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double)partialTick;
            double lerpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double)partialTick;
            double lerpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double)partialTick;
            double headY = lerpY + (double)entity.getHeight() + 0.5;
            float[] screen = Nametags.worldToScreen(lerpX, headY, lerpZ, camPos, viewMatrix, projMatrix, sw, sh);
            if (screen == null) continue;
            float screenX = Math.round(screen[0]);
            float screenY = Math.round(screen[1]);
            float distance = (float)Math.sqrt(dist);
            float scale = Math.max(0.5f, Math.min(1.0f, 1.0f - distance / 20.0f));
            this.renderNametag(guiGraphics, font, target, screenX, screenY, scale, distance);
        }
    }

    private void renderNametag(DrawContext guiGraphics, TextRenderer font, PlayerEntity player, float cx, float cy, float scale, float distance) {
        float health = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        String name = player.getName().getString();
        String hpStr = String.format("%.0f", health);
        
        // Получаем пинг
        int ping = 0;
        try {
            net.minecraft.client.network.PlayerListEntry entry = mc.getNetworkHandler() != null
                    ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid())
                    : null;
            if (entry != null) {
                ping = entry.getLatency();
            }
        } catch (Exception e) {
            ping = 0;
        }
        
        // Собираем брон
        ArrayList<ItemStack> armorItems = new ArrayList<>();
        if (this.showArmor()) {
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
            ItemStack mainHand = player.getEquippedStack(EquipmentSlot.MAINHAND);
            ItemStack offHand = player.getEquippedStack(EquipmentSlot.OFFHAND);
            if (!helmet.isEmpty()) armorItems.add(helmet);
            if (!chest.isEmpty()) armorItems.add(chest);
            if (!legs.isEmpty()) armorItems.add(legs);
            if (!boots.isEmpty()) armorItems.add(boots);
            if (!mainHand.isEmpty()) armorItems.add(mainHand);
            if (!offHand.isEmpty()) armorItems.add(offHand);
        }
        
        // Расчёт размеров
        float hpPct = Math.min(1.0f, health / maxHp);
        int hpColor = hpPct > 0.6f ? 0xFF55FF55 : (hpPct > 0.3f ? 0xFFFFAA00 : 0xFFFF5555);
        int pingColor = ping < 80 ? 0xFF55FF55 : (ping < 150 ? 0xFFFFAA00 : 0xFFFF5555);
        
        MutableText nameText = Text.literal(name);
        MutableText hpText = Text.literal(" " + hpStr);
        MutableText pingText = Text.literal(" " + ping + "ms");
        
        int nameW = font.getWidth(nameText);
        int hpW = font.getWidth(hpText);
        int pingW = font.getWidth(pingText);
        int textRowW = nameW + hpW + pingW + 12; // +12 для иконки HP и отступа
        
        // Размеры брони
        float armorRowH = armorItems.isEmpty() ? 0 : 22; // 16px items + padding
        
        float bgPad = 4.0f;
        float bgW = Math.max((float)textRowW + bgPad * 2.0f, 50.0f);
        float bgH = 11 + 3; // текст + HP бар
        
        float bgX = -bgW / 2.0f;
        float bgY = -(armorRowH > 0 ? armorRowH + 2 : 0); // выравнивание если есть броня
        
        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(cx, cy);
        guiGraphics.getMatrices().scale(scale, scale);
        
        // === ВЕРХНИЙ РЯД: ИКОНКИ БРОНИ ===
        if (!armorItems.isEmpty()) {
            float totalArmorW = (float)armorItems.size() * 18.0f - 2.0f;
            float armorStartX = -totalArmorW / 2.0f;
            for (int i = 0; i < armorItems.size(); i++) {
                ItemStack item = armorItems.get(i);
                float itemX = armorStartX + (float)(i * 18);
                guiGraphics.drawItem(item, (int)itemX, (int)(bgY - 18));
                
                // Уровень зачарования над иконкой
                int maxEnchLvl = getMaxEnchantmentLevel(item);
                if (maxEnchLvl > 0) {
                    String enchStr = romanize(maxEnchLvl);
                    int enchW = font.getWidth(enchStr);
                    guiGraphics.drawText(font, enchStr, (int)(itemX + 8 - enchW / 2), (int)(bgY - 24), 0xFFFFFFFF, true);
                }
            }
        }
        
        // === ФОНОВЫЙ ПРЯМОУГОЛЬНИК ===
        // Основной фон (полупрозрачный)
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + bgH), 0xC0000000);
        // Тонкая цветная полосочка сверху (обводка/подсветка)
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + 1), 0xFF4080FF);
        
        // === СТРОКА ТЕКСТА ===
        int textY = (int)(bgY + 2);
        
        // Рассчитываем позицию для центрирования
        int totalTextW = 10 + nameW + hpW + pingW; // 10px сердечко + отступ + текст
        int textStartX = (int)(bgX + bgW / 2.0f - totalTextW / 2.0f);
        
        // HP иконка (сердечко, нарисованное вручную)
        this.drawHeart(guiGraphics, textStartX + 1, textY + 1, 0xFFFF5555);
        
        // Имя игрока
        guiGraphics.drawText(font, nameText, textStartX + 10, textY, 0xFFFFFFFF, true);
        
        // HP значение
        guiGraphics.drawText(font, hpText, textStartX + 10 + nameW, textY, hpColor, true);
        
        // Пинг
        guiGraphics.drawText(font, pingText, textStartX + 10 + nameW + hpW, textY, pingColor, true);
        
        // === HP БАР ===
        float barX = bgX + 2.0f;
        float barY = bgY + bgH - 2.5f;
        float barW = bgW - 4.0f;
        float barH = 2.0f;
        
        // Фон бара
        guiGraphics.fill((int)barX, (int)barY, (int)(barX + barW), (int)(barY + barH), 0x50000000);
        
        // Заполнение HP
        float fillW = barW * hpPct;
        if (fillW > 0.0f) {
            int barColor = hpPct > 0.6f ? 0xFF55FF55 : (hpPct > 0.3f ? 0xFFFFAA00 : 0xFFFF5555);
            guiGraphics.fill((int)barX, (int)barY, (int)(barX + fillW), (int)(barY + barH), barColor);
        }
        
        // Absorption (жёлтые сердца)
        if (absorption > 0.0f) {
            float absW = barW * Math.min(1.0f, absorption / maxHp);
            guiGraphics.fill((int)(barX + barW - absW), (int)barY, (int)(barX + barW), (int)(barY + barH), 0xFFFFAA00);
        }
        
        guiGraphics.getMatrices().popMatrix();
    }
    
    // Вспомогательный метод: получить макс. уровень зачарования
    private int getMaxEnchantmentLevel(ItemStack item) {
        try {
            ItemEnchantmentsComponent enchComp = item.get(DataComponentTypes.ENCHANTMENTS);
            if (enchComp == null) return 0;
            
            int maxLevel = 0;
            // Попробуем получить все зачарования итерацией
            try {
                for (RegistryEntry<Enchantment> ench : enchComp.getEnchantments()) {
                    int level = enchComp.getLevel(ench);
                    if (level > maxLevel) maxLevel = level;
                }
            } catch (Exception e2) {
                // Fallback: просто возвращаем 0, если API не работает
                return 0;
            }
            return maxLevel;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Конвертировать число в римские цифры
    private String romanize(int num) {
        if (num <= 0) return "";
        if (num == 1) return "I";
        if (num == 2) return "II";
        if (num == 3) return "III";
        if (num == 4) return "IV";
        if (num == 5) return "V";
        if (num == 6) return "VI";
        if (num == 7) return "VII";
        if (num == 8) return "VIII";
        if (num == 9) return "IX";
        if (num == 10) return "X";
        return String.valueOf(num); // Fallback для очень высоких уровней
    }
    
    // Рисует маленькое сердечко 7x6 пикселей вручную
    private void drawHeart(DrawContext g, int x, int y, int color) {
        g.fill(x + 1, y,     x + 3, y + 1, color); // верхняя левая часть
        g.fill(x + 4, y,     x + 6, y + 1, color); // верхняя правая часть
        g.fill(x,     y + 1, x + 7, y + 3, color); // широкая часть
        g.fill(x + 1, y + 3, x + 6, y + 4, color); // сужение
        g.fill(x + 2, y + 4, x + 5, y + 5, color); // ещё сужение
        g.fill(x + 3, y + 5, x + 4, y + 6, color); // острие
    }
}
