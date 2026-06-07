# План восстановления runtime-visuals (Minecraft 1.21.11)

## Текущее состояние репозитория

| Область | Состояние |
|---------|-----------|
| **Сборка Gradle** | `modid` + `com.example.*` в `src/client`, `src/main` — **собирается** |
| **Оригинал JAR** | `runtime-visuals.jar` → декомпиляция в `src-decompiled/` (455 .java, intermediary `class_*`) |
| **Ресурсы оригинала** | `extracted-rv/` (fabric.mod.json, mixins, assets, accessWidener, bundled netty jars) |
| **Дубликаты** | `src/src/main/resources/` — лишняя копия modid-конфигов (удалить) |
| **Цель** | Заменить/мигрировать проект на `rich.*` + `antidaunleak.*`, yarn-имена, рабочие миксины |

---

## A. Инвентаризация mixin-конфигов

### 1) Оригинал runtime-visuals (`extracted-rv/` / `src-decompiled/`)

#### `mixins.json` (в `fabric.mod.json` ✓)
- **package:** `rich.mixin`
- **environment client[]:** 42 класса
- **environment mixins[] (common):** 7 классов

| Секция | Класс | Файл в src-decompiled | Примечание |
|--------|-------|----------------------|------------|
| client | AccessibilityOnboardingScreenMixin | ✓ | |
| client | AnimationsMixin | ✓ | priority 900, target class_329 |
| client | BlockOutlineMixin | ✓ | |
| client | CameraMixin | ✓ | |
| client | ChatInputSuggestorMixin | ✓ | |
| client | ChatScreenMixin | ✓ | |
| client | ChunkOcclusionDataBuilderMixin | ✓ | |
| client | ClientPlayerEntityMixin | ✓ | |
| client | ClientPlayerInteractionManagerMixin | ✓ | дубль с MixinClientPlayerInteractionManager |
| client | ClientPlayNetworkHandlerMixin | ✓ | |
| client | ClientWorldAccessor | ✓ | |
| client | ClientWorldMixin | ✓ | |
| client | CustomCapeMixin | ✓ | |
| client | EntityRendererMixin | ✓ | @Mixin(class_897) — **проверить @Inject vs yarn** |
| client | FogDistanceMixin | ✓ | |
| client | FogRendererMixin | ✓ | |
| client | GameRendererMixin | ✓ | |
| client | HandledScreenMixin | ✓ | |
| client | InventoryAnimMixin | ✓ | |
| client | HeldItemRendererMixin | ✓ | |
| client | IClientWorld | ✓ | interface mixin |
| client | InGameHudMixin | ✓ | |
| client | InGameOverlayRendererMixin | ✓ | |
| client | IScreen | ✓ | interface mixin |
| client | KeyboardInputMixin | ✓ | |
| client | KeyboardMixin | ✓ | |
| client | LightmapTextureManagerMixin | ✓ | |
| client | LivingEntityRendererMixin | ✓ | |
| client | MinecraftClientMixin | ✓ | |
| client | MixinBuiltChunk | ✓ | |
| client | MixinClientPlayerInteractionManager | ✓ | тот же target class_636 |
| client | MixinItemEntityRenderer | ✓ | |
| client | MixinPlayerEntityRenderer | ✓ | class_1007 PlayerEntityRenderer |
| client | MouseMixin | ✓ | |
| client | MultiplayerServerListWidgetMixin | ✓ | |
| client | PlayerListHudMixin | ✓ | |
| client | ScreenMixin | ✓ | |
| client | SkyRendererMixin | ✓ | |
| client | SplashOverlayMixin | ✓ | |
| client | StatusEffectFogModifierMixin | ✓ | |
| client | TitleScreenMixin | ✓ | |
| client | WindowMixin | ✓ | |
| mixins | ClientConnectionAccessor | ✓ | |
| mixins | ClientConnectionMixin | ✓ | |
| mixins | EntityMixin | ✓ | |
| mixins | FireworkRocketEntityMixin | ✓ | |
| mixins | LivingEntityMixin | ✓ | |
| mixins | PlayerEntityMixin | ✓ | |
| mixins | TextVisitFactoryMixin | ✓ | |

**Есть в коде, НЕ в mixins.json** (решение на этапе B):
- `MultiplayerScreenOpenMixin`
- `ItemGroupsAccessor`, `CreativeInventoryScreenAccessor`
- `IClientPlayerInteractionManager`
- `accessor/PlayerInventoryAccessor`

**Проблемы JSON:** дублированный ключ `"injectors"` (строки 49–54) — оставить один блок.

#### `mixins.sodium.json` (в `fabric.mod.json` ✓, `required: false`)
- **package:** `rich.mixin`
- **client:** `WorldRendererMixin` ✓

#### `rich.mixins.json` (**НЕ** в `fabric.mod.json` оригинала)
- **package:** `rich.mixin.client` — **пакет не существует** в декомпиляции
- **mixins:** `InGameHudMixin`, `MinecraftClientMixin` — лежат в `rich.mixin`
- **Действие:** не подключать OR исправить package → `rich.mixin` и добавить в fabric только если нужен второй конфиг (скорее удалить как мёртвый)

