package com.github.alexthe666.rats.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** Vanilla's water bubble that lives outside water too (the auto curdler's milk) */
public class MilkBubbleParticle extends SingleQuadParticle {

	public MilkBubbleParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
		super(level, x, y, z, sprite);
		// vanilla's bubble (its constructor is private since 26.x)
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.xd = xSpeed * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
		this.yd = ySpeed * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
		this.zd = zSpeed * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
		this.lifetime = (int) (8.0 / (this.random.nextFloat() * 0.8 + 0.2));
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			this.yd += 0.002D;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.85F;
			this.yd *= 0.85F;
			this.zd *= 0.85F;
		}
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}

	public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new MilkBubbleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite.get(random));
		}
	}
}
