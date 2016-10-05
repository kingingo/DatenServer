package eu.epicpvp.dataserver.terminal.commands;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.dataserver.player.OnlinePlayer;
import eu.epicpvp.dataserver.player.PlayerManager;
import eu.epicpvp.dataserver.protocoll.packets.PacketChatMessage;
import eu.epicpvp.dataserver.protocoll.packets.PacketChatMessage.TargetType;
import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.ChatColor;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;

public class CommandSendMessage implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		if(args.length<2){
			writer.sendMessage("§cEmpty player/message");
			return;
		}
		OnlinePlayer player = null;
		if(args[0].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
			player = PlayerManager.getPlayer(UUID.fromString(args[0]));
		}
		else if(args[0].length()<=16){
			player = PlayerManager.getPlayer(args[0]);
		}
		if(player == null){
			writer.sendMessage("§cPlayer \""+args[0]+"\" not found");
			return;
		}
		if(!player.isLoaded() || !player.isPlaying()){
			writer.sendMessage("§cPlayer isnt online! ("+(player.isLoaded()?player.isPlaying()?"Undef":"Not playing":"Not loaded")+")");
			return;
		}
		player.getPlayerBungeecord().writePacket(new PacketChatMessage(ChatColor.translateAlternateColorCodes('&', StringUtils.join(Arrays.copyOfRange(args, 1,args.length)," ")), new PacketChatMessage.Target[]{new PacketChatMessage.Target(TargetType.PLAYER, null, player.getUuid().toString())}));
		writer.sendMessage("§aMessage send. Your message: §6"+StringUtils.join(Arrays.copyOfRange(args, 1,args.length)," "));
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/sendMessage <Player/UUID> <Messgage>", "Send a message to a player")).build();
	}

}
