package cz.los.jr_journal.bot.handler;

import cz.los.jr_journal.bot.BotResponse;
import cz.los.jr_journal.bot.command.Command;
import cz.los.jr_journal.bot.conversation.ConversationKeeper;
import cz.los.jr_journal.bot.conversation.ReportConversation;
import cz.los.jr_journal.model.Attendance;
import cz.los.jr_journal.model.BotUser;
import cz.los.jr_journal.model.Group;
import cz.los.jr_journal.model.JournalEntry;
import cz.los.jr_journal.service.GroupService;
import cz.los.jr_journal.service.JournalEntryService;
import cz.los.jr_journal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static cz.los.jr_journal.bot.command.Command.NEW_GROUP;
import static cz.los.jr_journal.bot.command.Command.REPORT;
import static cz.los.jr_journal.bot.conversation.ReportConversation.ReportConversationStep.*;


@Slf4j
public class ReportHandler extends AbstractCommandHandler implements CommandHandler<SendMessage> {

    private static final String PICK_YEAR = """
            Выеди данные о занятии
            Год
            """;

    private final UserService userService;
    private final GroupService groupService;
    private final JournalEntryService entryService;
    private final ConversationKeeper keeper;

    public ReportHandler(UserService userService, GroupService groupService, JournalEntryService entryService, ConversationKeeper keeper) {
        this.userService = userService;
        this.groupService = groupService;
        this.entryService = entryService;
        this.keeper = keeper;
    }

    @Override
    public BotResponse<SendMessage> handle(Update update) {
        log.info("Handling create report command. {}", update);
        Message message = update.getMessage();
        if (message == null) {
            message = update.getCallbackQuery().getMessage();
        }
        if (message != null && message.isCommand()) {
            return handleFirstStep(message, REPORT, PICK_YEAR, keeper);
        }
        long chatId = message.getChatId();
        if (keeper.conversationExists(chatId)) {
            ReportConversation conversation = (ReportConversation) keeper.get(chatId);
            ReportConversation.ReportConversationStep step = conversation.getReportStep();
            return switch (step) {
                case YEAR -> readYear(update, conversation);
                case MONTH -> readMonth(update, conversation);
                case DAY -> readDay(update, conversation);
                case GROUP -> readGroup(update, conversation);
                case TOPIC -> readTopic(update, conversation);
                case WHO -> readWho(update, conversation);
                case OTHER_MENTOR -> readOtherMentor(update, conversation);
                case WHAT_COULD_BE_IMPROVED_DECISION -> readWhatCouldBeImprovedDecision(update, conversation);
                case WHAT_COULD_BE_IMPROVED_COMMENT -> readWhatCouldBeImprovedComment(update, conversation);
                case COMMENT_DECISION -> readCommentDecision(update, conversation);
                case COMMENT -> readComment(update, conversation);
                case CONFIRMATION -> readConfirmation(update, conversation);
            };
        }
        log.warn("Something went wrong with command. {}", NEW_GROUP);
        keeper.remove(chatId);
        return new BotResponse<>(SendMessage.builder()
                .chatId(chatId)
                .text(OPERATION_ABORTED)
                .build());
    }

