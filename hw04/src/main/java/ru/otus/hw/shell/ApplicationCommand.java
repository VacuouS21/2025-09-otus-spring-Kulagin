package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent(value = "Application Commands")
@RequiredArgsConstructor
public class ApplicationCommand {

    private final TestRunnerService testRunnerService;

    @ShellMethod(value = "Start command", key = {"s", "start"})
    public void start() {
        testRunnerService.run();
    }
}
