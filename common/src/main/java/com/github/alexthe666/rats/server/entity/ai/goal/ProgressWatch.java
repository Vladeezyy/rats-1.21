package com.github.alexthe666.rats.server.entity.ai.goal;

/**
 * Notices a rat that no longer gets closer to its work target (#887, #818: the deposit, pickup and lumberjack goals
 * kept going forever when the target could not be reached or was gone, so the rat froze with an item in its paw until
 * someone pushed it or opened its menu).
 */
public final class ProgressWatch {
	/** 5 seconds without getting closer */
	public static final int GIVE_UP_TICKS = 100;
	/** before the same goal looks for work again */
	public static final int RETRY_DELAY = 60;

	private double best = Double.MAX_VALUE;
	private int idle;

	public void reset() {
		this.best = Double.MAX_VALUE;
		this.idle = 0;
	}

	/** True once the distance has not shrunk for {@link #GIVE_UP_TICKS} ticks */
	public boolean stuck(double distance) {
		if (distance < this.best - 0.25D) {
			this.best = distance;
			this.idle = 0;
		} else {
			this.idle++;
		}
		return this.idle > GIVE_UP_TICKS;
	}
}
