package eu.epicpvp.dataserver.terminal;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public class ArgumentList {
	@Getter
	public static class Argument {
		private String argument;
		private String message;
		private boolean outdated;
		
		public Argument(String argument, String message, boolean outdated) {
			this.argument = argument;
			this.message = message;
			this.outdated = outdated;
		}
		
		public Argument(String argument, String message) {
			this.argument = argument;
			this.message = message;
		}
		
		public String format(){
			return argument+" >> "+(outdated?"§c":"§a")+message;
		}
	}
	@Singular
	private List<Argument> args = new ArrayList<>();
	
	public List<Argument> getArguments(){
		return args;
	}
}
