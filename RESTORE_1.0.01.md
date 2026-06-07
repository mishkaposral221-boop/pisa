# Восстановление RunTime Visuals 1.0.01

Проект восстановлен из `runtime-visuals-1.0.01.jar`.

## Метаданные мода (из fabric.mod.json)

| Поле | Значение |
|------|----------|
| id | `copyright` |
| name | RunTime Visuals |
| version | 1.0.01 |
| author | vavilovsky |
| environment | client |
| minecraft | 1.21.11 |
| java | >= 21 |
| preLaunch entrypoint | `rich.util.mods.config.wave.WaveCapesConfigOverride` |
| client entrypoint | `rich.Initialization` |
| accessWidener | `runtime_visuals.accesswidener` |
| mixins | `mixins.json`, `mixins.sodium.json` |
| bundled jars | CometRenderer-3.0.0, netty-codec-socks-4.1.82, netty-handler-proxy-4.1.82 |

## Состав

- **558 классов** (`.java` структурные скелеты) в пакетах `rich.*` и `antidaunleak.*`
- **Ресурсы**: `fabric.mod.json`, `mixins.json`, `mixins.sodium.json`, `runtime_visuals.accesswidener`, `assets/rich/**`, `META-INF/jars/**`
- **Билд**: `build.gradle`, `settings.gradle`, `gradle.properties`

## ВАЖНО: ограничение декомпиляции

Исходники восстановлены как **структурные скелеты**: точные пакеты, объявления
классов/интерфейсов/enum, наследование, поля (типы и модификаторы) и сигнатуры
методов. **Тела методов — заглушки**, потому что полноценный декомпилятор
байткода (CFR / Vineflower / Fernflower) был недоступен офлайн в песочнице
(нет интернета, нет предустановленных инструментов).

Эта ветка `restore-1.0.01` содержит конфиги + билд-файлы. Полный проект со всеми
558 java-скелетами, бинарными ассетами (212 файлов) и вложенными jar'ами собран
в ZIP-архив `runtime-visuals-1.0.01-restored.zip` (доступен для скачивания в чате).

Для получения тел методов используйте настоящий декомпилятор по
`runtime-visuals-1.0.01.jar`, либо тела из ветки с `src-decompiled/`.

## Полный список классов

См. `CLASS_MANIFEST.txt`.
