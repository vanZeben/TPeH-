package com.imdeity.tpeh.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;

public class TeleportCommand implements CommandExecutor {

	public void executeTeleport(Player teleporter, Player teleportee) {
		if (Deity.player.teleport(teleporter, teleportee.getLocation())) {
			Deity.chat.sendPlayerMessage(teleporter, "Teleport",
					"You teleported to " + teleportee.getName() + ".");
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
			if (Deity.perm.has(player, "deity.teleport.teleport")
					|| Deity.perm.isLeastModerator(player)) {
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
					if (Deity.perm.isLeastSubAdmin(teleportee)) {
						Deity.chat.sendPlayerError(player, "Teleport",
								"You cannot teleport to admins.");
						return false;
					}
					if (!Deity.perm.isLeastModerator(player)) {
						if (teleportee.getWorld().getName()
								.equalsIgnoreCase("events")) {
							Deity.chat.sendPlayerError(player, "Teleport",
									"You cannot teleport to the event world");
							return false;
						}
						if (player.getWorld().getName()
								.equalsIgnoreCase("events")) {
							Deity.chat
									.sendPlayerError(player, "Teleport",
											"You cannot teleport while in the event world");
							return false;
						}
					}
				}
				this.executeTeleport(player, teleportee);
				return true;
			} else if (split[0].contains(",")) {
				int x = Integer.parseInt(split[0].split(",")[0]);
				int y = Integer.parseInt(split[0].split(",")[1]);
				int z = Integer.parseInt(split[0].split(",")[2]);
				Location location = new Location(player.getWorld(), x, y, z);
				Deity.player.teleport(player, location);
				Deity.chat.sendPlayerMessage(player, "You teleported to X:" + x
						+ ", Y:" + y + ", Z:" + z);
			} else {
				Deity.chat.sendPlayerMessage(player, "Sorry but " + split[0]
						+ " is not online");
			}
		} else if (split.length == 2) {
			Player teleportee = Deity.server.getOnlinePlayer(split[0]);
			Player teleporter = Deity.server.getOnlinePlayer(split[1]);
			if ((teleportee != null) && teleportee.isOnline()
					&& (teleporter != null) && teleporter.isOnline()) {
				if (!Deity.perm.isLeastSubAdmin(player)) {
					return false;
				}
				this.executeTeleport(player, teleportee);
				return true;
			} else if ((teleportee != null) && teleportee.isOnline()
					&& split[1].contains(",")) {
				int x = Integer.parseInt(split[1].split(",")[0]);
				int y = Integer.parseInt(split[1].split(",")[1]);
				int z = Integer.parseInt(split[1].split(",")[2]);
				Location location = new Location(teleportee.getWorld(), x, y, z);
				Deity.player.teleport(teleportee, location);
				Deity.chat.sendPlayerMessage(player, "You teleported "
						+ teleportee + " to X:" + x + ", Y:" + y + ", Z:" + z);
			} else {
				Deity.chat.sendPlayerMessage(player, "Sorry but " + split[0]
						+ " is not online");
			}
		} else {
			Deity.chat.sendPlayerMessage(player, "Teleport",
					"Sorry but you there was an error in that command.");
		}
		return false;
	}
}
