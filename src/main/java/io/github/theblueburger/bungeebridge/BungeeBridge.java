package io.github.theblueburger.bungeebridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class BungeeBridge extends Plugin {
    static ProxyServer proxyServer;
    static Configuration config;
    static JDA jda;
    static Logger logger;
    static String guildId;
    static String channelID;
    static boolean discordEnabled;
    static File configFilePath;
    static HashMap<String, Integer> recentMessagesCount;
    static List<String> mutes; // can be UUID or a number, UUID = mc player, num = discord id
    static long resetRatelimitAt;
    static void resetRatelimit() {
        resetRatelimitAt = Instant.now().toEpochMilli();
        recentMessagesCount = new HashMap<String, Integer>();
    }
    @Override
    public void onEnable() {
        resetRatelimit();
        // Plugin startup logic
        proxyServer = getProxy();
        logger = getSLF4JLogger();
        try {
            makeConfig();
        } catch (IOException e) {
            logger.error("Cant make config");
            e.printStackTrace();
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            logger.error("Cant load config");
            e.printStackTrace();
        }

        logger.info("Starting BungeeBridge");
        logger.info("Made by TheBlueBurger");
        getProxy().getPluginManager().registerListener(this, new Events());
        mutes = config.getStringList("mutes");
        if(config.getBoolean("discord_bot.enabled")) {
            logger.info("Logging in to Discord");
            JDABuilder builder = JDABuilder.create(config.getString("discord_bot.token"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS);
            builder.setActivity(Activity.watching("Minecraft chat"));
            jda = builder.build();
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                logger.error("Cannot login to discord");
                e.printStackTrace();
                return;
            }
            guildId = config.getString("discord_bot.guild_id");
            channelID = config.getString("discord_bot.channel_id");
            jda.addEventListener(new DiscordListeners());
            logger.info("logged into discord");
        }
        configFilePath = new File(getDataFolder(), "config.yml");
        proxyServer.getPluginManager().registerCommand(this, new BMuteCommand());
        proxyServer.getPluginManager().registerCommand(this, new BUnmuteCommand());
    }
    static void saveMutesToConfig() {
        config.set("mutes", mutes);
    }
    static void saveConfig() throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFilePath);
    }

    static boolean isMutedMinecraft(ProxiedPlayer p) {
        String uuid = p.getUniqueId().toString();
        return mutes.contains(uuid);
    }
    static boolean isMutedDiscord(User u) {
        String id = u.getId();
        return mutes.contains(id);
    }

    static boolean allowedToBroadcast(String sender) {
        if((resetRatelimitAt + 5000) < Instant.now().toEpochMilli()) resetRatelimit();
        if(!recentMessagesCount.containsKey(sender)) return true;
        int currentCount = recentMessagesCount.get(sender);
        return currentCount < 5;
    }

    static void addMessageCount(String sender) {
        if(!recentMessagesCount.containsKey(sender)) {
            recentMessagesCount.put(sender, 1);
            return;
        }
        int currentCount = recentMessagesCount.get(sender);
        recentMessagesCount.put(sender, currentCount+1);
    }

    static void sendDiscordChat(String msg) {
        Thread thread = new Thread(() -> jda.getChannelById(TextChannel.class, channelID).sendMessage(MessageCreateBuilder.from(MessageCreateData.fromContent(msg)).setAllowedMentions(Collections.singleton(Message.MentionType.USER)).build()).complete());
        thread.start();
    }


    static void broadcast(String source, String sender, String message) {
        String str = String.format("[%s] %s: %s",
                source,
                sender,
                message);
        logger.debug("Received message from source {}, author {}, message: {}", source, sender, message);
        broadcastRaw(str, source);
    }

    static void broadcastRaw(String str, @Nullable String exceptServer) {
        BungeeBridge.proxyServer.getServersCopy().forEach((s, serverInfo) -> {
            if(serverInfo.getName().equals(exceptServer)) return;
            serverInfo.getPlayers().forEach(otherPlayer -> {
                otherPlayer.sendMessage(new TextComponent(str));
            });
        });
        if(exceptServer != null == !exceptServer.equals("discord")) {
            sendDiscordChat(str);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public void makeConfig() throws IOException {
        // Create plugin config folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "config.yml");

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
            InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
            in.transferTo(outputStream); // Throws IOException
        }
    }
}
