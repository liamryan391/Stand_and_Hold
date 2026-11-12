package com.liamryan.standandhold.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.liamryan.standandhold.StandAndHoldConstants;
import com.liamryan.standandhold.common.npc.HumanNpc;
import com.liamryan.standandhold.common.npc.NpcVariants;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * M33 D5 experiment: the Army Rifleman on GeckoLib keyframes. Same original atlas variants and render-only build as the
 * native renderer ({@link HumanNpcRenderer}); geometry comes from the Blockbench source via
 * {@code scripts/export-geckolib-rifleman.py}. Client-only; registered only when the experiment renderer is selected.
 */
public final class RiflemanGeoRenderer extends GeoEntityRenderer<HumanNpc, RiflemanGeoRenderer.State> {
    /** Declares GeckoLib's state interface directly, so compiling needs no interface injection; GeckoLib's runtime methods still apply. */
    public static final class State extends LivingEntityRenderState implements GeoRenderState { }
    static final DataTicket<Integer> VARIANT = DataTicket.create("standandhold_variant", Integer.class);
    static final DataTicket<Float> BUILD_WIDTH = DataTicket.create("standandhold_build_width", Float.class);
    static final DataTicket<Float> BUILD_HEIGHT = DataTicket.create("standandhold_build_height", Float.class);

    public RiflemanGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        shadowRadius = 0.5F;
        withRenderLayer(new ItemInHandGeoLayer<>(context, this));
    }
    @Override public State createRenderState(HumanNpc npc, Void ignored) { return new State(); }
    @Override public void addRenderData(HumanNpc npc, Void ignored, State state, float partialTick) {
        state.addGeckolibData(VARIANT, NpcVariants.index(npc.getUUID()));
        state.addGeckolibData(BUILD_WIDTH, NpcVariants.width(npc.getUUID()));
        state.addGeckolibData(BUILD_HEIGHT, NpcVariants.height(npc.getUUID()));
    }
    /** Render-only build variation, as on the native rig; the hitbox is unchanged. */
    @Override public void scaleModelForRender(RenderPassInfo<State> info, float widthScale, float heightScale) {
        State state = info.renderState();
        super.scaleModelForRender(info, widthScale * state.getOrDefaultGeckolibData(BUILD_WIDTH, 1F), heightScale * state.getOrDefaultGeckolibData(BUILD_HEIGHT, 1F));
    }

    private static final class Model extends GeoModel<HumanNpc> {
        private static final Identifier ASSET = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "entity/army_rifleman");
        private final Identifier[] textures = new Identifier[NpcVariants.COUNT];
        Model() {
            for (int i = 0; i < textures.length; i++) {
                textures[i] = Identifier.fromNamespaceAndPath(StandAndHoldConstants.MOD_ID, "textures/entity/variants/army_rifleman_" + i + ".png");
            }
        }
        @Override public Identifier getModelResource(GeoRenderState state) { return ASSET; }
        @Override public Identifier getTextureResource(GeoRenderState state) {
            return textures[Math.floorMod(state.getOrDefaultGeckolibData(VARIANT, 0), textures.length)];
        }
        @Override public Identifier getAnimationResource(HumanNpc npc) { return ASSET; }
    }
}
