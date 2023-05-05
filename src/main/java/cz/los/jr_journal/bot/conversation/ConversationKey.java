package cz.los.jr_journal.bot.conversation;

import cz.los.jr_journal.bot.command.Command;

public record ConversationKey(long chatId, Command command) {}
