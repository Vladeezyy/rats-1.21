package com.github.alexthe666.rats;

import com.github.alexthe666.rats.client.ClientConfig;
import com.github.alexthe666.rats.server.ServerConfig;
import dev.vladeezyy.rats.platform.ConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class ConfigHolder {

	public static final ConfigSpec CLIENT_SPEC;
	public static final ConfigSpec SERVER_SPEC;
	static final ClientConfig CLIENT;
	static final ServerConfig SERVER;

	static {
		{
			final Pair<ClientConfig, ConfigSpec> specPair = new ConfigSpec.Builder().configure(ClientConfig::new);
			CLIENT = specPair.getLeft();
			CLIENT_SPEC = specPair.getRight();
		}
		{
			final Pair<ServerConfig, ConfigSpec> specPair = new ConfigSpec.Builder().configure(ServerConfig::new);
			SERVER = specPair.getLeft();
			SERVER_SPEC = specPair.getRight();
		}
	}
}