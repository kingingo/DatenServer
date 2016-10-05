package eu.epicpvp.dataserver.terminal;

public class Debugger {
	public static void printMessage(String message){
		System.out.println("[Debugger] ["+getLastCallerClass()+"] -> "+message);
	}
	
	public static String getLastCallerClass(){
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stack.length; i++) {
			StackTraceElement currunt = stack[i];
			if(!currunt.getClassName().equalsIgnoreCase(Debugger.class.getName()) && !currunt.getClassName().equalsIgnoreCase("java.lang.Thread") && !currunt.getClassName().contains("eu.epicpvp.dataserver.terminal.")){
				return currunt.getClassName()+":"+currunt.getLineNumber();
			}
		}
		return "unknown:-1";
	}
}
