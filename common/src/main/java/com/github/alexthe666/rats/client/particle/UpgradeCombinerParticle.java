package com.github.alexthe666.rats.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.SimpleParticleType;

public class UpgradeCombinerParticle extends SingleQuadParticle {

	public UpgradeCombinerParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
		this.lifetime = 15;
		this.gravity = 0;
		this.quadSize = 0.15F;
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}

	@Override

	public int getLightCoords(float f) {
		int i = super.getLightCoords(f);
		int k = i >> 16 & 255;
		return 240 | k << 16;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.quadSize = 0.15F - (this.age * 0.01F);
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.xd *= 0D;
			this.yd *= 0D;
			this.zd *= 0D;
			this.move(this.xd, this.yd, this.zd);
		}
	}

	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {

		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			UpgradeCombinerParticle particle = new UpgradeCombinerParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite.get(random));
			return particle;
		}
	}
}