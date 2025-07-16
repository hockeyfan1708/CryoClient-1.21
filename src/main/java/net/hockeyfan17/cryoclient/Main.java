package net.hockeyfan17.cryoclient;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MOD_ID = "cryoclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Text CryoClientName;
	static {
		CryoClientName = Text.literal("")
				.copy()
				.append(Text.literal("[").formatted(Formatting.DARK_GRAY))
				.append(Text.literal("Cryo").formatted(Formatting.AQUA))
				.append(Text.literal("Client").formatted(Formatting.WHITE))
				.append(Text.literal("]").formatted(Formatting.DARK_GRAY))
				.append(Text.literal(" "));
	}

	@Override
	public void onInitialize() {

	}
}