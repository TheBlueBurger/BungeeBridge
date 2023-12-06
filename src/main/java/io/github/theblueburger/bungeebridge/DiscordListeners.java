package io.github.theblueburger.bungeebridge;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static io.github.theblueburger.bungeebridge.BungeeBridge.*;

public class DiscordListeners extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent e) {
        logger.info("Discord connected");
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(!e.isFromGuild()) return;
        if(!e.getGuild().getId().equals(guildId)) return;
        if(!e.getChannel().getId().equals(channelID)) return;
        if(e.getAuthor().isBot()) return;
        Member member = e.getMember();
        if(member == null) return;
        if(isMutedDiscord(e.getAuthor())) return;
        if(!allowedToBroadcast(e.getAuthor().getId())) return;
        addMessageCount(e.getAuthor().getId());
        BungeeBridge.broadcast("discord", member.getEffectiveName(), e.getMessage().getContentDisplay());
    }
}
