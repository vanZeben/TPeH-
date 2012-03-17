package com.imdeity.tpeh.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;

public class SummonCommand implements CommandExecutor {

	public void executeTeleport(Player teleporter, Player teleportee) {
		if (Deity.player.teleport(teleportee, teleporter.getLocation())) {
			Deity.chat.sendPlayerMessage(teleporter, "Teleport",
					"You teleported " + teleportee.getName() + " to you.");
			String sql = "INSERT INTO "
					+ Deity.data.getDB().tableName("deity_", "teleports")
					+ " (`type`, `teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?, ?);";
			Deity.data.getDB().Write(sql, "tp", teleporter.getName(),
					teleportee.getName(), 1);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Deity.perm.isLeastSubAdmin(player)) {
				return this.wrapCommand(player, args);
			}
			return false;
		}
		return false;
	}

	private boolean wrapCommand(Player player, String[] split) {
		if (split.length == 1) {
			Player teleportee = Deity.server.getOnlinePlayer(split[0]);
			if ((teleportee != null) && teleportee.isOnline()) {
				if (!Deity.perm.isLeastSubAdmin(player)) {
					if (teleportee.getWorld().getName()
							.equalsIgnoreCase("events")) {
						Deity.chat.sendPlayerError(player, "Teleport",
								"You cannot teleport to the event world");
						return false;
					}
					if (player.getWorld().getName().equalsIgnoreCase("events")) {
						Deity.chat.sendPlayerError(player, "Teleport",
								"You cannot teleport while in the event world");
						return false;
					}
				}
				this.executeTeleport(player, teleportee);
				return true;
			}
		} else {
			Deity.chat.sendPlayerMessage(player, "Teleport",
					"Sorry but you there was an error in that command.");
		}
		return false;
	}
}
