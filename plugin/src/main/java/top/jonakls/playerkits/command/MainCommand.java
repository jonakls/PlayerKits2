package top.jonakls.playerkits.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand extends Command {

    protected MainCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] arguments) {
        if (!(commandSender instanceof Player player)) {

            if (arguments.length != 0) {
                commandSender.sendMessage("You must be a player to use this command.");
                return false;
            }


        }


        return false;
    }
}
