package net.hockeyfan17.cryoclient.util;

import net.hockeyfan17.cryoclient.CryoConfig;
import net.hockeyfan17.cryoclient.Main;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HelpCommand {
    public static Text Help() {
        return Main.CryoClientName.copy()
                .append(Text.literal(" --- Help Page 1 of 1 ---").formatted(Formatting.AQUA));
    }

    public static Text HidePassengers() {
        String Name = "HidePassengers";


        return yellowText(Name, "Toggles the " + Name + " feature\nHides players riding on your head while in a boat").copy()
                .append(clickCommandToggle(Name, CryoConfig.INSTANCE.hidePassengersToggle));
    }


    public static Text BoatTrail() {
        String Name = "BoatTrail";


        return yellowText(Name, "Toggles the " + Name + " feature\nPlayers will draw a small colored line behind their boats").copy()
                .append(clickCommandToggle(Name, CryoConfig.INSTANCE.boatTrailToggle))


                .append(yellowText(Name + " SetDuration", "Changes how long the BoatTrails last"))
                .append(clickCommandInt("BoatTrail SetDuration ", String.valueOf(CryoConfig.INSTANCE.trailDuration)))


                .append(yellowText(Name + " SetRenderDistance", "Changes how far away the BoatTrails will render"))
                .append(clickCommandInt("BoatTrail SetRenderDistance ", String.valueOf(CryoConfig.INSTANCE.renderDistance)));
    }

    public static Text QuickRace() {
        String Name = "QuickRace";

        return yellowText(Name, "Running this command automatically creates a QuickRace on a random track").copy()
                .append(clickCommandInt(Name + " ", "Laps"))
                .append(clickCommandInt(Name + " ", "Pits"));
    }

    public static Text FullHelp() {
        return Help().copy()
                .append(HidePassengers())
                .append(BoatTrail())
                .append(QuickRace());
    }

    // =================================================================================================================================================

    private static Text yellowText(String text, String hover){
        return Text.literal("\n     /").setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover).formatted(Formatting.WHITE)))).copy()
                .append(Text.of(Main.CryoCommand).copy().setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover).formatted(Formatting.WHITE)))))
                .append(Text.literal(" " + text + " ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover).formatted(Formatting.WHITE)))
                ));
    }

    private static Text clickCommandToggle(String command, boolean toggle){
        String colour = toggle ? "GREEN" : "RED";
        String Setting = toggle ? "Enabled" : "Disabled";
        String text = "[" + Setting + "]";
        return clickCommand(text, colour, command, false);
    }

    private static Text clickCommandInt(String command, String number){
        String text = "[" + number + "]";
        return clickCommand(text, "GREEN", command, true);
    }

    private static Text clickCommand(String text, String colour, String command, boolean ifInt){
        if (ifInt) {
            return Text.literal(text + " ").setStyle(Style.EMPTY.withColor(Formatting.valueOf(colour))
                    .withBold(true)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Change")))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ("/CryoClient " + command))));
        } else {
            return Text.literal(text + " ").setStyle(Style.EMPTY.withColor(Formatting.valueOf(colour))
                    .withBold(true)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Toggle")))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/CryoClient " + command))));
        }
    }
}