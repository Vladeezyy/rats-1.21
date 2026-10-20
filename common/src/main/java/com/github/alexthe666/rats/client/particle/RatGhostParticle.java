package com.github.alexthe666.rats.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.SimpleParticleType;

public class RatGhostParticle extends SingleQuadParticle {

	public RatGhostParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
		this.alpha = 1.0F;
		this.lifetime = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
	}

	@Override
	public int getLightCoords(float f) {
		int i = super.getLightCoords(f);
		int k = i >> 16 & 255;
		return 240 | k << 16;
	}

	@Override
	public void tick() {
		super.tick();
		this.yd *= 0.0D;
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {

		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			RatGhostParticle particle = new RatGhostParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite.get(random));
			return particle;
		}
	}
}