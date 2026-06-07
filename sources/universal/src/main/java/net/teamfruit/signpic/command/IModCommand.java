package net.teamfruit.signpic.command;

import java.util.SortedSet;

import javax.annotation.Nonnull;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public interface IModCommand extends ICommand {
	@Nonnull
	String getFullCommandString();

	default int getRequiredPermissionLevel() {
		return 0;
	}

	@Nonnull
	SortedSet<SubCommand> getChildren();

	void printHelp(@Nonnull ICommandSender arg0);
}
