package eu.epicpvp.dataserver.terminal.commands;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.connection.server.ServerThread;
import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.dataserver.protocoll.packets.PacketChatMessage;
import eu.epicpvp.dataserver.protocoll.packets.PacketChatMessage.TargetType;
import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.ChatColor;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;

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
