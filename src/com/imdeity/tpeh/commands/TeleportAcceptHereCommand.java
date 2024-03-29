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

public class TeleportAcceptHereCommand implements CommandExecutor {

	private TPeH plugin;

	public TeleportAcceptHereCommand(TPeH instance) {
		this.plugin = instance;
	}

	public void executeTeleport(Player teleporter, Player teleportee) {

		List<Option> options = new ArrayList<Option>();
		options.add(new Option("agree", new ConfirmQuestionTask(teleporter,
				teleportee) {
			@Override
			public void run() {
				if (Deity.player.teleport(this.teleportee,
						this.teleporter.getLocation())) {
					Deity.chat.sendPlayerMessage(this.teleporter, "Teleport",
							"You teleported " + this.teleportee.getName()
									+ " to you.");
					Deity.chat.sendPlayerMessage(this.teleportee, "Teleport",
							"You teleported to " + this.teleporter.getName()
									+ ".");
					String sql = "INSERT INTO "
							+ Deity.data.getDB().tableName("deity_",
									"teleports")
							+ " (`type`, `teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?, ?);";
					Deity.data.getDB().Write(sql, "tpah",
							this.teleporter.getName(),
							this.teleportee.getName(), 1);
				}
			}
		}));

		options.add(new Option("deny", new ConfirmQuestionTask(teleporter,
				teleportee) {
			@Override
			public void run() {

				Deity.chat.sendPlayerError(this.teleporter, "Teleport",
						"Sorry the teleport was denied");
				Deity.chat.sendPlayerMessage(this.teleportee, "Teleport",
						"You denied the teleport");
				String sql = "INSERT INTO "
						+ Deity.data.getDB().tableName("deity_", "teleports")
						+ " (`type`, `teleporter`, `teleportee`, `is_allowed`) VALUES (?, ?, ?, ?);";
				Deity.data.getDB()
						.Write(sql, "tpah", this.teleporter.getName(),
								this.teleportee.getName(), 0);
			}
		}));
		Question question = new Question(teleportee.getName(),
				"Do you agree to this teleport?", options);
		try {
			this.plugin.appendQuestion(this.plugin.getQuestioner(), question);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Deity.perm.has(player, "deity.teleport.accepthere")) {
				return this.wrapCommand(player, args);
			} else {
				return false;
			}
		}
		return false;
	}

	private boolean wrapCommand(Player player, String[] split) {
		if (split.length == 1) {
			Player teleportee = Deity.server.getOnlinePlayer(split[0]);
			if ((teleportee != null) && teleportee.isOnline()) {
				if (Deity.perm.isAdmin(teleportee)
						|| Deity.perm.isSubAdmin(teleportee)) {
					Deity.chat.sendPlayerError(player, "Teleport",
							"You cannot summon admins.");
					return false;

				}
				if (!Deity.perm.isLeastModerator(player)) {
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
				Deity.chat.sendPlayerMessage(player,
						"You requested to teleport to " + teleportee.getName());
				Deity.chat.sendPlayerMessage(teleportee, player.getName()
						+ " has requested that you be teleported to him.");
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