    protected BotResponse<SendMessage> handleFirstStep(Message message, Command expectedCommand, String firstMessage,
                                                       ConversationKeeper keeper) {
        log.info("Trying to handle first step...");
        Optional<Command> command = extractCommand(message);
        if (command.stream().anyMatch(it -> it == expectedCommand)) {
            ReportConversation conversation = createConversation(message);
            if (keeper.add(conversation)) {
                conversation.setReportStep(YEAR);
                log.info("First step successful!");
                return pickYear(message);
            }
        }
        log.warn("Command is wrong or conversation exists...");
        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text(WRONG_CONVERSATION_STATE)
                .build());
    }

    private BotResponse<SendMessage> pickYear(Message message) {
        List<InlineKeyboardButton> years = new ArrayList<>();
        int current = Year.now().getValue();
        for (int decrement = 0; decrement < 3; decrement++) {
            InlineKeyboardButton year = new InlineKeyboardButton();
            year.setText(String.valueOf(current - decrement));
            year.setCallbackData("YEAR_" + current);
            years.add(year);
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(years.get(0)));
        rows.add(List.of(years.get(1)));
        rows.add(List.of(years.get(2)));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(message.getChatId())
                .text("Выбери Год")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readYear(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("YEAR_")) {
                String yearString = data.replace("YEAR_", "");
                Year year = Year.of(Integer.parseInt(yearString));
                conversation.setYear(year);
                conversation.setReportStep(MONTH);
                return pickMonth(conversation);
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел года. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickMonth(ReportConversation conversation) {
        List<InlineKeyboardButton> months = new ArrayList<>();
        for (Month month : Month.values()) {
            InlineKeyboardButton monthButton = new InlineKeyboardButton();
            monthButton.setText(month.getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")));
            monthButton.setCallbackData("MONTH_" + month);
            months.add(monthButton);
        }

        // Split the buttons into rows of 7 (one row for each week)
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < months.size(); i += 2) {
            List<InlineKeyboardButton> row = months.subList(i, Math.min(i + 2, months.size()));
            rows.add(row);
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Выбери Месяц")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readMonth(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("MONTH_")) {
                String monthString = data.replace("MONTH_", "");
                Month month = Month.valueOf(monthString);
                conversation.setMonth(month);
                conversation.setReportStep(DAY);
                return pickDay(conversation);
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Не увидел месяца. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickDay(ReportConversation conversation) {
        Year year = conversation.getYear();
        Month month = conversation.getMonth();
        int daysInMonth = month.length(year.isLeap());
        int startOfMonthDayOfWeek = LocalDate.of(year.getValue(), month, 1).getDayOfWeek().getValue();

        // Get the days of the week in the appropriate locale
        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        String[] dayNames = new String[daysOfWeek.length];
        for (int i = 0; i < daysOfWeek.length; i++) {
            dayNames[i] = daysOfWeek[i].getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("ru"));
        }

        // Create the header row
        List<InlineKeyboardButton> headerRow = new ArrayList<>();
        for (String name : dayNames) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(name.toUpperCase());
            button.setCallbackData(" "); // Set the callback data to the full day name
            headerRow.add(button);
        }

        // Create a list of buttons representing days of the month
        List<InlineKeyboardButton> days = new ArrayList<>();
        int currentDay = 1;
        for (int i = 1; i <= 35; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (i >= startOfMonthDayOfWeek && currentDay <= daysInMonth) {
                button.setText(String.valueOf(currentDay));
                button.setCallbackData("DAY_" + currentDay);
                currentDay++;
            } else {
                button.setText(" ");
                button.setCallbackData(" ");
            }
            days.add(button);
        }

        // Split the buttons into rows of 7 (one row for each week)
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(headerRow);
        for (int i = 0; i < days.size(); i += 7) {
            List<InlineKeyboardButton> row = days.subList(i, Math.min(i + 7, days.size()));
            rows.add(row);
        }

        // Create the keyboard markup with the rows of buttons
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);
        return new BotResponse(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text(String
                        .format(
                                """
                                        Выбери День
                                        %s %s
                                        """,
                                month.getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")).toUpperCase(),
                                year))
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readDay(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("DAY_")) {
                String dayString = data.replace("DAY_", "");
                MonthDay day = MonthDay.of(conversation.getMonth(), Integer.parseInt(dayString));
                conversation.setDay(day);
                conversation.setReportStep(GROUP);
                return pickGroup(update.getMessage(), conversation);
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел дня. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickGroup(Message message, ReportConversation conversation) {
        BotUser botUser = conversation.getBotUser();
        userService.enrichWithGroups(botUser);
        if (botUser.getGroups().isEmpty()) {
            keeper.remove(message.getChatId());
            return new BotResponse<>(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("У тебя нет групп, к которым ты привязан. Добавь группу при помощи команды /assign")
                    .build());
        }
        List<InlineKeyboardButton> groups = new ArrayList<>();
        for (Group group : botUser.getGroups()) {
            InlineKeyboardButton groupButton = new InlineKeyboardButton();
            String displayName = group.getDisplayName();
            groupButton.setText(displayName);
            groupButton.setCallbackData("GROUP_" + displayName);
            groups.add(groupButton);
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            List<InlineKeyboardButton> row = groups.subList(i, Math.min(i + 1, groups.size()));
            rows.add(row);
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Выбери Группу")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readGroup(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("GROUP_")) {
                String groupName = data.replace("GROUP_", "").toLowerCase();
                Group group = groupService.findByName(groupName).orElseThrow(() -> {
                    String errorMessage = "Could not fetch group:" + groupName;
                    log.error(errorMessage);
                    return new RuntimeException(errorMessage);
                });
                conversation.setGroup(group);
                conversation.setReportStep(TOPIC);
                return pickTopic(conversation);
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел группы. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickTopic(ReportConversation conversation) {
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Тема занятия?")
                .build());
    }

    private BotResponse<SendMessage> readTopic(Update update, ReportConversation conversation) {
        if (update.hasMessage() && update.getMessage().getText() != null) {
            String topic = update.getMessage().getText().trim();
            conversation.setTopic(topic);
            conversation.setReportStep(WHO);
            return pickWho(conversation);
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел темы занятия. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickWho(ReportConversation conversation) {
        List<InlineKeyboardButton> decisions = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        String yes = "ДА";
        yesButton.setText(yes);
        yesButton.setCallbackData("DECISION_" + yes);
        decisions.add(yesButton);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        String no = "НЕТ";
        noButton.setText(no);
        noButton.setCallbackData("DECISION_" + no);
        decisions.add(noButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(decisions);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Ты проводил/ла занятие?")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readWho(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("DECISION_")) {
                String decision = data.replace("DECISION_", "");
                boolean userConductedClass;
                if (decision.equalsIgnoreCase("да")) {
                    userConductedClass = true;
                } else if (decision.equalsIgnoreCase("нет")) {
                    userConductedClass = false;
                } else {
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text("Не увидел кто проводил занятие. Попробуй еще")
                            .build());
                }
                conversation.setUserConductedClass(userConductedClass);
                conversation.setReportStep(OTHER_MENTOR);
                return pickOtherMentor(conversation);
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел ты ли проводил/ла занятие. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickOtherMentor(ReportConversation conversation) {
        List<InlineKeyboardButton> decisions = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        String yes = "ДА";
        yesButton.setText(yes);
        yesButton.setCallbackData("DECISION_" + yes);
        decisions.add(yesButton);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        String no = "НЕТ";
        noButton.setText(no);
        noButton.setCallbackData("DECISION_" + no);
        decisions.add(noButton);

        InlineKeyboardButton partiallyButton = new InlineKeyboardButton();
        String partially = "ЧАСТИЧНО";
        partiallyButton.setText(partially);
        partiallyButton.setCallbackData("DECISION_" + partially);
        decisions.add(partiallyButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(decisions);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Присутствовал/ла ли на занятии другой/ая метнор/киня?")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readOtherMentor(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("DECISION_")) {
                String decision = data.replace("DECISION_", "");
                Attendance otherMentorAttendance;
                if (decision.equalsIgnoreCase("да")) {
                    otherMentorAttendance = Attendance.YES;
                } else if (decision.equalsIgnoreCase("нет")) {
                    otherMentorAttendance = Attendance.NO;
                } else if (decision.equalsIgnoreCase("частично")) {
                    otherMentorAttendance = Attendance.PARTIALLY;
                } else {
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text("Не увидел кто проводил занятие. Попробуй еще")
                            .build());
                }
                conversation.setOtherMentorAttendance(otherMentorAttendance);
                conversation.setReportStep(WHAT_COULD_BE_IMPROVED_DECISION);
                return pickWhatCouldBeImproved(conversation);
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел был ли на занятии другой/ая метор/киня. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> pickWhatCouldBeImproved(ReportConversation conversation) {
        List<InlineKeyboardButton> decisions = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        String yes = "ДА";
        yesButton.setText(yes);
        yesButton.setCallbackData("DECISION_" + yes);
        decisions.add(yesButton);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        String no = "НЕТ";
        noButton.setText(no);
        noButton.setCallbackData("DECISION_" + no);
        decisions.add(noButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(decisions);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Может можно что-то улучшить?")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readWhatCouldBeImprovedDecision(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("DECISION_")) {
                String decision = data.replace("DECISION_", "");
                boolean couldBeImproved = false;
                if (decision.equalsIgnoreCase("да")) {
                    couldBeImproved = true;
                } else if (decision.equalsIgnoreCase("нет")) {
                    couldBeImproved = false;
                } else {
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text("Не увидел можно ли что-то улучшить или нет. Попробуй еще")
                            .build());
                }
                if (couldBeImproved) {
                    conversation.setReportStep(WHAT_COULD_BE_IMPROVED_COMMENT);
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text("Напиши что можно что-то улучшить?")
                            .build());
                } else {
                    conversation.setReportStep(COMMENT_DECISION);
                    return pickComment(conversation);
                }
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел можно ли что-то улучшить или нет. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> readWhatCouldBeImprovedComment(Update update, ReportConversation conversation) {
        if (update.hasMessage() && update.getMessage().getText() != null) {
            String comment = update.getMessage().getText().trim();
            conversation.setWhatCouldBeImproved(comment);
            conversation.setReportStep(COMMENT_DECISION);
        }
        return pickComment(conversation);
    }

    private BotResponse<SendMessage> pickComment(ReportConversation conversation) {
        List<InlineKeyboardButton> decisions = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        String yes = "ДА";
        yesButton.setText(yes);
        yesButton.setCallbackData("DECISION_" + yes);
        decisions.add(yesButton);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        String no = "НЕТ";
        noButton.setText(no);
        noButton.setCallbackData("DECISION_" + no);
        decisions.add(noButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(decisions);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Есть ли какие-то дополнительные комментарии?")
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readCommentDecision(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("DECISION_")) {
                String decision = data.replace("DECISION_", "");
                boolean otherComment = false;
                if (decision.equalsIgnoreCase("да")) {
                    otherComment = true;
                } else if (decision.equalsIgnoreCase("нет")) {
                    otherComment = false;
                } else {
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text("Не увидел есть ли у тебя дополнительный комментарий или нет. Попробуй еще")
                            .build());
                }
                if (otherComment) {
                    conversation.setReportStep(COMMENT);
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text("Напиши дополнительный комментарий к занятию и школе в целом.")
                            .build());
                } else {
                    conversation.setReportStep(CONFIRMATION);
                    return confirmReport(conversation);
                }
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не увидел есть ли у тебя дополнительный комментарий или нет. Попробуй еще")
                .build());
    }

    private BotResponse<SendMessage> readComment(Update update, ReportConversation conversation) {
        if (update.hasMessage() && update.getMessage().getText() != null) {
            String comment = update.getMessage().getText().trim();
            conversation.setAdditionalComments(comment);
            conversation.setReportStep(CONFIRMATION);
        }
        return confirmReport(conversation);
    }

    private BotResponse<SendMessage> confirmReport(ReportConversation conversation) {
        List<InlineKeyboardButton> decisions = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        String yes = "ДА";
        yesButton.setText(yes);
        yesButton.setCallbackData("DECISION_" + yes);
        decisions.add(yesButton);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        String no = "НЕТ";
        noButton.setText(no);
        noButton.setCallbackData("DECISION_" + no);
        decisions.add(noButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(decisions);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(rows);

        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text(String.format("""
                        Поддтверди создание отчета по занятию
                        %s
                        """, conversation.getSummary()
                ))
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private BotResponse<SendMessage> readConfirmation(Update update, ReportConversation conversation) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            String data = update.getCallbackQuery().getData().trim();
            if (data.startsWith("DECISION_")) {
                String decision = data.replace("DECISION_", "");
                if (decision.equalsIgnoreCase("да")) {
                    keeper.remove(conversation.getChatId());
                    Optional<JournalEntry> entryOptional = entryService.createEntry(conversation);
                    if (entryOptional.isPresent()) {
                        log.info("Journal entry crated successfully! Entry id: {}", entryOptional.get().getEntryId());
                        return new BotResponse<>(SendMessage.builder()
                                .chatId(conversation.getChatId())
                                .text("Занятие сохранено!")
                                .build());
                    } else {
                        throw new RuntimeException("Could not create journal entry!");
                    }
                } else if (decision.equalsIgnoreCase("нет")) {
                    log.info("Aborting journal entry creation due to user's decision...");
                    keeper.remove(conversation.getChatId());
                    return new BotResponse<>(SendMessage.builder()
                            .chatId(conversation.getChatId())
                            .text(OPERATION_ABORTED)
                            .build());
                }
            }
        }
        return new BotResponse<>(SendMessage.builder()
                .chatId(conversation.getChatId())
                .text("Не твоего решения. Попробуй еще")
                .build());
    }

    private ReportConversation createConversation(Message message) {
        ReportConversation conversation = createConversation(message.getChatId());
        Optional<BotUser> userOptional = userService.findUserByTelegramId(message.getFrom().getId());
        BotUser botUser = userOptional.orElseGet(() -> userService.createUser(message).get());
        conversation.setBotUser(botUser);
        return conversation;
    }

    @Override
    protected ReportConversation createConversation(long chatId) {
        return new ReportConversation(chatId);
    }
}
