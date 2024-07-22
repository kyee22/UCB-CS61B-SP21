package gitlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Control {
    public static void executeInDirectory(String cwd, String[] args) throws IOException, InterruptedException {
        // 构建命令
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(System.getProperty("user.dir"));  // 项目目录的绝对路径
        command.add("gitlet.Main");
        command.addAll(Arrays.asList(args));

        // 创建ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(cwd));

        // 启动进程
        Process process = pb.start();

        // 获取子进程的输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // 获取子进程的错误输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println(line);
            }
        }

        // 等待进程结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Gitlet command failed with exit code " + exitCode);
        }
    }

}
