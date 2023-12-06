package io.github.theblueburger.bungeebridge;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import static io.github.theblueburger.bungeebridge.BungeeBridge.mutes;

public class BUnmuteCommand extends Command {
    public BUnmuteCommand() {
        super("bunmute", "bungeebridge.unmute", "burgerunmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1) {
            sender.sendMessage(TextComponent.fromLegacyText("Usage:\n/bunmute 574110505254256640\n/bunmute Notch"));
            return;
        }
        ProxiedPlayer p = BungeeBridge.proxyServer.getPlayer(args[0]);
        if(p == null) {
            if(mutes.contains(args[0])) {
                mutes.remove(args[0]);
                BMuteCommand.saveConfig(sender);
            } else {
                sender.sendMessage(TextComponent.fromLegacyText("not found"));
                return;
            }
        } else {
            mutes.remove(p.getUniqueId().toString());
            BMuteCommand.saveConfig(sender);
        }
        sender.sendMessage(TextComponent.fromLegacyText("Done"));
    }
}
