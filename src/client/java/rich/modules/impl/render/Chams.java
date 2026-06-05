package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.c;

/**
 * Chams — рендер сквозь стены: тело, броня, предметы в руках.
 *
 * <p>RICH$EQUIPMENT_TARGET — флаг, который LivingEntityRendererMixin ставит в true
 * на время render() целевого игрока. Все миксины (броня, предметы)
 * читают этот флаг чтобы ограничить эффект только целевыми игроками.</p>
 */
public class Chams extends ModuleStructure {

    // Set true by LivingEntityRendererMixin while rendering a Chams target player.
    // All equipment mixins (armor, items) read this flag to scope the no-depth
    // (through-wall) layer only to Chams targets.
    public static volatile boolean RICH$EQUIPMENT_TARGET = false;

    /** Цвет тинта сквозь стены (ARGB, полупрозрачный красный по умолчанию). */
    public ColorSetting color = new ColorSetting("Color", "Through-wall tint color")
            .value(0x80FF3333);

    /** Показывать броню сквозь стены. */
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Show armor through walls")
            .setValue(true);

    /** Показывать предметы в руках сквозь стены. */
    public BooleanSetting showItems = new BooleanSetting("ShowItems", "Show held items through walls")
            .setValue(true);

    public static Chams getInstance() {
        return c.a(Chams.class);
    }

    public Chams() {
        super("Chams", "Render entity models through walls", ModuleCategory.VISUALS);
        this.settings(this.color, this.showArmor, this.showItems);
    }
}
