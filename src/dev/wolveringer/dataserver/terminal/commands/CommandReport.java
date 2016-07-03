package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.terminal.ArgumentList;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.ArgumentList.Argument;
import dev.wolveringer.report.ReportEntity;
import dev.wolveringer.report.ReportManager;

public class CommandReport implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("list")){
				writer.sendMessage("§aOpen Reports:");
				for(ReportEntity e : ReportManager.getInstance().getOpenReports())
					writer.sendMessage("["+e.getReportId()+"] "+PlayerManager.getPlayer(e.getReporter()).getName()+" ("+e.getReporter()+") report against "+PlayerManager.getPlayer(e.getTarget()).getName()+" ("+e.getTarget()+") for reason: "+e.getReson()+" extra reason: "+e.getInfos()+". Report state: "+e.getState()+" Open: "+e.isOpen());
			}
		}
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/report list", "List open reports")).arg(new Argument("/report stats player <Player>", "View report stats of a player.")).arg(new Argument("/report stats global", "View global Report stats.")).build();
	}

}
