package rich.modules.impl.misc;

import rich.Initialization;
import rich.events.api.EventManager;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.animations.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Panic Mode:
 * - Активация через бинд или через GUI: одинаковые эффекты
 * - В GUI модуль всегда показывается как выключенный (нельзя случайно оставить включённым)
 * - Клавиша настраивается в GUI (биндинг PanicMode), по умолчанию HOME
 */
public class PanicMode extends ModuleStructure {

    private static PanicMode instance;

    private boolean panicActive = false;
    private boolean rejoinedAfterPanic = false;
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
     * Вызывается автоматически, когда игрок включает модуль через GUI.
     * Триггерит панику и сразу сбрасывает визуальный статус на «выключен».
     */
    @Override
    public void activate() {
        activatePanic();
        // Сбрасываем визуальный статус модуля в «выключен» без рекурсии:
        // напрямую меняем state + анимацию + снимаем с подписки событий
        this.state = false;
        this.getAnimation().setDirection(Direction.BACKWARDS);
        EventManager.unregister(this);
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

    /** @return true если GUI заблокирован */
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
