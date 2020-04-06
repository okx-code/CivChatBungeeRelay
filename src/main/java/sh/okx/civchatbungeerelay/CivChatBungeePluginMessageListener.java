package sh.okx.civchatbungeerelay;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CivChatBungeePluginMessageListener implements Listener {
  private final CivChatBungeeRelayPlugin plugin;

  public CivChatBungeePluginMessageListener(
      CivChatBungeeRelayPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void on(PluginMessageEvent e) {
    if (!e.getTag().equals("CIVCHAT")) {
      return;
    }

    ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());

    String subchannel = in.readUTF();
    if (subchannel.equals("LIST")) {
      if (!(e.getSender() instanceof Server)) {
        return;
      }
      Server server = (Server) e.getSender();
      sendPlayerList(server.getInfo());
    }
  }

  @EventHandler
  public void on(ServerSwitchEvent e) {
    delayedFullUpdate();
  }

  @EventHandler
  public void on(PlayerDisconnectEvent e) {
    delayedFullUpdate();
  }

  private void delayedFullUpdate() {
    ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
      for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
        sendPlayerList(server);
      }
    }, 100, TimeUnit.MILLISECONDS);
  }

  private void sendPlayerList(ServerInfo dest) {
    Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();

    ByteArrayDataOutput reply = ByteStreams.newDataOutput();

    reply.writeUTF("LIST");

    Set<ProxiedPlayer> filtered = new HashSet<>();
    for (ProxiedPlayer player : players) {
      if (player.getServer().getInfo() != dest) {
        filtered.add(player);
      }
    }

    reply.writeInt(filtered.size());

    for (ProxiedPlayer player : filtered) {

      UUID uuid = player.getUniqueId();
      reply.writeLong(uuid.getMostSignificantBits());
      reply.writeLong(uuid.getLeastSignificantBits());
    }

    dest.sendData("CIVCHAT", reply.toByteArray());
  }
}
