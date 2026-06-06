package rich.modules.impl.combat;

import java.util.Random;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AimEngine {

    public enum Profile { SLOTH, SMOOTH, FUNTIME, HVH }

    private final AimAssist config;
    private final Random rng = new Random();

    // Состояние вращения
    private float lastYaw, lastPitch;
    private float rawYawDelta, rawPitchDelta;
    private boolean firstFrame = true;

    // Лок на цель
    private UUID lockedUUID   = null;
    private int  lostTicks    = 0;
    private int  reactionTicks = 0;

    // Aim-point по хитбоксу
    private double offX = 0, offY = 0.65, offZ = 0;
    private int aimPointTtl = 0;

    // Prediction
    private double prevTX, prevTZ;
    private boolean hasPrev = false;

    // GCD remainder (дробный остаток для snap)
    private float yawRem = 0, pitchRem = 0;

    // Тик-бюджет (угловая скорость за тик)
    private long  lastTick = -1;
    private float tickYawUsed = 0, tickPitchUsed = 0;
    private float tickYawCap  = 15f, tickPitchCap = 9f;

    public AimEngine(AimAssist config) { this.config = config; }

    // ─── Главный вызов каждый frame ────────────────────────────────────────────
    public void onFrame(float partialTick) {
        MinecraftClient mc = config.mc;
        if (!config.isState() || mc.player == null || mc.world == null) { reset(); return; }
        if (mc.currentScreen != null) return;

        ClientPlayerEntity player = mc.player;
        if (firstFrame) {
            lastYaw = player.getYaw(); lastPitch = player.getPitch();
            firstFrame = false; return;
        }

        rawYawDelta   = MathHelper.wrapDegrees(player.getYaw()   - lastYaw);
        rawPitchDelta = player.getPitch() - lastPitch;
        lastYaw   = player.getYaw();
        lastPitch = player.getPitch();

        float mouseDelta = Math.abs(rawYawDelta) + Math.abs(rawPitchDelta);
        // Ассист ТОЛЬКО при движении мыши (настраиваемо)
        if (config.onlyOnMove() && mouseDelta < 0.01f) {
            yawRem = 0; pitchRem = 0; return;
        }

        Entity target = resolveTarget(mc, player);
        if (target == null) { yawRem = 0; pitchRem = 0; return; }

        // Имитация времени реакции при первом обнаружении
        if (reactionTicks > 0) { --reactionTicks; return; }

        if (!canSeeTarget(mc, player, target)) {
            if (++lostTicks > 8) { lockedUUID = null; reactionTicks = 0; hasPrev = false; }
            return;
        }
        lostTicks = 0;

        if (--aimPointTtl <= 0) randomizeAimPoint(target, player.distanceTo(target));

        applyRotation(mc, player, target);
    }

    // ─── Применение ротации (профиль + GCD) ───────────────────────────────────
    private void applyRotation(MinecraftClient mc, ClientPlayerEntity player, Entity target) {
        Vec3d eye = player.getEyePos();

        // Простое предсказание позиции цели
        double tx = target.getX(), tz = target.getZ();
        if (hasPrev) {
            float dist = player.distanceTo(target);
            float pf   = MathHelper.clamp(dist * 0.4f, 0.5f, 2.0f);
            tx += (tx - prevTX) * pf;
            tz += (tz - prevTZ) * pf;
        }
        prevTX = target.getX(); prevTZ = target.getZ(); hasPrev = true;

        double ty = target.getY() + target.getHeight() * offY + rng.nextGaussian() * 0.005;
        double dx = tx + offX + rng.nextGaussian() * 0.003 - eye.x;
        double dy = ty - eye.y;
        double dz = tz + offZ + rng.nextGaussian() * 0.003 - eye.z;

        double hd = Math.sqrt(dx*dx + dz*dz);
        float wantYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float wantPitch = (float)(-Math.toDegrees(Math.atan2(dy, hd)));

        float yawD   = MathHelper.wrapDegrees(wantYaw   - player.getYaw());
        float pitchD = wantPitch - player.getPitch();
        float total  = (float) Math.sqrt(yawD*yawD + pitchD*pitchD);
        if (total < 0.001f) return;

        // Выход из FOV — сбрасываем лок
        if (total > config.fov() + 12f) { lockedUUID = null; return; }

        // Deadzone — не дёргаемся в центре хитбокса
        float halfW   = target.getWidth() * 0.5f;
        float dist    = player.distanceTo(target);
        float deadzone = Math.max((float)Math.toDegrees(Math.atan2(halfW, dist)) * 0.15f, 0.25f);
        if (total < deadzone) { yawRem = 0; pitchRem = 0; return; }

        // Случайный пропуск кадра (4%) — убирает ботовый паттерн
        if (rng.nextFloat() < 0.04f) return;

        // GCD (квант мыши MC)
        float sens = (float)((Double)mc.options.getMouseSensitivity().getValue()).doubleValue() * 0.6f + 0.2f;
        float gcd  = sens * sens * sens * 1.2f;

        // Per-axis straight-line step по профилю
        float[] step = profileStep(yawD, pitchD, total);
        float rawYaw   = step[0];
        float rawPitch = step[1];

        // Тик-бюджет с рандомным потолком (фиксированный потолок — сигнатура бота)
        long curTick = mc.world.getTime();
        if (curTick != lastTick) {
            lastTick = curTick; tickYawUsed = 0; tickPitchUsed = 0;
            tickYawCap   = 13f + rng.nextFloat() * 5f;   // 13..18°/тик
            tickPitchCap =  7.5f + rng.nextFloat() * 3f; //  7.5..10.5°/тик
        }
        rawYaw   = MathHelper.clamp(rawYaw,   -(tickYawCap   - Math.abs(tickYawUsed)),
                                               tickYawCap    - Math.abs(tickYawUsed));
        rawPitch = MathHelper.clamp(rawPitch, -(tickPitchCap - Math.abs(tickPitchUsed)),
                                               tickPitchCap  - Math.abs(tickPitchUsed));

        // Snap к GCD-сетке с накоплением дробного остатка
        float desY = rawYaw   + yawRem;
        float desP = rawPitch + pitchRem;
        float applyYaw   = Math.round(desY / gcd) * gcd;
        float applyPitch = Math.round(desP / gcd) * gcd;
        yawRem   = MathHelper.clamp(desY - applyYaw,   -gcd*4, gcd*4);
        pitchRem = MathHelper.clamp(desP - applyPitch, -gcd*4, gcd*4);

        tickYawUsed   += applyYaw;
        tickPitchUsed += applyPitch;

        if (applyYaw != 0 || applyPitch != 0) {
            float ny = player.getYaw()   + applyYaw;
            float np = MathHelper.clamp(player.getPitch() + applyPitch, -90f, 90f);
            player.setYaw(ny);
            player.setPitch(np);
            lastYaw   = ny;
            lastPitch = np;
        }
    }

    /**
     * Per-axis straight-line cap + профиль.
     * cap = |axisDelta/total| * MAX — держит траекторию по прямой к цели.
     * AC детектируют, когда yaw/pitch ratio меняется нелинейно (дуга вместо прямой).
     */
    private float[] profileStep(float yawD, float pitchD, float total) {
        float yaxisRatio  = Math.abs(yawD)   / total;
        float paxisRatio  = Math.abs(pitchD) / total;
        boolean toward    = rawYawDelta * yawD + rawPitchDelta * pitchD > 0;

        return switch (config.getProfile()) {
            case SLOTH -> {
                // ~7..12° yaw, ~4..7° pitch — медленно, почти как рука
                float yCap = yaxisRatio  * (7f  + rng.nextFloat() * 5f);
                float pCap = paxisRatio  * (4f  + rng.nextFloat() * 3f);
                yield new float[]{
                    MathHelper.clamp(yawD,   -yCap, yCap) + (rng.nextFloat()-0.5f)*0.3f,
                    MathHelper.clamp(pitchD, -pCap, pCap) + (rng.nextFloat()-0.5f)*0.2f
                };
            }
            case FUNTIME -> {
                // cap 130°, lerp 0.85 — быстрый, как FunTime/Releon
                float yCap = yaxisRatio  * 130f;
                float pCap = paxisRatio  * 130f;
                float cy   = MathHelper.clamp(yawD,   -yCap, yCap);
                float cp   = MathHelper.clamp(pitchD, -pCap, pCap);
                float mul  = toward ? 0.85f : 0.1f;
                yield new float[]{ cy * mul, cp * mul };
            }
            case HVH -> {
                // Почти instant — для серверов без rot-проверок
                yield new float[]{
                    MathHelper.clamp(yawD,   -yaxisRatio*360f, yaxisRatio*360f),
                    MathHelper.clamp(pitchD, -paxisRatio*360f, paxisRatio*360f)
                };
            }
            default -> { // SMOOTH
                float maxDeg  = 18f + config.smoothness() * 14f; // 18..32°
                float yCap    = yaxisRatio  * maxDeg;
                float pCap    = paxisRatio  * maxDeg * 0.65f;
                float t       = Math.min((total - 0.25f) / 18f, 1f);
                float strength = toward ? 0.5f + t*0.35f : 0.05f;
                float jY      = (rng.nextFloat()-0.5f) * 0.16f;
                float jP      = (rng.nextFloat()-0.5f) * 0.10f;
                // Overshoot/undershoot
                float r = rng.nextFloat();
                float os = r < 0.14f ? 1.04f + rng.nextFloat()*0.12f
                         : r < 0.32f ? 0.72f + rng.nextFloat()*0.18f : 0.95f;
                float ry = MathHelper.clamp(yawD   * strength * os + jY, -yCap, yCap);
                float rp = MathHelper.clamp(pitchD * strength * os * 0.6f + jP, -pCap, pCap);
                if (!toward) { ry *= 0.12f; rp *= 0.12f; }
                yield new float[]{ ry, rp };
            }
        };
    }

    // ─── Разрешение цели (лок + поиск) ───────────────────────────────────────
    private Entity resolveTarget(MinecraftClient mc, ClientPlayerEntity player) {
        if (lockedUUID != null) {
            for (Entity e : mc.world.getEntities()) {
                if (!e.getUuid().equals(lockedUUID) || !(e instanceof LivingEntity le)) continue;
                if (le.isAlive() && !le.isSpectator() && player.distanceTo(e) <= config.maxDistance()) {
                    lostTicks = 0; return e;
                }
                if (++lostTicks >= 25) lockedUUID = null;
                break;
            }
        }
        Entity found = config.findTarget(mc);
        if (found != null) {
            lockedUUID    = found.getUuid();
            lostTicks     = 0;
            hasPrev       = false;
            reactionTicks = 1 + rng.nextInt(2); // 1..2 тика задержки реакции
            aimPointTtl   = 0;
        }
        return found;
    }

    // ─── Видимость: 3 точки (голова, торс, ноги) ─────────────────────────────
    private boolean canSeeTarget(MinecraftClient mc, PlayerEntity player, Entity target) {
        Vec3d eye = player.getEyePos();
        double[] yOffsets = { target.getHeight() * 0.9, target.getHeight() * 0.5, 0.1 };
        for (double yo : yOffsets) {
            Vec3d tp  = new Vec3d(target.getX(), target.getY() + yo, target.getZ());
            BlockHitResult hit = mc.world.raycast(new RaycastContext(
                eye, tp, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player));
            if (hit.getType() == HitResult.Type.MISS || hit.getPos().distanceTo(tp) < 1.0)
                return true;
        }
        return false;
    }

    // ─── Рандомизация aim-point внутри хитбокса ───────────────────────────────
    private void randomizeAimPoint(Entity target, float dist) {
        double half = Math.max(0.04, target.getWidth() * 0.38);
        offX = MathHelper.clamp(rng.nextGaussian() * half * 0.45, -half, half);
        offZ = MathHelper.clamp(rng.nextGaussian() * half * 0.45, -half, half);
        double minY = dist > 3f ? 0.48 : 0.56;
        double maxY = dist > 3f ? 0.76 : 0.86;
        offY = minY + rng.nextDouble() * (maxY - minY);
        aimPointTtl = 8 + rng.nextInt(14);
    }

    private void reset() {
        lockedUUID = null; reactionTicks = 0; aimPointTtl = 0;
        hasPrev = false; firstFrame = true;
        yawRem = 0; pitchRem = 0;
        tickYawUsed = 0; tickPitchUsed = 0; lastTick = -1;
    }
}
