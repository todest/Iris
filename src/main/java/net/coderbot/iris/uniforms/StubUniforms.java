package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.MinecraftClient;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.*;

/**
 * Gets some things in some shaderpacks working right now (Hardcoded/Stub)
 */
public class StubUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private StubUniforms() {
	}

	public static void addStubUniforms(UniformHolder uniforms) {
		uniforms
				.uniform1i(ONCE, "depthtex1", () -> 11)
				.uniform1i(ONCE, "gaux1", () -> 7)
				.uniform1i(ONCE, "gaux2", () -> 8)
				.uniform1i(ONCE, "gaux3", () -> 9)
				.uniform1i(ONCE, "gaux4", () -> 10)
				.uniform1i(ONCE, "colortex4", () -> 7)
				.uniform1i(ONCE, "colortex5", () -> 8)
				.uniform1i(ONCE, "colortex6", () -> 9)
				.uniform1i(ONCE, "colortex7", () -> 10);
        }
}
