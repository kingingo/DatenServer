package dev.wolveringer.dataserver.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.fusesource.jansi.AnsiConsole;

import dev.wolveringer.dataserver.Main;

@SuppressWarnings("deprecation")
public class CostumSystemPrintStream extends PrintStream {
	String buffer;
	public CostumSystemPrintStream() {
		super(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				throw new RuntimeException("error 001");
			}
		});
	}

	public int hashCode() {
		return out.hashCode();
	}

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public boolean equals(Object obj) {
		return out.equals(obj);
	}

	public String toString() {
		return "CP";
	}

	public void flush() {}

	public void close() {}
	public boolean checkError() {
		return false;
	}

	public void write(int b) {
		println(b);
	}

	public void write(byte[] buf, int off, int len) {
		Main.getConsoleWriter().write("§cWritebyte: "+Arrays.toString(ArrayUtils.subarray(buf, off, off+len)));
	}

	public void print(boolean b) {
		println(b);
	}

	public void print(char c) {
		println(c);
	}

	public void print(int i) {
		println(i);
	}

	public void print(long l) {
		println(l);
	}

	public void print(float f) {
		println(f);
	}

	public void print(double d) {
		println(d);
	}

	public void print(char[] s) {
		println(s);
	}

	public void print(String s) {
		println(s);
	}

	public void print(Object obj) {
		println(obj);
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
		Main.getConsoleWriter().write(String.format(format, args));
		return this;
	}

	public PrintStream format(Locale l, String format, Object... args) {
		Main.getConsoleWriter().write(String.format(l,format, args));
		return this;
	}

	public PrintStream append(CharSequence csq) {
		Main.getConsoleWriter().write("§cAppend: "+csq);
		return this;
	}

	public PrintStream append(CharSequence csq, int start, int end) {
		Main.getConsoleWriter().write("§cAppend: "+csq);
		return this;
	}

	public PrintStream append(char c) {
		Main.getConsoleWriter().write("§cAppend: "+c);
		return this;
	}
}
