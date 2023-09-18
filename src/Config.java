import java.time.*;
import java.util.*;

import static java.util.Map.entry;

class ConfigError extends Error {
    public enum Type {
        MISS_ARG,   // 命令行参数期望参数缺失
        TOO_MANY_ARG, // 命令行获取参数过多
        ERR_ARG_RES, // 参数解析错误
        INPUT_ERROR,    // 从控制台读取输入出错
    }
    public final Type type;
    private static final Map<Type, String> TYPE_MSG_MAP = Map.ofEntries(
            entry(Type.MISS_ARG, "缺少参数"),
            entry(Type.INPUT_ERROR, "读取输入错误"),
            entry(Type.TOO_MANY_ARG, "参数过多")
    );
    public ConfigError(Type type) {
        super("配置出错：" + TYPE_MSG_MAP.get(type));
        this.type = type;
    }
}

/**
 * 解析命令行参数
 * <h1>用法</h1>
 * <ol>
 *    <li>首先通过 <code>addOption</code> 添加 <code>option</code>，该 <code>option</code> 给出所有能匹配的字符串</li>
 *    <li>解析时，当遇到一个命令行参数匹配任意一个 <code>option</code> 对象，则将接下来的尽可能多的命令行参数传给该 <code>option</code> 对象，取决于是否遇到下一个 <code>option</code> 对象或解析到结尾</li>
 *    <li>在所有 <code>args</code> 匹配之前的参数被收集到 <code>args</code> 中</li>
 * </ol>
 */
class Commander {
    static class Option {
        final String[] flags;
        String flag = null;
        String[] args = new String[] {};
        Option(String[] flags) {
            this.flags = flags;
        }
        boolean contains(String anObject) {
            for (var value: this.flags) {
                if (value.equals(anObject)) {
                    return true;
                }
            }
            return false;
        }
    }
    private final ArrayList<Option> options = new ArrayList<>();
    private String[] args = new String[0];
    public Commander() {}
    public void resolve(String[] args) {
        var argList = new ArrayList<String>();
        Option curOption = null;
        for (String arg : args) {
            var match = findMatchOption(arg);
            if (match.isEmpty()) {
                argList.add(arg);
            } else {
                if (curOption != null) {
                    curOption.args = argList.toArray(new String[0]);
                } else {
                    this.args = argList.toArray(new String[0]);
                }
                curOption = match.get();
            }
        }
    }
    Optional<Option> findMatchOption(String arg) {
        for (var option: options) {
            if (option.contains(arg)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    public Option addOption(String[] values) {
        var option = new Option(values);
        options.add(option);
        return option;
    }
}

public final class Config {
    public final LocalDate start;
    public final int columnNum;
    public final int monthNum;

    static Config init(String[] args) throws ConfigError {
        LocalDate start;
        int columnNum;
        int monthNum;
        Commander commander = new Commander();
        // 开始时间
        var startOption = commander.addOption(new String[] {"--start", "-s"});
        // 每列的月份数量
        var columnOption = commander.addOption(new String[] {"--column", "-c"});
        // 打印的月份数
        var monthNumOption = commander.addOption(new String[] {"--month-num", "-m"});
        commander.resolve(args);
        // 处理起始年月
        if (startOption.args.length > 0) {
            if (startOption.args.length == 1) {
                var a = startOption.args[0];
                var year$month = a.split(":");
                var yearScanner = new Scanner(year$month[0]);
                var monthScanner = new Scanner(year$month[1]);
                if (yearScanner.hasNextInt() && monthScanner.hasNextInt()) {
                    try {
                        start = LocalDate.of(yearScanner.nextInt(), monthScanner.nextInt(), 1);
                    } catch (DateTimeException e) {
                        throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                    }
                } else {
                    throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                }
            } else {
                throw new ConfigError(ConfigError.Type.TOO_MANY_ARG);
            }
        } else {
            var inputScanner = new Scanner(System.in);
            System.out.print("请输入起始年份：");
            if (inputScanner.hasNextInt()) {
                var startYear = inputScanner.nextInt();
                System.out.print("请输入起始月份：");
                if (inputScanner.hasNextInt()) {
                    var startMonth = inputScanner.nextInt();
                    try {
                        start = LocalDate.of(startYear, startMonth, 1);
                    } catch (DateTimeException e) {
                        throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                    }
                } else {
                    throw new ConfigError(ConfigError.Type.INPUT_ERROR);
                }
            } else {
                throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
            }
        }
        // 处理列
        if (columnOption.args.length > 0) {
            if (columnOption.args.length != 1) {
                throw new ConfigError(ConfigError.Type.MISS_ARG);
            }
            var columnNumScanner = new Scanner(columnOption.args[0]);
            if (columnNumScanner.hasNextInt()) {
                columnNum = columnNumScanner.nextInt();
                if (columnNum <= 0) {
                    throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                }
            } else {
                throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
            }
        } else {
            var inputScanner = new Scanner(System.in);
            System.out.print("请输入一行要打印的月份数：");
            if (inputScanner.hasNextInt()) {
                columnNum = inputScanner.nextInt();
                if (columnNum <= 0) {
                    throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                }
            } else {
                throw new ConfigError(ConfigError.Type.INPUT_ERROR);
            }
        }
        // 处理要打印的月份数
        if (monthNumOption.args.length > 0) {
            if (monthNumOption.args.length != 1) {
                throw new ConfigError(ConfigError.Type.TOO_MANY_ARG);
            }
            var monthNumScanner = new Scanner(monthNumOption.args[0]);
            if (monthNumScanner.hasNextInt()) {
                monthNum = monthNumScanner.nextInt();
                if (monthNum < 0) {
                    throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                }
            } else {
                throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
            }
        } else {
            var inputScanner = new Scanner(System.in);
            System.out.print("请输入要打印的月份数：");
            if (inputScanner.hasNextInt()) {
                monthNum = inputScanner.nextInt();
                if (monthNum < 0) {
                    throw new ConfigError(ConfigError.Type.ERR_ARG_RES);
                }
            } else {
                throw new ConfigError(ConfigError.Type.INPUT_ERROR);
            }
        }
        return new Config(start, columnNum, monthNum);
    }
    private Config(LocalDate start, int columnNum, int monthNum) {
        this.start = start;
        this.columnNum = columnNum;
        this.monthNum = monthNum;
    }
}
