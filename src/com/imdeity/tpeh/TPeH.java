package com.imdeity.tpeh;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ca.xshade.bukkit.questioner.Questioner;
import ca.xshade.questionmanager.Option;
import ca.xshade.questionmanager.Question;

import com.imdeity.deityapi.Deity;
import com.imdeity.tpeh.commands.PutCommand;
import com.imdeity.tpeh.commands.SummonCommand;
import com.imdeity.tpeh.commands.TeleportAcceptCommand;
import com.imdeity.tpeh.commands.TeleportAcceptHereCommand;
import com.imdeity.tpeh.commands.TeleportCommand;
import com.imdeity.tpeh.questioner.TPeHQuestionTask;

public class TPeH extends JavaPlugin {

	private Questioner questioner;

	public void appendQuestion(Questioner questioner, Question question)
			throws Exception {
		for (Option option : question.getOptions()) {
			if (option.getReaction() instanceof TPeHQuestionTask) {
				((TPeHQuestionTask) option.getReaction()).setTPeH(this);
			}
		}
		questioner.appendQuestion(question);
	}

	private boolean checkPlugins() {
		Plugin test = this.getServer().getPluginManager()
				.getPlugin("Questioner");

		if ((test != null) && (test instanceof Questioner) && test.isEnabled()) {
			this.questioner = (Questioner) test;
			this.questioner.loadClasses();
			return true;
		}
		return false;
	}

	public Questioner getQuestioner() {
		return this.questioner;
	}

	@Override
	public void onDisable() {
		this.questioner = null;
		Deity.chat.out("[TPeH]", "Disabled!");
	}

	@Override
	public void onEnable() {
		if (this.checkPlugins()) {
			this.registerCommands();
			this.setupDatabase();
			Deity.chat.out("[TPeH]", "Enabled!");
		} else {
			Deity.chat.out("[TPeH]", "Questioner not found, Disabling plugin.");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	private void registerCommands() {
		this.getCommand("teleportaccept").setExecutor(
				new TeleportAcceptCommand(this));
		this.getCommand("teleport").setExecutor(new TeleportCommand());
		this.getCommand("put").setExecutor(new PutCommand());
		this.getCommand("teleporthere").setExecutor(new SummonCommand());
		this.getCommand("teleportaccepthere").setExecutor(
				new TeleportAcceptHereCommand(this));
	}

	private void setupDatabase() {
		if (Deity.data.getDB() != null) {
			Deity.data
					.getDB()
					.Write("CREATE TABLE IF NOT EXISTS "
							+ Deity.data.getDB().tableName("deity_",
									"teleports")
							+ " ("
							+ "`id` INT(16) NOT NULL AUTO_INCREMENT ,"
							+ "`type` VARCHAR(4) NOT NULL ,"
							+ "`teleporter` VARCHAR(16) NOT NULL ,"
							+ "`teleportee` VARCHAR(16) NOT NULL ,"
							+ "`is_allowed` INT(1) NOT NULL ,"
							+ "PRIMARY KEY (`id`),"
							+ "INDEX (`teleporter`),"
							+ "INDEX (`type`)"
							+ ") ENGINE = MYISAM COMMENT =  'Teleporting Record Log';");
		}
	}
}
