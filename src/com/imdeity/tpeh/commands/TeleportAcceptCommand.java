package com.imdeity.tpeh.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;

import com.imdeity.deityapi.Deity;
import com.imdeity.tpeh.TPeH;
import com.imdeity.tpeh.questioner.ConfirmQuestionTask;

public class TeleportAcceptCommand implements CommandExecutor {

	private TPeH plugin;

	public TeleportAcceptCommand(TPeH instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Deity.perm.has(player, "deity.teleport.accept")) {
				return wrapCommand(player, args);
			} else {
				Deity.chat.sendPlayerError(player, "Teleport",
						"Insufficient Vespene Gas.");
			}
		}
		return false;
	}

	private boolean wrapCommand(Player player, String[] split) {
		if (split.length == 1) {
			Player teleportee = Deity.server.getOnlinePlayer(split[0]);
			if (teleportee != null && teleportee.isOnline()) {
				if (Deity.perm.isAdmin(teleportee)
						|| Deity.perm.isSubAdmin(teleportee)) {
					Deity.chat.sendPlayerError(player, "Teleport",
							"You cannot teleport to admins.");
					return false;
				}
				if (teleportee.getWorld().getName().equalsIgnoreCase("events")) {
					Deity.chat.sendPlayerError(player, "Teleport",
							"You cannot teleport to the event world");
					return false;
				}
				if (player.getWorld().getName().equalsIgnoreCase("events")) {
					Deity.chat.sendPlayerError(player, "Teleport",
							"You cannot teleport while in the event world");
					return false;
				}
				Deity.chat.sendPlayerMessage(teleportee, player.getName()
						+ " has requested to teleport to you.");
				this.executeTeleport(player, teleportee);
				return true;
			}
		} else
			Deity.chat.sendPlayerMessage(player, "Teleport",
					"Sorry but you there was an error in that command.");
		return false;
	}

	public void executeTeleport(Player teleporter, Player teleportee) {

		List<Option> options = new ArrayList<Option>();
		options.add(new Option("agree", new ConfirmQuestionTask(teleporter,
				teleportee) {
			@Override
			public void run() {
				Deity.chat.sendPlayerMessage(teleporter, "Teleport",
						"You teleported to " + teleportee.getName() + ".");
				Deity.chat.sendPlayerMessage(teleportee, "Teleport",
						teleporter.getName() + " teleported to you.");

				Deity.player.teleportAsCommand(teleporter, teleportee.getLocation());
				String sql = "INSERT INTO "
						+ Deity.data.getDB().tableName("deity_", "teleports")
						+ " (`type`, `teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?, ?);";
				Deity.data.getDB().Write(sql, "tpa", teleporter.getName(),
						teleportee.getName(), 1);
			}
		}));

		options.add(new Option("deny", new ConfirmQuestionTask(teleporter,
				teleportee) {
			@Override
			public void run() {
				Deity.chat.sendPlayerError(teleporter, "Teleport",
						"Sorry the teleport was denied");
				Deity.chat.sendPlayerMessage(teleportee, "Teleport",
						"You denied the teleport");
				String sql = "INSERT INTO "
						+ Deity.data.getDB().tableName("deity_", "teleports")
						+ " (`type`, `teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?, ?);";
				Deity.data.getDB().Write(sql, "tpa", teleporter.getName(),
						teleportee.getName(), 0);
			}
		}));
		Question question = new Question(teleportee.getName(),
				"Do you agree to this teleport?", options);
		try {
			plugin.appendQuestion(plugin.getQuestioner(), question);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
