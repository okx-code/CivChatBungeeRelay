package sh.okx.civchatbungeerelay;

import net.md_5.bungee.api.plugin.Plugin;

public class CivChatBungeeRelayPlugin extends Plugin {

  @Override
  public void onEnable() {
    getProxy().registerChannel("CIVCHAT");
    getProxy().getPluginManager().registerListener(this, new CivChatBungeePluginMessageListener(this));
  }
}
