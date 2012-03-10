package com.imdeity.tpeh.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;

public class TeleportCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Deity.perm.has(player, "deity.teleport.teleport") || Deity.perm.isLeastModerator(player)) {
				return wrapCommand(player, args);
			}
			return false;
		}
		return false;
	}

	private boolean wrapCommand(Player player, String[] split) {
		if (split.length == 1) {
			Player teleportee = Deity.server.getOnlinePlayer(split[0]);
			if (teleportee != null && teleportee.isOnline()) {
				if (Deity.perm.isAdmin(teleportee) || Deity.perm.isSubAdmin(teleportee)) {
					Deity.chat.sendPlayerError(player, "Teleport", "You cannot teleport to admins.");
					return false;
				}
				if (teleportee.getWorld().getName().equalsIgnoreCase("events")) {
					Deity.chat.sendPlayerError(player, "Teleport", "You cannot teleport to the event world");
					return false;
				}
				if (player.getWorld().getName().equalsIgnoreCase("events")) {
					Deity.chat.sendPlayerError(player, "Teleport", "You cannot teleport while in the event world");
					return false;
				}
				this.executeTeleport(player, teleportee);
				return true;
			}
		} else
			Deity.chat.sendPlayerMessage(player, "Teleport", "Sorry but you there was an error in that command.");
		return false;
	}

	public void executeTeleport(Player teleporter, Player teleportee) {
		if (Deity.player.teleport(teleporter, teleportee.getLocation())) {
			Deity.chat.sendPlayerMessage(teleporter, "Teleport", "You teleported to " + teleportee.getName() + ".");
			Deity.chat.sendPlayerMessage(teleportee, "Teleport", teleporter.getName() + " teleported to you.");
			String sql = "INSERT INTO " + Deity.data.getDB().tableName("deity_", "teleports") + " (`type`, `teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?, ?);";
			Deity.data.getDB().Write(sql, "tp", teleporter.getName(), teleportee.getName(), 1);
		}
	}
}
