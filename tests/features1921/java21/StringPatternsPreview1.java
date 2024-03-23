import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.lang.StringTemplate.RAW;
import static java.util.FormatProcessor.FMT;

/**
 * Examples taken from <a href="https://openjdk.org/jeps/430">JEP 430</a>
 */
public class StringPatternsPreview1 {
  public static void main(String[] args) {
    // Embedded expressions can be strings
    String firstName = "Bill", lastName = "Duck";
    System.out.println(STR."\{firstName} \{lastName}");

    // Embedded expressions can perform arithmetic
    int x = 10, y = 20;
    System.out.println(STR."\{x} + \{y} = \{x + y}");

    // Embedded expressions can invoke methods and access fields
    System.out.println(STR."You have a \{getOfferType()} waiting for you!");
    Request req = new Request();
    System.out.println(STR."Access at \{req.date} \{req.time} from \{req.ipAddress}");

    // Embedded expressions can use double quotes without escaping them
    String filePath = "_dummy.dat";
    File file = new File(filePath);
    System.out.println(STR."The file \{filePath} \{file.exists() ? "does" : "does not"} exist");

    // Embedded expressions can span multiple lines
    System.out.println(
      STR."The time is \{
        DateTimeFormatter
          .ofPattern("HH:mm:ss")
          .format(LocalTime.of(11, 11, 11))
        } or roughly eleven after eleven"
    );

    // Embedded expressions can be nested
    String[] fruit = { "apples", "oranges", "peaches" };
    System.out.println(STR."\{fruit[0]}, \{STR."\{fruit[1]}, \{fruit[2]}"}\n");

    // Embedded expressions can be used in multi-line strings
    String title = "My Web Page";
    String text = "Hello, world";
    String html = STR."""
      <html>
        <head>
          <title>\{title}</title>
        </head>
        <body>
          <p>\{text}</p>
        </body>
      </html>
      """;
    System.out.println(html);

    // The FMT template processor interprets format specifiers which appear to the left of embedded expressions.
    // The format specifiers are the same as those defined in java.util.Formatter.
    Rectangle[] zone = new Rectangle[] {
      new Rectangle("Alfa", 17.8, 31.4),
      new Rectangle("Bravo", 9.6, 12.4),
      new Rectangle("Charlie", 7.1, 11.23),
    };
    String table = FMT."""
      Description     Width    Height     Area
      %-12s\{zone[0].name}  %7.2f\{zone[0].width}  %7.2f\{zone[0].height}     %7.2f\{zone[0].area()}
      %-12s\{zone[1].name}  %7.2f\{zone[1].width}  %7.2f\{zone[1].height}     %7.2f\{zone[1].area()}
      %-12s\{zone[2].name}  %7.2f\{zone[2].width}  %7.2f\{zone[2].height}     %7.2f\{zone[2].area()}
      \{" ".repeat(28)} Total %7.2f\{zone[0].area() + zone[1].area() + zone[2].area()}
      """;
    System.out.println(table);

    // Built-in security: Each template expression needs to pass through a processor.
    String name = "Joan";
    StringTemplate stringTemplate = RAW."My name is \{name}";
    String processedTemplate = STR.process(stringTemplate);
    System.out.println(processedTemplate);
  }

  static Object getOfferType() {
    return "special New Year's sale discount";
  }

  static class Request {
    LocalDate date;
    LocalTime time;
    InetAddress ipAddress;

    Request() {
      LocalDateTime dateTime = LocalDateTime.of(2011, 11, 11, 11, 11, 11);
      date = dateTime.toLocalDate();
      time = dateTime.toLocalTime();
      try {
        // localhost/127.0.0.1
        ipAddress = InetAddress.getByAddress("localhost", new byte[] { 127, 0, 0, 1 });
      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      }
    }
  }

  record Rectangle(String name, double width, double height) {
    double area() {
      return width * height;
    }
  }
}
