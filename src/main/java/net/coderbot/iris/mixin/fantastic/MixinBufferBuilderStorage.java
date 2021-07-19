package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.Iris;
import net.coderbot.iris.fantastic.ExtendedBufferStorage;
import net.coderbot.iris.fantastic.FantasticVertexConsumerProvider;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilderStorage.class)
public abstract class MixinBufferBuilderStorage implements ExtendedBufferStorage {
	@Shadow
	@Final
	private VertexConsumerProvider.Immediate entityVertexConsumers;

	@Unique
	private VertexConsumerProvider.Immediate buffered = new FantasticVertexConsumerProvider();

	@Unique
	private int begins = 0;

	@Unique
	private OutlineVertexConsumerProvider outlineVertexConsumers = new OutlineVertexConsumerProvider(buffered);

	@Inject(method = "getEntityVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceEntityVertexConsumers(CallbackInfoReturnable<VertexConsumerProvider.Immediate> provider) {
		if (!Iris.getIrisConfig().shouldDisableEntityRenderingOptimizations()) {
			if (begins == 0) {
				return;
			}
		}
		provider.setReturnValue(buffered);
	}

	@Inject(method = "getOutlineVertexConsumers", at = @At("HEAD"), cancellable = true)
	private void iris$replaceOutlineVertexConsumers(CallbackInfoReturnable<OutlineVertexConsumerProvider> provider) {
		if (!Iris.getIrisConfig().shouldDisableEntityRenderingOptimizations()) {
			if (begins == 0) {
				return;
			}

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

	public void setEnabled(boolean enabled) {
		if (enabled) {
			buffered = new FantasticVertexConsumerProvider();
			begins = 0;
			outlineVertexConsumers = new OutlineVertexConsumerProvider(buffered);
		} else {
			buffered = this.entityVertexConsumers;
			begins = 0;
			outlineVertexConsumers = new OutlineVertexConsumerProvider(this.entityVertexConsumers);
			System.gc();
		}
	}
}
