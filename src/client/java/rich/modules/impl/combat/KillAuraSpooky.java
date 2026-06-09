package rich.modules.impl.combat;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

/**
 * KillAura — профиль SpookyTime / Releon (SPAngle).
 *
 * Silent-режим (истинный без движения камеры):
 *   1. onTick: сохраняем realYaw/realPitch (камера игрока).
 *              Устанавливаем player.yaw = curYaw → пакет уходит с нашими углами.
 *   2. onWorldRender: до рендера восстанавливаем player.yaw = realYaw
 *              И обнуляем prevYaw/prevPitch → нет артефактов интерполяции.
 *              Камера рисуется с realYaw → не движется от 1-го лица.
 *
 * W-release: идентично Triggerbot (SUPPRESS_FORWARD + finally).
 */
public class KillAuraSpooky extends ModuleStructure {

    // ── Настройки ──────────────────────────────────────────────────────────
    public final SliderSettings range      = new SliderSettings("Range",     "Attack range")       .setValue(3.1F).range(1.0F, 6.0F);
    public final SliderSettings fovSetting = new SliderSettings("FOV",       "Max angle to target").setValue(180.0F).range(10.0F, 180.0F);
    public final BooleanSetting onlySword  = new BooleanSetting("OnlySword", "Only sword/axe")     .setValue(true);
    public final BooleanSetting silentRot  = new BooleanSetting("Silent",    "Silent rotation")    .setValue(true);
    public final BooleanSetting swayOn     = new BooleanSetting("Sway",      "SPAngle idle sway")  .setValue(true);

    // ── Ротация ────────────────────────────────────────────────────────────
    private final Random rng = new Random();
    private float   curYaw;
    private float   curPitch;
    private boolean hasRotation = false;

    /** Реальные углы камеры игрока (не меняются при silent) */
    private float   realYaw;
    private float   realPitch;

    private Entity lockedTarget = null;
    private long   targetLostMs = 0;
    private float  holdYaw;
    private float  holdPitch;

    // ── Атака / W-release ─────────────────────────────────────────────────
    private Entity  pendingAttack     = null;
    private long    preAttackDeadline = 0L;
    private boolean pendingWasForward = false;

    private int     ticksOutOfWater = 10;
    private int     ticksOnGround   = 0;
    private boolean wasFalling      = false;
    private int     fallGroundTicks = 0;

    private static final int   W_PRE_MIN            = 1;
    private static final int   W_PRE_MAX            = 15;
    private static final int   FALL_GATE_TICKS      = 40;
    private static final float CRIT_CHARGE          = 0.84F;
    private static final float GROUND_ATTACK_CHARGE = 0.93F;
    private static final int   GROUND_COMBO_DELAY   = 2;

    public KillAuraSpooky() {
        super("KillAura", "SpookyTime kill-aura (SPAngle)", ModuleCategory.UTILITIES);
        this.settings(range, fovSetting, onlySword, silentRot, swayOn);
    }

    public static KillAuraSpooky getInstance() {
        return c.a(KillAuraSpooky.class);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        Triggerbot.SUPPRESS_FORWARD = false;
        fullReset();
    }

