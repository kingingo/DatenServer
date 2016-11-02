package eu.epicpvp.dataserver.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

import eu.epicpvp.dataserver.Main;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

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

	@Override
	public int hashCode() {
		return out.hashCode();
	}

	@Override
	public void write(@NonNull byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public boolean equals(Object obj) {
		return out.equals(obj);
	}

	@Override
	public String toString() {
		return "CP";
	}

	@Override
	public void flush() {}

	@Override
	public void close() {}

	@Override
	public boolean checkError() {
		return false;
	}

	@Override
	public void write(int b) {
		println(b);
	}

	@Override
	public void write(@NonNull byte[] buf, int off, int len) {
		write("§cWritebyte: " + Arrays.toString(ArrayUtils.subarray(buf, off, off + len)));
	}

	@Override
	public void print(boolean b) {
		println(b);
	}

	@Override
	public void print(char c) {
		println(c);
	}

	@Override
	public void print(int i) {
		println(i);
	}

	@Override
	public void print(long l) {
		println(l);
	}

	@Override
	public void print(float f) {
		println(f);
	}

	@Override
	public void print(double d) {
		println(d);
	}

	@Override
	public void print(@NonNull char[] s) {
		println(s);
	}

	@Override
	public void print(String s) {
		println(s);
	}

	@Override
	public void print(Object obj) {
		println(obj);
	}

	@Override
	public void println() {
		write("");
	}

	@Override
	public void println(boolean b) {
		write(Boolean.toString(b));
	}

	@Override
	public void println(char c) {
		write(Character.toString(c));
	}

	@Override
	public void println(int i) {
		write(Integer.toString(i));
	}

	@Override
	public void println(long l) {
		write(Long.toString(l));
	}

	@Override
	public void println(float f) {
		write(Float.toString(f));
	}

	@Override
	public void println(double d) {
		write(Double.toString(d));
	}

	@Override
	public void println(@NonNull char[] ca) {
		write(String.valueOf(ca));
	}

	@Override
	public void println(String x) {
		write("§a[" + Debugger.getLastCallerClass() + "] §f" + ObjectUtils.toString(x));
	}

	@Override
	public void println(Object x) {
		write(ObjectUtils.toString(x));
	}

	@Override
	public PrintStream printf(@NonNull String format, Object... args) {
		write(String.format(format, args));
		return this;
	}

	@Override
	public PrintStream printf(Locale l, @NonNull String format, Object... args) {
		write(String.format(l, format, args));
		return this;
	}

	@Override
	public PrintStream format(@NonNull String format, Object... args) {
		write(String.format(format, args));
		return this;
	}

	@Override
	public PrintStream format(Locale l, @NonNull String format, Object... args) {
		write(String.format(l, format, args));
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq) {
		write("§cAppend: " + csq);
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		write("§cAppend: " + csq);
		return this;
	}

	@Override
	public PrintStream append(char c) {
		write("§cAppend: " + c);
		return this;
	}

	public void write(String message) {
		Main.getConsoleWriter().write(message);
	}
}
