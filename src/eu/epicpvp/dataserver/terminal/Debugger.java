package eu.epicpvp.dataserver.terminal;

public class Debugger {
	public static void printMessage(String message){
		System.out.println("[Debugger] ["+getLastCallerClass()+"] -> "+message);
	}

	public static String getLastCallerClass(){
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement currunt : stack) {
			if (!currunt.getClassName().equalsIgnoreCase(Debugger.class.getName()) && !currunt.getClassName().equalsIgnoreCase("java.lang.Thread") && !currunt.getClassName().contains("eu.epicpvp.dataserver.terminal.")) {
				try {
					return Class.forName(currunt.getClassName()).getSimpleName() + ":" + currunt.getLineNumber();
				} catch (ClassNotFoundException e) {
					return currunt.getClassName() + ":" + currunt.getLineNumber();
				}
			}
		}
		return "unknown:-1";
	}
}