#### `runtime_visuals.accesswidener` (в `fabric.mod.json` ✓)
- Поля/классы уже в **yarn**-именах (PlayerControllerMP, LocalPlayer, GameRenderer…)
- Подключить в loom: `accessWidener = "runtime_visuals.accesswidener"`

#### crm / CometRenderer
- Отдельный мод `crm` — **свой** `crm.mixins.json` в JAR CometRenderer, не дублировать в проекте

---

### 2) Текущий modid-проект (`src/main/resources/`)

#### `modid.mixins.json`
- **package:** `com.example.mixin`
- **mixins[]:** `ExampleMixin` (файл есть в `src/main/java`)

#### `modid.client.mixins.json`
- **package:** `com.example.client.mixin`
- **client[]:** 9 классов — все файлы в `src/client/java` ✓
- **EntityRendererMixin:** уже `PlayerEntityRenderState` (исправлено ранее)

**При миграции на runtime-visuals:** modid-конфиги и `com.example.*` убрать или оставить только если нужен гибрид (по ТЗ — восстановить оригинал → **заменить**).

---

## B. Entrypoints (оригинал vs modid)

| Entrypoint | Оригинал (`extracted-rv/fabric.mod.json`) | Текущий modid |
|------------|-------------------------------------------|---------------|
| `preLaunch` | `rich.util.mods.config.wave.WaveCapesConfigOverride` | отсутствует |
| `client` | `rich.Initialization` | `com.example.client.ExampleModClient` |

**Нет** `main` в оригинале (`environment: client` only).

**Инициализация rich:** `Initialization.onInitializeClient()` → `d.a()`; `init()` создаёт `Manager` — найти, кто вызывает `init()` (вероятно mixin / preLaunch).

**ModMenuInitializer / preLaunch:** относится к **старому** `com.example` слою; в runtime-visuals аналог — `WaveCapesConfigOverride` + `Initialization`.

---

## C. Фазы работ (порядок)

### Фаза 0 — Структура (быстро)
1. Удалить `src/src/` (дубликаты resources).
2. Скопировать `src-decompiled/rich/**` → `src/client/java/rich/**`, `antidaunleak/**` → `src/main/java/` (если есть server/common код) или всё в client.
3. Скопировать `extracted-rv/assets`, `mixins*.json`, `runtime_visuals.accesswidener`, `META-INF/jars` → `src/main/resources` / `src/client/resources`.
4. Заменить `fabric.mod.json` на оригинал (id `copyright`, entrypoints, mixins, accessWidener, jars).
5. Обновить `build.gradle`: mod id, зависимости (netty jars из META-INF/jars?), antidaunleak если отдельный артефакт.

### Фаза 1 — Маппинги yarn (~300+ файлов)
- Массовая замена `class_NNN` → yarn через Loom named mappings + ручная правка остатков.
- Инструмент: `gradlew compileClientJava` + IDE / скрипт по ошибкам.
- Приоритет: `rich.mixin.*`, `Initialization`, `Manager`, render pipeline.

### Фаза 2 — Миксины (по одному при runClient)
- Прогнать `runClient`, ловить `InvalidInjectionException`.
- Критичные кандидаты (как в MOD_ERROR_LOG для modid): **EntityRendererMixin**, **Player/Living entity renderers**, **Fog/StatusEffect**, **GameRenderer**.
- Сверять `@Inject` дескрипторы с yarn (1.21.11): `PlayerEntityRenderState`, `EntityRenderState`, `RenderState` pipeline.

### Фаза 3 — Зависимости
- `com.ferra13671.*` — только из Maven (CometRenderer 3.0.0), не из src-decompiled.
- `antidaunleak.api` — оставить в проекте или вынести в jar dependency.
- Не компилировать дубликаты из `src-decompiled` рядом с Maven.

### Фаза 4 — Сборка и запуск
- `./gradlew build` → 0 errors.
- `./gradlew runClient` → нет mixin crash.
- Нативный OOM (0xC0000409) — **вне scope** до стабильной сборки/запуска.

---

## D. Оценка рисков

| Риск | Митигация |
|------|-----------|
| 300+ intermediary имён | Итеративная компиляция, пакетами (mixin → util → modules) |
| Два мода в одном репо | Удалить `com.example` после миграции |
| Sodium mixin optional | `required: false` — OK без Sodium |
| Bundled netty in fabric.mod.json `jars` | Скопировать в resources, проверить loom nested jars |
| Obfuscated `rich.util.d`, `rich.util.a` | Не переименовывать без необходимости |

---

## E. Следующий шаг (после утверждения плана)

1. Выполнить **Фазу 0** (структура + fabric.mod.json + удаление src/src).
2. Первый `gradlew compileClientJava` — выгрузить **топ-50** ошибок компиляции.
3. Чинить итеративно до `build` SUCCESS, затем `runClient` и mixin-фиксы.
