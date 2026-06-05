package rich.modules.impl.misc;

import rich.Initialization;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Panic Mode:
 * 1. Сохраняет список включённых модулей
 * 2. Выключает ВСЕ модули
 * 3. Блокирует открытие GUI мода
 *
 * После перезахода на сервер (onGameJoin):
 *   - GUI разблокируется
 *   - Модули остаются выключены
 *
 * Когда игрок открывает GUI после перезахода:
 *   - Паник-режим снимается
 *   - Восстанавливает все ранее включённые модули
 *
 * Клавиша по умолчанию: HOME (268)
 */
public class PanicMode extends ModuleStructure {

    private static PanicMode instance;

    /** true = паника активна (GUI заблокирован, все модули выключены) */
    private boolean panicActive = false;

    /**
     * true = игрок уже перезашёл после паники.
     * GUI разблокирован, но модули ещё не восстановлены.
     */
    private boolean rejoinedAfterPanic = false;

    /** Модули, которые были включены до паники */
    private final List<ModuleStructure> savedEnabledModules = new ArrayList<>();

    public PanicMode() {
        super("PanicMode", "Скрывает все функции до перезахода на сервер", ModuleCategory.UTILITIES);
        this.setKey(268); // HOME
        instance = this;
    }

    public static PanicMode getInstance() {
        return instance;
    }

    /**
     * Активировать паник-режим.
     * Сохраняет и выключает все активные модули, блокирует GUI.
     */
    public void activatePanic() {
        if (panicActive) return;
        panicActive = true;
        rejoinedAfterPanic = false;
        savedEnabledModules.clear();

        Initialization init = Initialization.getInstance();
        if (init == null || init.getManager() == null) return;

        for (ModuleStructure m : init.getManager().getModuleRepository().allModules()) {
            if (m != this && m.isState()) {
                savedEnabledModules.add(m);
                m.setState(false);
            }
        }
    }

    /**
     * Вызывается при заходе игрока на сервер (GameJoinS2CPacket).
     */
    public void onServerJoin() {
        if (panicActive && !rejoinedAfterPanic) {
            rejoinedAfterPanic = true;
        }
    }

    /**
     * Вызывается когда игрок открыл GUI после перезахода.
     */
    public void onGuiOpenedAfterPanic() {
        if (panicActive && rejoinedAfterPanic) {
            panicActive = false;
            rejoinedAfterPanic = false;
            for (ModuleStructure m : savedEnabledModules) {
                m.setState(true);
            }
            savedEnabledModules.clear();
        }
    }

    /** @return true если GUI сейчас заблокирован */
    public boolean isGuiBlocked() {
        return panicActive && !rejoinedAfterPanic;
    }

    public boolean isPanicActive() {
        return panicActive;
    }

    /** @return true если ждём открытия GUI для восстановления модулей */
    public boolean isAwaitingGuiOpen() {
        return panicActive && rejoinedAfterPanic;
    }
}
