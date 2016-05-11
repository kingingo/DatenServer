package dev.wolveringer.dataserver.terminal.commands;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage.TargetType;
import dev.wolveringer.dataserver.terminal.ArgumentList;
import dev.wolveringer.dataserver.terminal.ArgumentList.Argument;
import dev.wolveringer.dataserver.terminal.ChatColor;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandBroadcast implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		if(args.length == 0){
			writer.sendMessage("§cEmpty message is not allowed!");
		}
		for(Client c : ServerThread.getBungeecords())
			c.writePacket(new PacketChatMessage(ChatColor.translateAlternateColorCodes('&', StringUtils.join(args," ")), new PacketChatMessage.Target[]{new PacketChatMessage.Target(TargetType.BROTCAST, null, null)}));
		writer.sendMessage("Breadcast: "+ChatColor.translateAlternateColorCodes('&', StringUtils.join(args," ")));
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/broad <message>", "Broadcast a message")).build();
	}

}
