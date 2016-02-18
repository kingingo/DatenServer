package dev.wolveringer.dataserver.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.commons.lang3.ObjectUtils;
import org.fusesource.jansi.AnsiConsole;

import dev.wolveringer.dataserver.Main;

@SuppressWarnings("deprecation")
public class CostumSystemPrintStream extends PrintStream {
	PrintStream out;

	public CostumSystemPrintStream() {
		super(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new RuntimeException("error 001");
			}
		});
		out = AnsiConsole.out;
	}

	public int hashCode() {
		return out.hashCode();
	}

	public void write(byte[] b) throws IOException {
		
	}

	public boolean equals(Object obj) {
		return out.equals(obj);
	}

	public String toString() {
		return out.toString();
	}

	public void flush() {}

	public void close() {}
	public boolean checkError() {
		return out.checkError();
	}

	public void write(int b) {
		out.println(b);
	}

	public void write(byte[] buf, int off, int len) {}

	public void print(boolean b) {
		out.println(b);
	}

	public void print(char c) {
		out.println(c);
	}

	public void print(int i) {
		out.println(i);
	}

	public void print(long l) {
		out.println(l);
	}

	public void print(float f) {
		out.println(f);
	}

	public void print(double d) {
		out.println(d);
	}

	public void print(char[] s) {
		out.println(s);
	}

	public void print(String s) {
		out.println(s);
	}

	public void print(Object obj) {
		out.println(obj);
	}

	public void println() {
		Main.getConsoleWriter().write("");
	}

	public void println(boolean x) {
		Main.getConsoleWriter().write(x+"");
	}

	public void println(char x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public void println(int x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public void println(long x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public void println(float x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public void println(double x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public void println(char[] x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public void println(String x) {
		Main.getConsoleWriter().write("["+Debugger.getLastCallerClass()+"] "+ObjectUtils.toString(x));
	}

	public void println(Object x) {
		Main.getConsoleWriter().write(ObjectUtils.toString(x));
	}

	public PrintStream printf(String format, Object... args) {
		Main.getConsoleWriter().write(String.format(format, args));
		return this;
	}

	public PrintStream printf(Locale l, String format, Object... args) {
		Main.getConsoleWriter().write(String.format(l,format, args));
		return this;
	}

	public PrintStream format(String format, Object... args) {
		return this;
	}

	public PrintStream format(Locale l, String format, Object... args) {
		return this;
	}

	public PrintStream append(CharSequence csq) {
		return this;
	}

	public PrintStream append(CharSequence csq, int start, int end) {
		return this;
	}

	public PrintStream append(char c) {
		return this;
	}
}
