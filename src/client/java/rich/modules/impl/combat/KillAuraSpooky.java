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
 * KillAura — TestSmooth-style rotation (волны + lerp 0.6f).
 *
 * Silent: onWorldRender восстанавливает realYaw/realPitch перед рендером.
 *   → от 1-го лица камера не двигается; пакеты уходят с curYaw.
 *
 * W-release: SUPPRESS_FORWARD, как в Triggerbot.
 */
public class KillAuraSpooky extends ModuleStructure {

    // ── Настройки ──────────────────────────────────────────────────────────
    public final SliderSettings range      = new SliderSettings("Range",     "Attack range")       .setValue(3.1F).range(1.0F, 6.0F);
    public final SliderSettings fovSetting = new SliderSettings("FOV",       "Max FOV to target")  .setValue(180.0F).range(10.0F, 180.0F);
    public final BooleanSetting onlySword  = new BooleanSetting("OnlySword", "Only sword/axe")     .setValue(true);
    public final BooleanSetting silentRot  = new BooleanSetting("Silent",    "Silent rotation")    .setValue(true);

    // ── Ротация ────────────────────────────────────────────────────────────
    private final Random rng = new Random();

    /** Текущий серверный угол */
    private float curYaw;
    private float curPitch;
    private boolean hasRotation = false;

    /** Реальные углы камеры игрока */
    private float realYaw;
    private float realPitch;

    private Entity lockedTarget = null;

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
        super("KillAura", "SpookyTime kill-aura (TestSmooth)", ModuleCategory.UTILITIES);
        this.settings(range, fovSetting, onlySword, silentRot);
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

            // Сохраняем реальные углы камеры до любых наших изменений
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
                    wantSuppress = true;
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

            if (target == null) {
                lockedTarget = null;
                hasRotation  = false;
                return;
            }
            lockedTarget = target;

            // ── TestSmooth-style rotation ─────────────────────────────────
            Vec3d aimPoint = getNearestVisiblePoint(player, target);
            float[] next   = testSmoothRotate(player, aimPoint);
            curYaw   = next[0];
            curPitch = next[1];
            hasRotation = true;

            // Отправляем пакет с curYaw (silentRot: onWorldRender вернёт реальную камеру)
            setPacketRotation(player, curYaw, curPitch);

            // Атака
            tickAttack(player, target);

        } finally {
            Triggerbot.SUPPRESS_FORWARD = wantSuppress;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDER: восстанавливаем камеру — от 1-го лица ничего не движется
    // ══════════════════════════════════════════════════════════════════════
    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (!silentRot.isValue()) return;
        if (mc.player == null || !this.isState()) return;
        mc.player.setYaw(realYaw);
        mc.player.setPitch(realPitch);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TestSmooth-style алгоритм (волновой шум + lerp 0.6f)
    // ══════════════════════════════════════════════════════════════════════
    private float[] testSmoothRotate(ClientPlayerEntity player, Vec3d aimPoint) {
        Vec3d eye  = player.getEyePos();
        double dx  = aimPoint.x - eye.x;
        double dy  = aimPoint.y - eye.y;
        double dz  = aimPoint.z - eye.z;
        double hd  = Math.sqrt(dx * dx + dz * dz);

        float targetYaw   = (float)  Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, hd)));

        float fromYaw   = hasRotation ? curYaw   : realYaw;
        float fromPitch = hasRotation ? curPitch : realPitch;

        // Дельты
        float yawDelta   = MathHelper.wrapDegrees(targetYaw   - fromYaw);
        float pitchDelta = targetPitch - fromPitch;
        float rotDiff    = (float) Math.max(Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta)), 0.0001);

        // Базовые скорости (randomLerp)
        float baseSpeedYaw   = randomLerp(45f, 67f);
        float baseSpeedPitch = randomLerp(10f, 25f);
        if (rotDiff < 10f) {
            baseSpeedYaw   *= (rotDiff / 10f);
            baseSpeedPitch *= (rotDiff / 10f);
        }

        float straightYaw   = Math.abs(yawDelta   / rotDiff) * baseSpeedYaw;
        float straightPitch = Math.abs(pitchDelta / rotDiff) * baseSpeedPitch;

        // Волновой шум (из TestSmooth)
        long ms     = System.currentTimeMillis();
        float wave1 = (float)(Math.sin(ms / 60.0) * 5.0);
        float wave2 = (float)(Math.cos(ms / 50.0) * 4.0);
        float wave3 = (float)(Math.sin(ms / 70.0) * 3.0);

        float yawNoise   = ((wave1 + wave3) * 0.15f) + randomLerp(-0.5f, 0.5f);
        float pitchNoise = ( wave2          * 0.15f) + randomLerp(-0.5f, 0.5f);

        // desired
        float desiredYaw   = fromYaw   + MathHelper.clamp(yawDelta,   -straightYaw,   straightYaw)   + yawNoise;
        float desiredPitch = fromPitch + MathHelper.clamp(pitchDelta, -straightPitch, straightPitch) + pitchNoise;

        // lerp 0.6f — плавность, как в TestSmooth
        float finalYaw   = lerp(fromYaw,   desiredYaw,   0.6f);
        float finalPitch = lerp(fromPitch, desiredPitch, 0.6f);
        finalPitch = MathHelper.clamp(finalPitch, -90f, 90f);

        return new float[]{ finalYaw, finalPitch };
    }

    private float randomLerp(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // ══════════════════════════════════════════════════════════════════════
    // setPacketRotation
    // ══════════════════════════════════════════════════════════════════════
    private void setPacketRotation(ClientPlayerEntity player, float yaw, float pitch) {
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.headYaw = yaw;
        player.bodyYaw = yaw;
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
        if (!critAchievable(player)) {
            if (charge >= GROUND_ATTACK_CHARGE) queueAttack(target);
            return;
        }
        if (!player.isOnGround()) {
            if (critBlocker(player) == null && charge >= CRIT_CHARGE) {
                wasFalling = false; fallGroundTicks = 0;
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
        player.setSprinting(false);
        mc.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        if (wasForward && mc.options.forwardKey.isPressed()) {
            player.setSprinting(true);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ПОИСК ЦЕЛИ
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
            float  tY  = (float)  Math.toDegrees(Math.atan2(-diff.x, diff.z));
            double tP  = -Math.toDegrees(Math.atan2(diff.y, hd));
            float baseYaw   = hasRotation ? curYaw   : realYaw;
            float basePitch = hasRotation ? curPitch : realPitch;
            double dY  = MathHelper.wrapDegrees(tY - baseYaw);
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
        double lx = -Math.sin(yaw) * Math.cos(pitch);
        double ly = -Math.sin(pitch);
        double lz =  Math.cos(yaw) * Math.cos(pitch);
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
        lockedTarget = null;
        hasRotation  = false;
        realYaw = 0; realPitch = 0;
        curYaw  = 0; curPitch  = 0;
        pendingAttack = null; preAttackDeadline = 0L;
        ticksOnGround = 0; wasFalling = false; fallGroundTicks = 0;
        ticksOutOfWater = 10;
    }
}
