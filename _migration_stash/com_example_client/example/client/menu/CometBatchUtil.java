package com.example.client.menu;

import com.ferra13671.cometrenderer.minecraft.batch.IPrimitiveBatch;
import com.ferra13671.cometrenderer.minecraft.batch.impl.ColoredRectBatch;
import com.ferra13671.cometrenderer.minecraft.batch.impl.RoundedRectBatch;

/**
 * Ensures CometRenderer batches are drawn and native resources are released.
 */
public final class CometBatchUtil {
    private CometBatchUtil() {
    }

    public static void flush(RoundedRectBatch batch) {
        if (batch == null || batch.isBuilt()) {
            return;
        }
        IPrimitiveBatch built = batch.build();
        try (built) {
            built.tryDraw();
        } catch (Exception e) {
            built.close();
            throw e;
        }
    }

    public static void flush(ColoredRectBatch batch) {
        if (batch == null || batch.isBuilt()) {
            return;
        }
        IPrimitiveBatch built = batch.build();
        try (built) {
            built.tryDraw();
        } catch (Exception e) {
            built.close();
            throw e;
        }
    }

    public static void discard(RoundedRectBatch batch) {
        if (batch != null && !batch.isBuilt()) {
            batch.close();
        }
    }

    public static void discard(ColoredRectBatch batch) {
        if (batch != null && !batch.isBuilt()) {
            batch.close();
        }
    }
}
