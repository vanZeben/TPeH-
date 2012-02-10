package com.imdeity.tpeh.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;

public class DescendCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Deity.perm.has(player, "deity.descend")) {
				int levels = 1, levels2 = 1;
				if (args.length == 1) {
					try {
						levels = Integer.parseInt(args[0]);
						levels2 = levels;
					} catch (NumberFormatException ex) {
						Deity.chat.sendPlayerError(player, "ImDeity", args[0] + " is not a valid number");
					}
				}
				while (levels > 0) {
					if (!Deity.player.descend(player)) {
						Deity.chat.sendPlayerMessage(player, "ImDeity", "&fSorry you are as low as you can go.");
						return true;
					}
					--levels;
				}
				Deity.chat.sendPlayerMessage(player, "ImDeity", "&fYou have gone down &a" + (levels2 == 1 ? levels2 + " level" : levels2 + " levels"));
				return true;
			} else {
				Deity.chat.sendPlayerError(player, "ImDeity", "Sorry you dont have &4permission");
			}
		}
		return false;
	}
}
