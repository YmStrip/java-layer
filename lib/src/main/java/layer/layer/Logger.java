package layer.layer;

import layer.annotations.Config;
import layer.annotations.LayerClass;
import layer.entity.Layer;
import org.fusesource.jansi.Ansi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

@LayerClass
public class Logger extends Layer {
	@Config()
	String name = "I";
	@Config()
	Ansi.Color infoColor = Ansi.Color.BLUE;
	@Config()
	Ansi.Color infoTimeColor = Ansi.Color.BLUE;
	@Config()
	Ansi.Color infoTextColor = Ansi.Color.WHITE;
	@Config()
	Ansi.Color errColor = Ansi.Color.RED;
	@Config()
	Ansi.Color errTimeColor = Ansi.Color.RED;
	@Config()
	Ansi.Color errTextColor = Ansi.Color.RED;
	@Config()
	Ansi.Color sucColor = Ansi.Color.CYAN;
	@Config()
	Ansi.Color sucTimeColor = Ansi.Color.CYAN;
	@Config()
	Ansi.Color sucTextColor = Ansi.Color.WHITE;
	@Config()
	Ansi.Color warnColor = Ansi.Color.YELLOW;
	@Config()
	Ansi.Color warnTimeColor = Ansi.Color.YELLOW;
	@Config()
	Ansi.Color warnTextColor = Ansi.Color.WHITE;
	
	Object[] slice(Object[] array, int startIndex) {
		try {
			return Arrays.copyOfRange(array, startIndex, array.length);
		} catch (Exception e) {
			return new Object[]{};
		}
	}
	
	Object getIndex(Object[] array, int index) {
		try {
			return array[index];
		} catch (Exception e) {
			return null;
		}
	}
	
	public void log(String type, Object... data) {
		var color = Ansi.Color.WHITE;
		var textColor = Ansi.Color.WHITE;
		var timeColor = Ansi.Color.WHITE;
		var time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
		if (Objects.equals(type, "INFO")) {
			color = infoColor;
			timeColor = infoTimeColor;
			textColor = infoTextColor;
		} else if (Objects.equals(type, "WARN")) {
			color = warnColor;
			timeColor = warnTimeColor;
			textColor = warnTextColor;
		} else if (Objects.equals(type, "SUCCESS")) {
			color = sucColor;
			timeColor = sucTimeColor;
			textColor = sucTextColor;
		} else if (Objects.equals(type, "ERROR")) {
			color = errColor;
			timeColor = errTimeColor;
			textColor = errTextColor;
		} else {
			type = "MESSAGE";
		}
		var colorText = Ansi
			.ansi()
			.fg(timeColor)
			.a("[" + time + "]")
			.fg(color)
			.a("[" + name + "]")
			.fg(color)
			.a("[" + type + "] ")
			.fg(textColor)
			.a(String.format((String) (getIndex(data, 0) == null ? "" : getIndex(data, 0)), slice(data, 1)))
			.reset();
		var loggerText = Ansi
			.ansi()
			//.fg(timeColor)
			.a("[" + time + "]")
			//.fg(color)
			.a("[" + name + "]")
			//.fg(color)
			.a("[" + type + "] ")
			//.fg(textColor)
			.a(String.format((String) (getIndex(data, 0) == null ? "" : getIndex(data, 0)), slice(data, 1)))
			.reset();
		System.out.println(
			colorText
		);
	}
	
	public void info(Object... data) {
		log("INFO", data);
	}
	
	public void warn(Object... data) {
		log("WARN", data);
	}
	
	public void suc(Object... data) {
		log("SUCCESS", data);
	}
	
	public void err(Object... data) {
		log("ERROR", data);
	}
	
	public static void log(String name, String type, String data) {
		var time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
		System.out.println(Ansi.ansi()
			.fg(Ansi.Color.BLUE)
			.a("[" + time + "]")
			.fg(Ansi.Color.BLUE)
			.a("[" + name + "]")
			.fg(Ansi.Color.BLUE)
			.a("[" + type + "] ")
			.fg(Ansi.Color.WHITE)
			.a(data)
			.reset());
	}
	
	public Logger() {
		super();
	}
}
