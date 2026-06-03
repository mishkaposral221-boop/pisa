package rich.modules.impl.misc;

import java.awt.Color;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;

public class ItemHelper extends ModuleStructure {
   private static ItemHelper instance;
   private final ColorSetting goldenApple;
   private final ColorSetting enchantedGoldenApple;
   private final ColorSetting totemOfUndying;
   private final ColorSetting enderPearl;
   private final ColorSetting experienceBottle;
   private final ColorSetting chorusFruit;
   private final ColorSetting enderEye;
   private final ColorSetting sugar;
   private final ColorSetting fireCharge;
   private final ColorSetting phantomMembrane;
   private final ColorSetting netheriteScrap;
   private final ColorSetting driedKelp;
   private final ColorSetting snowball;

   public ItemHelper() {
      super("ItemHelper", "Подсветка предметов", ModuleCategory.UTILITIES);
      instance = this;
      this.goldenApple = new ColorSetting("Золотое яблоко", "Цвет для золотого яблока").value(new Color(255, 215, 0, 100).getRGB());
      this.enchantedGoldenApple = new ColorSetting("Зачарованное золотое яблоко", "Цвет для зачарованного золотого яблока")
         .value(new Color(128, 0, 128, 100).getRGB());
      this.totemOfUndying = new ColorSetting("Тотем бессмертия", "Цвет для тотема бессмертия").value(new Color(255, 0, 0, 100).getRGB());
      this.enderPearl = new ColorSetting("Жемчуг Края", "Цвет для жемчуга Края").value(new Color(0, 0, 255, 100).getRGB());
      this.experienceBottle = new ColorSetting("Бутылочка опыта", "Цвет для бутылочки опыта").value(new Color(255, 255, 0, 100).getRGB());
      this.chorusFruit = new ColorSetting("Плод хоруса", "Цвет для плода хоруса").value(new Color(0, 255, 0, 100).getRGB());
      this.enderEye = new ColorSetting("Дезка (Глаз Края)", "Цвет для глаза Края").value(new Color(0, 200, 100, 100).getRGB());
      this.sugar = new ColorSetting("Явка (Сахар)", "Цвет для сахара").value(new Color(200, 200, 200, 100).getRGB());
      this.fireCharge = new ColorSetting("Огненный заряд", "Цвет для огненного заряда").value(new Color(255, 80, 0, 100).getRGB());
      this.phantomMembrane = new ColorSetting("Божья аура (Мембрана)", "Цвет для мембраны фантома").value(new Color(0, 200, 200, 100).getRGB());
      this.netheriteScrap = new ColorSetting("Трапка (Незеритовый лом)", "Цвет для незеритового лома").value(new Color(139, 69, 19, 100).getRGB());
      this.driedKelp = new ColorSetting("Пласт (Сушёная ламинария)", "Цвет для сушёной ламинарии").value(new Color(80, 80, 80, 100).getRGB());
      this.snowball = new ColorSetting("Снежок", "Цвет для снежка").value(new Color(160, 220, 255, 100).getRGB());
      this.settings(
         this.goldenApple,
         this.enchantedGoldenApple,
         this.totemOfUndying,
         this.enderPearl,
         this.experienceBottle,
         this.chorusFruit,
         this.enderEye,
         this.sugar,
         this.fireCharge,
         this.phantomMembrane,
         this.netheriteScrap,
         this.driedKelp,
         this.snowball
      );
   }

   public static ItemHelper getInstance() {
      return instance;
   }

   public ColorSetting getGoldenApple() {
      return this.goldenApple;
   }

   public ColorSetting getEnchantedGoldenApple() {
      return this.enchantedGoldenApple;
   }

   public ColorSetting getTotemOfUndying() {
      return this.totemOfUndying;
   }

   public ColorSetting getEnderPearl() {
      return this.enderPearl;
   }

   public ColorSetting getExperienceBottle() {
      return this.experienceBottle;
   }

   public ColorSetting getChorusFruit() {
      return this.chorusFruit;
   }

   public ColorSetting getEnderEye() {
      return this.enderEye;
   }

   public ColorSetting getSugar() {
      return this.sugar;
   }

   public ColorSetting getFireCharge() {
      return this.fireCharge;
   }

   public ColorSetting getPhantomMembrane() {
      return this.phantomMembrane;
   }

   public ColorSetting getNetheriteScrap() {
      return this.netheriteScrap;
   }

   public ColorSetting getDriedKelp() {
      return this.driedKelp;
   }

   public ColorSetting getSnowball() {
      return this.snowball;
   }
}
