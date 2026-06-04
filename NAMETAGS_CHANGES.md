# Инструкции для компиляции Nametags.java (Updated)

## Быстрая проверка

Откройте Command Prompt в корне проекта:
```
cd /d "C:\Users\awepu\OneDrive\Desktop\Новая папка (5)"
```

Затем запустите:
```
gradlew.bat compileClientJava
```

## Что было изменено

### Файл: src/client/java/rich/modules/impl/render/Nametags.java

**Основные улучшения:**

1. **Иконки брони сверху**
   - Отображаются в горизонтальном ряду
   - Уровни зачарований (римские цифры I-X) над каждой иконкой
   - Используется API: `DataComponentTypes.ENCHANTMENTS`

2. **Сердечко вместо Unicode**
   - Старый код: `guiGraphics.drawText(font, "❤", ...)`
   - Новый код: `drawHeart(guiGraphics, x, y, color)` 
   - Рисует вручную через 6 fill() вызовов (7x6 пикселей)

3. **Центрирование текста**
   - Рассчитывается от центра плашки
   - Позиция: `bgX + bgW/2 - totalWidth/2`

4. **Стилизация**
   - Фон: полупрозрачный чёрный (0xC0000000)
   - Верхняя обводка: синяя (0xFF4080FF)
   - Цвета HP и пинга: зелёный/жёлтый/красный по порогам

5. **Health Bar**
   - Тонкая полоса (2px) под текстом
   - Заполнение пропорционально health/maxHealth
   - Жёлтый сегмент справа для absorption

## Ожидаемый результат компиляции

```
> Task :compileClientJava
BUILD SUCCESSFUL in XXs
```

Если возникнут ошибки, скопируйте вывод из консоли для отладки.

## Используемый API (1.21.11)

- `EnchantmentsComponent.getEnchantments()` - получить список зачарований
- `EnchantmentsComponent.getLevel(RegistryEntry)` - уровень конкретного зачарования
- `DrawContext.fill()` - рисование прямоугольников
- `DrawContext.drawText()` - рисование текста с тенью
- `DrawContext.drawItem()` - рисование иконок предметов
