package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.ExtendedBufferStorage;
import net.coderbot.iris.fantastic.FullyBufferedVertexConsumerProvider;
import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.EntityColorWrappedRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage implements ExtendedBufferStorage {
	@Shadow
	@Final
	private SortedMap<RenderLayer, BufferBuilder> entityBuilders;

	@Unique
	private final VertexConsumerProvider.Immediate buffered = new FullyBufferedVertexConsumerProvider();

	@Unique
	private int begins = 0;

	@Unique
	private final OutlineVertexConsumerProvider outlineVertexConsumers = new OutlineVertexConsumerProvider(buffered);

	@Unique
	private static void iris$assignBufferBuilder(SortedMap<RenderLayer, BufferBuilder> builderStorage, RenderLayer layer) {
		builderStorage.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
	}

	@Inject(method = "<init>()V", at = @At("RETURN"))
	private void iris$onInit(CallbackInfo ci) {

		// Vanilla depends on being able to write to some buffers at the same time as other ones
		// This includes enchantment glints.
		//
		// We need to make sure that wrapped variants of buffered render layers are buffered too,
		// or else we'll get crashes with this approach.
		List<RenderLayer> existingLayers = new ArrayList<>(entityBuilders.keySet());
		EntityColorRenderPhase entityColorPhase = new EntityColorRenderPhase(true, 0.0F);

		for (RenderLayer existingLayer : existingLayers) {
			RenderLayer wrappedLayer = new EntityColorWrappedRenderLayer("iris_entity_color", existingLayer, entityColorPhase);
			iris$assignBufferBuilder(entityBuilders, wrappedLayer);
		}
	}


	@Inject(method = "getEntityVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceEntityVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(buffered);
	}

	@Inject(method = "getEffectVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceEffectVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (begins == 0) {
			return;
		}

		// NB: We can return the same VertexConsumerProvider here as long as the block entity and its breaking animation
		// use different render layers. This seems like a sound assumption to make. This only works with our fully
		// buffered vertex consumer provider - vanilla's Immediate cannot be used here since it would try to return the
		// same buffer for the block entity and its breaking animation in many cases.
		//
		// If anything goes wrong here, Vanilla *will* catch the "duplicate delegates" error, so
		// this shouldn't cause silent bugs.
		provider.setReturnValue(buffered);
	}

	@Inject(method = "getOutlineVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceOutlineVertexConsumers(CallbackInfoReturnable<OutlineVertexConsumerProvider> provider) {
		if (begins == 0) {
			return;
		}

		provider.setReturnValue(outlineVertexConsumers);
	}

	@Override
	public void beginWorldRendering() {
		begins += 1;
	}

	@Override
	public void endWorldRendering() {
		begins -= 1;
	}
}
