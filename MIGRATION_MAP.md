# Карта переноса com.example → rich (до массовых правок)

## (a) Счётчики

| Источник | Файлов | Список |
|----------|--------|--------|
| `rich.*` + `antidaunleak.*` | 455 | `MIGRATION_rich_classes.txt` |
| `com.example.*` | 50 | `MIGRATION_example_classes.txt` |

---

## (b) Карта переноса com.example → rich

### Entrypoints / инициализация

| com.example | rich-аналог | Действие (Фаза 2) |
|-------------|-------------|-------------------|
| `ExampleModClient` | `rich.Initialization` | Логику `ModMenuInitializer.setup()` + keybind в `Initialization.onInitializeClient()` / `Manager` |
| `ModMenuInitializer` | `Manager`, `RenderHudCallback` в rich | CometRenderer-колбэки → интегрировать в `rich.util.render` / события rich; не дублировать CRM |
| `ExampleMod` (main) | — | Удалить (client-only mod) |

### Config

| com.example | rich-аналог | Действие |
|-------------|-------------|----------|
| `ConfigManager` | `rich.util.config.*`, `ConfigAutoSaver` | Перенести путь на `FabricLoader.getConfigDir()`; слить с `*Config.java` |

### Account

| com.example | rich-аналог | Действие |
|-------------|-------------|----------|
| `AccountSwitcherScreen` | `rich.screens.account.*` | Уникальный UI → дополнить `AccountRenderer` / новый экран |

### HUD

| com.example | rich-аналог | Действие |
|-------------|-------------|----------|
| `HudManager` | `rich.client.draggables`, `rich.screens.hud.*` | Виджеты → `AbstractHudElement` / новые HUD-модули |
| `HudWidget` | `AbstractHudElement` | Базовый класс rich |
| `ActiveModsWidget` | — | Новый draggable / HUD module |
| `TopBarWidget`, `InfoBarWidget`, `PotionsWidget`, `HotkeysWidget` | `Watermark`, `Info`, `Potions`, `HotKeys` | Слить логику в существующие или расширить |
| `TargetHudWidget` | `TargetHud`, `TargetHudModule` | Слить |

### Menu (ClickGUI)

| com.example | rich-аналог | Действие |
|-------------|-------------|----------|
| `ModMenu` | `rich.screens.clickgui.ClickGui` | Keybind + open state в ClickGui |
| `MenuScreen` | `ClickGui` | Не дублировать Screen |
| `MenuRenderer`, `CometBatchUtil` | `rich.util.render.*`, pipelines | **CometRenderer** → заменить на rich shader pipeline где возможно; иначе оставить CRM |
| `MenuTextRenderer`, `ColorPickerRenderer`, `MenuInputHandler`, `AnimationController` | `*Component` в clickgui | Слить уникальное |
| `MenuLayout`, `MenuColors`, `Easing`, `ColorUtil` | theme + animations | Перенести утилиты в `rich.theme` / `rich.util.animations` |
| `MenuData`, `Category`, `ModModule`, `Setting` | `ModuleRepository`, `ModuleStructure`, `Setting` | **Не дублировать** — привязать модули к rich.modules |

### Modules (функции)

| com.example | rich-аналог | Действие |
|-------------|-------------|----------|
| `AimAssist`, `AimEngine` | `rich.modules.impl.combat.aura.*` | Слить настройки в aura, не новый класс |
| `Triggerbot` | combat modules | Новый `ModuleStructure` или расширение |
| `AutoSprint` | `rich.modules.impl.movement.AutoSprint` | Слить логику |
| `Fullbright` | `rich.modules.impl.render.FullBright` | Слить |
| `AspectRatio` | `CameraSettings`? | Проверить + слить |
| `NoRender` | несколько render modules | Разбить по NoRender-флагам rich |
| `GlowESP`, `Nametags` | `Esp`, nametag render | Слить |
| `Prediction` | — | Новый render module |
| `AutoSwap` | `rich.modules.impl.combat.AutoSwap` | Слить |

### Mixins — **конфликты**

| com.example.mixin | rich.mixin | Действие |
|-------------------|------------|----------|
| `EntityRendererMixin` | `EntityRendererMixin` + `MixinPlayerEntityRenderer` | **Объединить** nametag logic в один таргет (`PlayerEntityRenderer`, `PlayerEntityRenderState`) |
| `GameRendererMixin` | `GameRendererMixin` | Объединить @Inject (noHurtCam, FOV, aspect) |
| `FogMixin` | `StatusEffectFogModifierMixin`, `FogDistanceMixin` | Объединить |
| `TitleScreenMixin` | `TitleScreenMixin` | Объединить (Accounts button) |
| `ChatScreenMixin` | `ChatScreenMixin` | Объединить HUD click |
| `GuiMixin` | `InGameOverlayRendererMixin` | Перенести fire overlay cancel |
| `HudOverlayMixin` | `InGameHudMixin`, `AnimationsMixin` | Объединить |
| `DoubleSliderCallbacksMixin` | — | Добавить в mixins.json |
| `ExampleClientMixin` | `MinecraftClientMixin` | Не дублировать |
| `ParticleEngineMixin`, `FogRendererMixin` | stubs | Удалить |
| `ExampleMixin` (main) | — | Удалить |

---

## (c) Mixin-конфиги (итоговый проект)

| Файл | В fabric.mod.json | package | Классов |
|------|-------------------|---------|---------|
| `mixins.json` | ✓ | `rich.mixin` | 42 client + 7 common |
| `mixins.sodium.json` | ✓ (optional) | `rich.mixin` | 1 |
| ~~`rich.mixins.json`~~ | ✗ удалить | — | мёртвый |
| ~~`modid.*`~~ | ✗ | — | legacy |

После Фазы 2: добавить в `mixins.json` только **новые** mixin-классы из переноса (без дубля таргетов).

---

## Фаза 0 — выполняется сейчас

1. Stash `com.example` → `_migration_stash/`
2. Copy `src-decompiled/{rich,antidaunleak}` → `src/client/java/`
3. Resources `extracted-rv` → `src/main/resources` + `src/client/resources`
4. `fabric.mod.json`, `mixins.json` (fix duplicate injectors), accessWidener
5. `build.gradle` + `settings.gradle` (id, loom aw, netty)
6. `gradlew compileClientJava` → log
