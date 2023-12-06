package io.github.theblueburger.bungeebridge;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;

public class Events implements Listener {
    static List<String> disallowedCommands = List.of("me", "say", "teammsg", "tell", "msg", "w");
    @EventHandler
    public void onConnect(ServerConnectedEvent e) {
        if(BungeeBridge.isMutedMinecraft(e.getPlayer())) return;
        if(!BungeeBridge.allowedToBroadcast(e.getPlayer().getUniqueId().toString())) return;
        BungeeBridge.addMessageCount(e.getPlayer().getUniqueId().toString());
        BungeeBridge.broadcastRaw(String.format("%s joined %s", e.getPlayer().getDisplayName(), e.getServer().getInfo().getName()), e.getServer().getInfo().getName());
    }
    @EventHandler
    public void onDisconnect(ServerDisconnectEvent e) {
        if(BungeeBridge.isMutedMinecraft(e.getPlayer())) return;
        if(!BungeeBridge.allowedToBroadcast(e.getPlayer().getUniqueId().toString())) return;
        BungeeBridge.addMessageCount(e.getPlayer().getUniqueId().toString());
        BungeeBridge.broadcastRaw(String.format("%s left %s", e.getPlayer().getDisplayName(), e.getTarget().getName()), e.getTarget().getName());
    }
    @EventHandler
    public void onPlayerChat(ChatEvent e) {
        if(e.isCancelled()) return;
        if(!(e.getSender() instanceof ProxiedPlayer player)) return;
        if(e.getMessage().startsWith("/")) {
            if(!BungeeBridge.isMutedMinecraft(player)) return;
            String msg = e.getMessage();
            msg = msg.split(" ")[0];
            msg = msg.replaceAll("minecraft:", "");
            msg = msg.replaceAll("essentials:", "");
            msg = msg.replaceAll("/", "");
            if(disallowedCommands.contains(msg)) e.setCancelled(true);
            return;
        }
        /*
        String str = String.format("[%s] %s: %s",
                player.getServer().getInfo().getName(),
                player.getDisplayName(),
                e.getMessage());
        BungeeBridge.proxyServer.getServersCopy().forEach((s, serverInfo) -> {
            if(serverInfo.getName().equals(player.getServer().getInfo().getName())) return;
            serverInfo.getPlayers().forEach(otherPlayer -> {
                otherPlayer.sendMessage(new TextComponent(str));
            });
        });
        BungeeBridge.sendDiscordChat(str);
        */
        if(BungeeBridge.isMutedMinecraft(player)) {
            player.sendMessage(TextComponent.fromLegacyText("ur muted lol"));
            e.setCancelled(true);
            return;
        }
        if(!BungeeBridge.allowedToBroadcast(player.getUniqueId().toString())) return;
        BungeeBridge.addMessageCount(player.getUniqueId().toString());
        BungeeBridge.broadcast(player.getServer().getInfo().getName(), player.getDisplayName(), e.getMessage());
    }
}
