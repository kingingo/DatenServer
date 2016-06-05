package dev.wolveringer.dataserver.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

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
		write("§cWritebyte: "+Arrays.toString(ArrayUtils.subarray(buf, off, off+len)));
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
		write("");
	}

	public void println(boolean x) {
		write(x+"");
	}

	public void println(char x) {
		write(ObjectUtils.toString(x));
	}

	public void println(int x) {
		write(ObjectUtils.toString(x));
	}

	public void println(long x) {
		write(ObjectUtils.toString(x));
	}

	public void println(float x) {
		write(ObjectUtils.toString(x));
	}

	public void println(double x) {
		write(ObjectUtils.toString(x));
	}

	public void println(char[] x) {
		write(ObjectUtils.toString(x));
	}

	public void println(String x) {
		write("["+Debugger.getLastCallerClass()+"] "+ObjectUtils.toString(x));
	}

	public void println(Object x) {
		write(ObjectUtils.toString(x));
	}

	public PrintStream printf(String format, Object... args) {
		write(String.format(format, args));
		return this;
	}

	public PrintStream printf(Locale l, String format, Object... args) {
		write(String.format(l,format, args));
		return this;
	}

	public PrintStream format(String format, Object... args) {
		write(String.format(format, args));
		return this;
	}

	public PrintStream format(Locale l, String format, Object... args) {
		write(String.format(l,format, args));
		return this;
	}

	public PrintStream append(CharSequence csq) {
		write("§cAppend: "+csq);
		return this;
	}

	public PrintStream append(CharSequence csq, int start, int end) {
		write("§cAppend: "+csq);
		return this;
	}

	public PrintStream append(char c) {
		write("§cAppend: "+c);
		return this;
	}
	
	public void write(String message){
		Main.getConsoleWriter().write(message);
	}
}
