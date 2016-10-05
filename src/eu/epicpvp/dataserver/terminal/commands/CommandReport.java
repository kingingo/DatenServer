package eu.epicpvp.dataserver.terminal.commands;

import eu.epicpvp.dataserver.player.PlayerManager;
import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;
import eu.epicpvp.datenserver.definitions.report.ReportEntity;
import eu.epicpvp.report.ReportManager;

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
