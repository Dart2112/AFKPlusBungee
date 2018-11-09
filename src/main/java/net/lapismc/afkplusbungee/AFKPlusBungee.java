package net.lapismc.afkplusbungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKPlusAPI;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public final class AFKPlusBungee extends JavaPlugin implements PluginMessageListener, Listener {

    private AFKPlus plugin;

    @Override
    public void onEnable() {
        plugin = new AFKPlusAPI().getPlugin();
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        getLogger().info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
    }

    @EventHandler
    public void onAFKStart(AFKStartEvent e) {
        String name = e.getPlayer().getName();
        Player player = Bukkit.getPlayer(e.getPlayer().getUUID());
        player.sendPluginMessage(this, "BungeeCord", compileState(name, "Start"));
    }

    @EventHandler
    public void onAFKStop(AFKStopEvent e) {
        String name = e.getPlayer().getName();
        Player player = Bukkit.getPlayer(e.getPlayer().getUUID());
        player.sendPluginMessage(this, "BungeeCord", compileState(name, "Stop"));
    }

    private void broadcastStart(String name) {
        Bukkit.broadcastMessage(plugin.config.getMessage("Broadcast.Start").replace("{PLAYER}", name));
    }

    private void broadcastStop(String name) {
        Bukkit.broadcastMessage(plugin.config.getMessage("Broadcast.Stop").replace("{PLAYER}", name));
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        getLogger().info(channel + ": Message received");
        if (!channel.equals("BungeeCord")) {
            return;
        }
        String sentMessage = getMessage(bytes);
        if (sentMessage == null)
            return;
        String[] message = sentMessage.split(":");
        String name = message[0];
        String state = message[1];
        if (state.equalsIgnoreCase("Start"))
            broadcastStart(name);
        if (state.equalsIgnoreCase("Stop"))
            broadcastStop(name);
    }


    private byte[] compileState(String username, String state) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("AFKPlus");
        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(msgBytes);
        try {
            dataOutputStream.writeUTF(username + ":" + state);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
        return out.toByteArray();
    }

    private String getMessage(byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (!subChannel.equalsIgnoreCase("AFKPlus")) {
            return null;
        }
        short len = in.readShort();
        byte[] msgBytes = new byte[len];
        in.readFully(msgBytes);
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(msgBytes));
        try {
            return dataInputStream.readUTF(); // Read the data in the same way you wrote it
        } catch (IOException e) {
            return null;
        }
    }
}
