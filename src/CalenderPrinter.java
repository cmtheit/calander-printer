import java.time.*;
import java.util.Arrays;

public class CalenderPrinter {
    Config config;
    public CalenderPrinter(Config config) {
        this.config = config;
    }
    public void print() {
        var monthDays = new int[Math.min(this.config.columnNum, 12)];
        Arrays.fill(monthDays, 1);
        monthDays[0] = config.start.getDayOfMonth();
        int monthLeft = this.config.monthNum;

    }
}
