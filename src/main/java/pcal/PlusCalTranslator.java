package pcal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.mayreh.intellij.plugin.util.StringUtil;

import lombok.Value;
import util.ToolIO;

public class PlusCalTranslator {
    @Value
    public static class Result {
        boolean success;
        List<String> errors;
        String translated;
    }

    public static @NotNull Result translate(@NotNull String text) {
        List<String> lines = text.lines().collect(Collectors.toList());

        ToolIO.reset();
        ToolIO.setMode(ToolIO.TOOL);
        PcalParams.resetParams();

        List<String> translated = trans.performTranslation(lines);
        if (translated != null) {
            return new Result(true,
                              Arrays.asList(ToolIO.getAllMessages()),
                              StringUtil.joinLines(translated));
        }

        return new Result(false,
                          Arrays.asList(ToolIO.getAllMessages()),
                          null);
    }
}
