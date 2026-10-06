package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiRenderer;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.List;
import java.util.Objects;

public final class Minecraft189UiRenderer
        implements UiRenderer {
    private final LegacyUiGraphics graphics;

    public Minecraft189UiRenderer(
            final LegacyUiGraphics graphics) {
        this.graphics = Objects.requireNonNull(
                graphics,
                "graphics");
    }

    @Override
    public void render(
            final RenderFrame frame,
            final UiViewport viewport,
            final List<UiDrawCommand> commands) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(commands, "commands");

        graphics.begin(viewport);
        try {
            renderCommands(commands);
        } finally {
            graphics.end();
        }
    }

    private void renderCommands(
            final List<UiDrawCommand> commands) {
        int index = 0;

        while (index < commands.size()) {
            final UiDrawCommand command =
                    Objects.requireNonNull(
                            commands.get(index),
                            "command");

            final int shapeRunEnd =
                    shapeRunEnd(
                            commands,
                            index);

            if (graphics instanceof LegacyUiBatchGraphics
                    && ((LegacyUiBatchGraphics) graphics)
                    .supportsShapeBatching()
                    && shapeRunEnd - index >= 2) {
                renderShapeBatch(
                        (LegacyUiBatchGraphics) graphics,
                        commands,
                        index,
                        shapeRunEnd);
                index = shapeRunEnd;
                continue;
            }

            renderCommand(command);
            index++;
        }
    }

    private void renderShapeBatch(
            final LegacyUiBatchGraphics batchGraphics,
            final List<UiDrawCommand> commands,
            final int start,
            final int end) {
        batchGraphics.beginShapeBatch();

        RuntimeException failure = null;
        try {
            for (int index = start;
                 index < end;
                 index++) {
                renderShape(
                        Objects.requireNonNull(
                                commands.get(index),
                                "command"));
            }
        } catch (RuntimeException error) {
            failure = error;
            throw error;
        } finally {
            try {
                batchGraphics.endShapeBatch();
            } catch (RuntimeException closeFailure) {
                if (failure != null) {
                    failure.addSuppressed(
                            closeFailure);
                } else {
                    throw closeFailure;
                }
            }
        }
    }

    private void renderCommand(
            final UiDrawCommand command) {
        if (isShape(command)) {
            renderShape(command);
            return;
        }

        if (command instanceof UiClipCommand) {
            final UiClipCommand clip =
                    (UiClipCommand) command;
            graphics.pushClip(
                    clip.bounds().x(),
                    clip.bounds().y(),
                    clip.bounds().width(),
                    clip.bounds().height());
            try {
                renderCommands(
                        clip.commands());
            } finally {
                graphics.popClip();
            }
            return;
        }

        if (command instanceof UiTextCommand) {
            final UiTextCommand text =
                    (UiTextCommand) command;
            graphics.drawText(
                    text.font(),
                    text.x(),
                    text.y(),
                    text.text(),
                    text.argb());
            return;
        }

        throw new UnsupportedUiCommandException(
                command.getClass().getName());
    }

    private void renderShape(
            final UiDrawCommand command) {
        if (command instanceof UiRectCommand) {
            final UiRectCommand rect =
                    (UiRectCommand) command;
            graphics.fillRect(
                    rect.x(),
                    rect.y(),
                    rect.width(),
                    rect.height(),
                    rect.argb());
            return;
        }

        if (command instanceof UiRoundedRectCommand) {
            final UiRoundedRectCommand rect =
                    (UiRoundedRectCommand) command;
            graphics.fillRoundedRect(
                    rect.x(),
                    rect.y(),
                    rect.width(),
                    rect.height(),
                    rect.radius(),
                    rect.argb());
            return;
        }

        if (command instanceof UiOutlineCommand) {
            final UiOutlineCommand outline =
                    (UiOutlineCommand) command;
            graphics.strokeRect(
                    outline.x(),
                    outline.y(),
                    outline.width(),
                    outline.height(),
                    outline.thickness(),
                    outline.argb());
            return;
        }

        throw new IllegalArgumentException(
                "not a shape command: "
                        + command.getClass().getName());
    }

    private static int shapeRunEnd(
            final List<UiDrawCommand> commands,
            final int start) {
        int index = start;
        while (index < commands.size()
                && isShape(
                Objects.requireNonNull(
                        commands.get(index),
                        "command"))) {
            index++;
        }
        return index;
    }

    private static boolean isShape(
            final UiDrawCommand command) {
        return command instanceof UiRectCommand
                || command instanceof UiRoundedRectCommand
                || command instanceof UiOutlineCommand;
    }
}