    // ══════════════════════════════════════════════════════════════════════
    // ГЛАВНЫЙ ТИК
    // ══════════════════════════════════════════════════════════════════════
    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                fullReset();
                return;
            }
            ClientPlayerEntity player = mc.player;

            // Сохраняем реальные углы камеры (ещё до любых наших изменений)
            realYaw   = player.getYaw();
            realPitch = player.getPitch();

            updateGroundWaterState(player);

            // ── Pending attack: ждём W-release ─────────────────────────────────
            if (pendingAttack != null) {
                if (!isEntityValid(pendingAttack)) {
                    pendingAttack = null;
                    preAttackDeadline = 0L;
                    return;
                }
                if (System.currentTimeMillis() < preAttackDeadline) {
                    wantSuppress = true;  // W виртуально зажат
                    // Держим наши углы в пакете пока ждём
                    if (hasRotation) setPacketRotation(player, curYaw, curPitch);
                    return;
                }
                Entity t = pendingAttack;
                boolean wasForward = pendingWasForward;
                pendingAttack = null;
                preAttackDeadline = 0L;
                doAttack(player, t, wasForward);
                return;
            }

            // ── Поиск цели ─────────────────────────────────────────────────
            Entity target = findTarget(player);
            long now = System.currentTimeMillis();

            if (target != null) {
                lockedTarget = target;
                targetLostMs = 0;
            } else if (lockedTarget != null) {
                if (targetLostMs == 0) {
                    targetLostMs = now;
                    holdYaw   = curYaw;
                    holdPitch = curPitch;
                }
                long elapsed = now - targetLostMs;
                if (elapsed < 50L) {
                    if (hasRotation) setPacketRotation(player, holdYaw, holdPitch);
                    return;
                } else if (elapsed < 550L) {
                    float t     = (float)(elapsed - 50L) / 500.0F;
                    float eased = t * (0.5F + 0.5F * t);
                    float retYaw   = holdYaw   + MathHelper.wrapDegrees(realYaw   - holdYaw)   * eased;
                    float retPitch = holdPitch + (realPitch - holdPitch) * eased;
                    if (hasRotation) setPacketRotation(player, retYaw, retPitch);
                    return;
                } else {
                    lockedTarget = null;
                    hasRotation  = false;
                    return;
                }
            } else {
                hasRotation = false;
                return;
            }

            // ── SPAngle ───────────────────────────────────────────────────
            Vec3d aimPoint = getNearestVisiblePoint(player, target);

            float prevYaw   = hasRotation ? curYaw   : realYaw;
            float prevPitch = hasRotation ? curPitch : realPitch;

            float[] next = spAngle(prevYaw, prevPitch, player.getEyePos(), aimPoint);
            curYaw   = next[0];
            curPitch = next[1];
            hasRotation = true;

            float[] snapped = gcdSnap(player, prevYaw, prevPitch, curYaw, curPitch);
            curYaw   = snapped[0];
            curPitch = snapped[1];

            // Устанавливаем curYaw на игрока:
            // - если silent: только для пакета, WorldRenderEvent восстановит realYaw
            // - если не silent: камера тоже движется
            setPacketRotation(player, curYaw, curPitch);

            // Атака
            tickAttack(player, target);

        } finally {
            Triggerbot.SUPPRESS_FORWARD = wantSuppress;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDER: восстанавливаем камеру до рендера (он уже принял curYaw из пакета)
    // ══════════════════════════════════════════════════════════════════════
    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (!silentRot.isValue()) return;
        if (mc.player == null || !this.isActive()) return;
        ClientPlayerEntity player = mc.player;

        // Восстанавливаем реальные углы перед рендером:
        // prevYaw = realYaw — нет артефактов интерполяции
        player.prevYaw   = realYaw;
        player.prevPitch = realPitch;
        player.setYaw(realYaw);
        player.setPitch(realPitch);
        // headYaw/bodyYaw не меняем — другие игроки видят поворот головы
    }

    // ══════════════════════════════════════════════════════════════════════
    // setPacketRotation: угол идёт в пакет, headYaw/bodyYaw тоже обновляются
    // (headYaw сервер шлёт для анимации модельки другим игрокам)
    // ══════════════════════════════════════════════════════════════════════
    private void setPacketRotation(ClientPlayerEntity player, float yaw, float pitch) {
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.headYaw = yaw;
        player.bodyYaw = yaw;
    }

    // ══════════════════════════════════════════════════════════════════════
    // SPANGLE
    // ══════════════════════════════════════════════════════════════════════
    private float[] spAngle(float curYaw, float curPitch, Vec3d eye, Vec3d aim) {
        double dx = aim.x - eye.x;
        double dy = aim.y - eye.y;
        double dz = aim.z - eye.z;
        double hd = Math.sqrt(dx * dx + dz * dz);

        float tYaw   = (float)  Math.toDegrees(Math.atan2(-dx, dz));
        float tPitch = (float)(-Math.toDegrees(Math.atan2(dy, hd)));

        float yawD   = MathHelper.wrapDegrees(tYaw   - curYaw);
        float pitchD = tPitch - curPitch;
        float total  = (float) Math.hypot(yawD, pitchD);

        if (total < 0.001F) return new float[]{ curYaw, curPitch };

        float yawLimit   = Math.min(Math.abs(yawD),   74.0F + (float)(rng.nextDouble() * 1.03));
        float pitchLimit = Math.min(Math.abs(pitchD), 32.33F);

        boolean yawReached = Math.abs(yawD)   >= yawLimit;
        float   yawStep    = yawReached ? 65.0F + rng.nextFloat() * 35.0F
                                       :  7.7F + rng.nextFloat() *  4.4F;
        float   yawScale   = Math.min(total, yawStep) / total;
        if (!yawReached) yawScale = yawScale * (0.5F + 0.5F * yawScale);
        float   applyYaw   = MathHelper.clamp(yawD,  -yawLimit, yawLimit) * yawScale;

        boolean pitchReached = Math.abs(pitchD) >= pitchLimit;
        float   pitchStep    = pitchReached ? 65.0F + rng.nextFloat() * 35.0F
                                           :  7.7F + rng.nextFloat() *  4.4F;
        float   pitchScale   = Math.min(total, pitchStep) / total;
        if (!pitchReached) pitchScale = pitchScale * (0.5F + 0.5F * pitchScale);
        float   applyPitch   = MathHelper.clamp(pitchD, -pitchLimit, pitchLimit) * pitchScale;

        float nextYaw   = curYaw   + applyYaw;
        float nextPitch = MathHelper.clamp(curPitch + applyPitch, -89.0F, 90.0F);

        if (swayOn.isValue()) {
            long ms = System.currentTimeMillis();
            float phase = (float)((ms % 12000L) / 1200.0 * 3.0 * (Math.PI * 2.0));
            nextYaw += (float)(Math.sin(phase) * 1.15 * rng.nextGaussian());
        }

        return new float[]{ nextYaw, nextPitch };
    }

    // ══════════════════════════════════════════════════════════════════════
    // GCD SNAP
    // ══════════════════════════════════════════════════════════════════════
    private float[] gcdSnap(ClientPlayerEntity player,
                             float prevYaw, float prevPitch,
                             float nextYaw, float nextPitch) {
        Object raw = mc.options.getMouseSensitivity().getValue();
        float sens;
        if (raw instanceof Double) {
            sens = ((Double) raw).floatValue() * 0.6F + 0.2F;
        } else if (raw instanceof Number) {
            sens = ((Number) raw).floatValue() * 0.6F + 0.2F;
        } else {
            sens = 0.5F * 0.6F + 0.2F;
        }
        float gcd = sens * sens * sens * 1.2F * 0.15F;
        if (gcd < 1e-4F) return new float[]{ nextYaw, nextPitch };

        float dY = MathHelper.wrapDegrees(nextYaw   - prevYaw);
        float dP = nextPitch - prevPitch;
        dY = Math.round(dY / gcd) * gcd;
        dP = Math.round(dP / gcd) * gcd;
        return new float[]{
            prevYaw   + dY,
            MathHelper.clamp(prevPitch + dP, -90.0F, 90.0F)
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    // АТАКА
    // ══════════════════════════════════════════════════════════════════════
    private void tickAttack(ClientPlayerEntity player, Entity target) {
        if (!canHit(player, target)) return;
        float charge = player.getAttackCooldownProgress(0.0F);

        if (isInWater()) {
            if (charge >= GROUND_ATTACK_CHARGE) queueAttack(target);
            return;
        }

        boolean critPossible = critAchievable(player);

        if (!critPossible) {
            if (charge >= GROUND_ATTACK_CHARGE) queueAttack(target);
            return;
        }

        if (!player.isOnGround()) {
            if (critBlocker(player) == null && charge >= CRIT_CHARGE) {
                wasFalling = false;
                fallGroundTicks = 0;
                queueAttack(target);
            }
            return;
        }

        if (wasFalling) return;

        if (charge >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
            queueAttack(target);
        }
    }

    private void queueAttack(Entity target) {
        pendingAttack     = target;
        pendingWasForward = mc.options.forwardKey.isPressed();
        int preMs         = ThreadLocalRandom.current().nextInt(W_PRE_MIN, W_PRE_MAX + 1);
        preAttackDeadline = System.currentTimeMillis() + preMs;
    }

    private void doAttack(ClientPlayerEntity player, Entity target, boolean wasForward) {
        // Пакет уже curYaw (установлен в onTick) — удар регистрируется правильно
        player.setSprinting(false);
        mc.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        if (wasForward && mc.options.forwardKey.isPressed()) {
            player.setSprinting(true);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ВЫБОР ЦЕЛИ
    // ══════════════════════════════════════════════════════════════════════
    private Entity findTarget(ClientPlayerEntity player) {
        if (mc.world == null) return null;
        Entity best      = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == player || !(e instanceof PlayerEntity)) continue;
            PlayerEntity pe = (PlayerEntity) e;
            if (!pe.isAlive() || pe.isSpectator()) continue;

            double dist = player.distanceTo(pe);
            if (dist > range.getValue()) continue;

            Vec3d diff = pe.getEyePos().subtract(player.getEyePos());
            double hd  = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
            double tY  =  Math.toDegrees(Math.atan2(-diff.x, diff.z));
            double tP  = -Math.toDegrees(Math.atan2(diff.y, hd));
            // FOV проверяем относительно curYaw (не realYaw)
            float baseYaw   = hasRotation ? curYaw   : realYaw;
            float basePitch = hasRotation ? curPitch : realPitch;
            double dY  = MathHelper.wrapDegrees((float)(tY - baseYaw));
            double dP  = tP - basePitch;
            double ang = Math.sqrt(dY * dY + dP * dP);
            if (ang > fovSetting.getValue()) continue;

            double score = ang + dist * 0.1;
            if (score < bestScore) { bestScore = score; best = e; }
        }
        return best;
    }

    private Vec3d getNearestVisiblePoint(ClientPlayerEntity player, Entity target) {
        Box   box    = target.getBoundingBox();
        Vec3d eye    = player.getEyePos();
        Vec3d center = box.getCenter();

        Vec3d eyeLevel = new Vec3d(
            center.x,
            MathHelper.clamp((float) eye.y, (float) box.minY, (float) box.maxY),
            center.z
        );
        if (canSee(player, eyeLevel)) return eyeLevel;

        float baseYaw   = hasRotation ? curYaw   : realYaw;
        float basePitch = hasRotation ? curPitch : realPitch;
        float yaw   = baseYaw   * (float)(Math.PI / 180);
        float pitch = basePitch * (float)(Math.PI / 180);
        double lx   = -Math.sin(yaw) * Math.cos(pitch);
        double ly   = -Math.sin(pitch);
        double lz   =  Math.cos(yaw) * Math.cos(pitch);
        Vec3d crosshair = eye.add(new Vec3d(lx, ly, lz).multiply(eye.distanceTo(center)));

        return new Vec3d(
            MathHelper.clamp(crosshair.x, box.minX, box.maxX),
            MathHelper.clamp(crosshair.y, box.minY, box.maxY),
            MathHelper.clamp(crosshair.z, box.minZ, box.maxZ)
        );
    }

    private boolean canSee(ClientPlayerEntity player, Vec3d point) {
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
            player.getEyePos(), point,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, player
        ));
        return hit.getType() == HitResult.Type.MISS || hit.getPos().distanceTo(point) < 1.0;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ВСПОМОГАТЕЛЬНЫЕ
    // ══════════════════════════════════════════════════════════════════════
    private void updateGroundWaterState(ClientPlayerEntity player) {
        if (isInWater()) { ticksOutOfWater = 0; }
        else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

        if (player.isOnGround()) {
            if (ticksOnGround < 100) ticksOnGround++;
            if (wasFalling && ++fallGroundTicks >= FALL_GATE_TICKS) {
                wasFalling = false; fallGroundTicks = 0;
            }
        } else {
            ticksOnGround = 0;
            if (player.getVelocity().y < 0.0) { wasFalling = true; fallGroundTicks = 0; }
        }
    }

    private boolean canHit(ClientPlayerEntity player, Entity e) {
        return e instanceof LivingEntity
            && ((LivingEntity) e).isAlive()
            && !player.isUsingItem()
            && isWeaponInHand(player);
    }

    private boolean isWeaponInHand(ClientPlayerEntity player) {
        if (!onlySword.isValue()) return true;
        Item item = player.getMainHandStack().getItem();
        return item.getRegistryEntry().isIn(ItemTags.SWORDS)
            || item.getRegistryEntry().isIn(ItemTags.AXES)
            || item.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
    }

    private boolean isEntityValid(Entity e) {
        return e instanceof LivingEntity && ((LivingEntity) e).isAlive()
            && mc.world != null && mc.world.getEntityById(e.getId()) != null;
    }

    private boolean isInWater() {
        return mc.player.isTouchingWater()
            || mc.player.isSubmergedInWater()
            || mc.player.isSwimming();
    }

    private boolean critAchievable(ClientPlayerEntity p) {
        return ticksOutOfWater >= 3
            && !p.isTouchingWater() && !p.isClimbing()
            && !p.hasStatusEffect(StatusEffects.LEVITATION)
            && !p.hasStatusEffect(StatusEffects.BLINDNESS)
            && !p.hasVehicle() && !p.getAbilities().flying;
    }

    private String critBlocker(ClientPlayerEntity p) {
        if (!(p.fallDistance > 0.0))                      return "fall<=0";
        if (ticksOutOfWater < 3)                          return "justLeftWater";
        if (p.isOnGround())                               return "onGround";
        if (p.isClimbing())                               return "climbing";
        if (p.isTouchingWater())                          return "water";
        if (p.hasStatusEffect(StatusEffects.LEVITATION))  return "levitation";
        if (p.hasStatusEffect(StatusEffects.BLINDNESS))   return "blindness";
        if (p.hasVehicle())                               return "vehicle";
        if (p.getAbilities().flying)                      return "flying";
        return null;
    }

    private void fullReset() {
        lockedTarget = null; targetLostMs = 0; hasRotation = false;
        holdYaw = 0; holdPitch = 0;
        realYaw = 0; realPitch = 0;
        pendingAttack = null; preAttackDeadline = 0L;
        ticksOnGround = 0; wasFalling = false; fallGroundTicks = 0;
        ticksOutOfWater = 10;
    }
}
