package io.github.theblueburger.bungeebridge;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import static io.github.theblueburger.bungeebridge.BungeeBridge.mutes;

public class BMuteCommand extends Command {
    public BMuteCommand() {
        super("bmute", "bungeebridge.mute", "burgermute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 2) {
            sender.sendMessage(TextComponent.fromLegacyText("Usage:\n/bmute discord 574110505254256640\n/bmute minecraft Notch"));
            return;
        }
        switch(args[0]) {
            case "discord":
            case "d":
                try {
                    Long.parseUnsignedLong(args[1]);
                } catch(Exception ignored) {
                    sender.sendMessage(TextComponent.fromLegacyText("Invalid"));
                    return;
                }
                if(mutes.contains(args[1])) {
                    sender.sendMessage(TextComponent.fromLegacyText("Already muted"));
                    return;
                }
                mutes.add(args[1]);
                saveConfig(sender);
                sender.sendMessage(TextComponent.fromLegacyText("Done"));
                break;
            case "minecraft":
            case "mc":
            case "m":
                ProxiedPlayer p = BungeeBridge.proxyServer.getPlayer(args[1]);
                if(p == null) {
                    sender.sendMessage(TextComponent.fromLegacyText("Not joined!"));
                    return;
                }
                if(mutes.contains(p.getUniqueId().toString())) {
                    sender.sendMessage(TextComponent.fromLegacyText("Already muted"));
                    return;
                }
                mutes.add(p.getUniqueId().toString());
                saveConfig(sender);
                sender.sendMessage(TextComponent.fromLegacyText("Done"));
                break;
            default:
                sender.sendMessage(TextComponent.fromLegacyText("Invalid subcommand"));
        }
    }
    static void saveConfig(CommandSender sender) {
        try {
            BungeeBridge.saveMutesToConfig();
            BungeeBridge.saveConfig();
        } catch(Exception exception) {
            sender.sendMessage(TextComponent.fromLegacyText("Cannot save config!"));
            BungeeBridge.logger.error("Cannot save config!");
            exception.printStackTrace();
        }
    }
}
