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

import java.util.UUID;

public final class AFKPlusBungee extends JavaPlugin implements PluginMessageListener, Listener {

    private AFKPlus plugin;
    private UUID id;

    @Override
    public void onEnable() {
        plugin = new AFKPlusAPI().getPlugin();
        id = UUID.randomUUID();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        getLogger().info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
    }

    @EventHandler
    public void onAFKStart(AFKStartEvent e) {
        String name = e.getPlayer().getName();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("AFKPlus");
        out.writeUTF(name);
        out.writeUTF("Start");
        out.writeUTF(id.toString());

        Player player = Bukkit.getPlayer(e.getPlayer().getUUID());
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @EventHandler
    public void onAFKStop(AFKStopEvent e) {
        String name = e.getPlayer().getName();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("AFKPlus");
        out.writeUTF(name);
        out.writeUTF("Stop");
        out.writeUTF(id.toString());

        Player player = Bukkit.getPlayer(e.getPlayer().getUUID());
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    private void broadcastStart(String name) {
        Bukkit.broadcastMessage(plugin.config.getMessage("Broadcast.Start").replace("{PLAYER}", name));
    }

    private void broadcastStop(String name) {
        Bukkit.broadcastMessage(plugin.config.getMessage("Broadcast.Stop").replace("{PLAYER}", name));
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals("AFKPlus")) {
            String name = in.readUTF();
            String state = in.readUTF();
            String serverUUID = in.readUTF();
            if (serverUUID.equals(id.toString()))
                return;
            if (state.equalsIgnoreCase("Start"))
                broadcastStart(name);
            if (state.equalsIgnoreCase("Stop"))
                broadcastStop(name);
        }
    }

}
