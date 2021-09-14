package com.tangykiwi.kiwiclient.util.render.shader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;

public class OutlineVertexConsumers {

	private static final Identifier nonExistentId = new Identifier("bleachhack", "shader-placeholder-id");

	public static VertexConsumer outlineOnlyConsumer(float r, float g, float b, float a) {
		OutlineVertexConsumerProvider vertexProvider = MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers();
		vertexProvider.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));

		return vertexProvider.getBuffer(RenderLayer.getOutline(nonExistentId));
	}

	public static VertexConsumerProvider outlineOnlyProvider(float r, float g, float b, float a) {
		OutlineVertexConsumerProvider vertexProvider = MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers();
		vertexProvider.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));

		return new Override(vertexProvider);
	}

	private static class Override implements VertexConsumerProvider {

		private OutlineVertexConsumerProvider parentProvider;

		public Override(OutlineVertexConsumerProvider parent) {
			this.parentProvider = parent;
		}

		public VertexConsumer getBuffer(RenderLayer renderLayer) {
			return parentProvider.getBuffer(RenderLayer.getOutline(nonExistentId));
		}
	}
}
