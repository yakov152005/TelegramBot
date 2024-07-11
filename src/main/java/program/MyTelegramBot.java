package program;

import com.github.javafaker.Faker;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static program.Constants.Text.TOKEN;
import static program.Constants.Text.USER_NAME;

public class MyTelegramBot extends TelegramLongPollingBot {

    private Map<String, Todo> todoMap;
    private List<String> chatIds;
    private Map<String, Integer> mathQuestions;
    private Map<String, Integer> mathQuestionsHard;
    private Map<String, Integer> mathQuestionsHardest;
    private Map<String, String> userStates;
    private Map<String, Integer> userPoints;
    private Map<String, Integer> userLevels;
    private static final AtomicInteger counter = new AtomicInteger();

    public MyTelegramBot() {
        this.todoMap = new HashMap<>();
        this.chatIds = new ArrayList<>();
        this.mathQuestions = new HashMap<>();
        this.mathQuestionsHard = new HashMap<>();
        this.mathQuestionsHardest = new HashMap<>();
        this.userStates = new HashMap<>();
        this.userPoints = new HashMap<>();
        this.userLevels = new HashMap<>();
        sendToAllFollowers();
    }

    public void sendToAllFollowers() {
        new Thread(() -> {
            while (true) {
                Calendar calendar = GregorianCalendar.getInstance();
                if (calendar.get(Calendar.HOUR_OF_DAY) == 2 && calendar.get(Calendar.MINUTE) == 18) {
                    for (String id : this.todoMap.keySet()) {
                        SendMessage sendMessage = new SendMessage(id, "Daniel Zino Maniek");
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = String.valueOf(update.getMessage().getChatId());
            String senderName = update.getMessage().getFrom().getFirstName();
            String senderLastName = update.getMessage().getFrom().getLastName();

            System.out.println("Message from: " + senderName + " " + senderLastName + " - " + messageText);

            switch (messageText) {
                case "/start":
                    sendStartMessage(chatId);
                    break;
                case "/reminders":
                    userStates.put(chatId, "reminder");
                    setReminder(chatId);
                    break;
                case "/chat":
                    userStates.put(chatId, "chat");
                    showChatOptions(chatId);
                    break;
                case "/cancel":
                    cancelAction(chatId);
                    break;
                default:
                    handleUserInput(update);
                    break;
            }
        }
    }

    private void sendStartMessage(String chatId) {
        SendMessage startMessage = new SendMessage(chatId, "Welcome! Choose an option:\n" +
                "/reminders - Set a reminder\n" +
                "/chat - Chat with me");
        try {
            execute(startMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUserInput(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String userState = userStates.getOrDefault(chatId, "");
        switch (userState) {
            case "reminder":
                handleReminderInput(update);
                break;
            case "chat":
                handleChatOptions(update);
                break;
            default:
                SendMessage unknownCommand = new SendMessage(chatId, "I didn't understand that command. Please choose from the menu -- > " +"\n"
                + """
                        /reminders - Set a reminder
                        /chat - Chat with me
                        """);

                try {
                    execute(unknownCommand);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

    private void setReminder(String chatId) {
        SendMessage reminderMessage = new SendMessage(chatId, "What do you want me to remind you?");
        todoMap.put(chatId, new Todo());
        try {
            execute(reminderMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleReminderInput(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        Todo todo = todoMap.get(chatId);
        if (todo.getText() == null) {
            todo.setText(update.getMessage().getText());
            SendMessage timeMessage = new SendMessage(chatId, "When?");
            try {
                execute(timeMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                int seconds = Integer.parseInt(update.getMessage().getText());
                new Thread(() -> {
                    try {
                        Thread.sleep(seconds * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    SendMessage reminderMessage = new SendMessage(chatId, "Do not forget to " + todo.getText() + "!");
                    try {
                        execute(reminderMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                userStates.remove(chatId); // Exit reminder state
                todoMap.remove(chatId); // Remove the reminder after it's set
            } catch (NumberFormatException e) {
                SendMessage errorMessage = new SendMessage(chatId, "Please enter a valid number of seconds.");
                try {
                    execute(errorMessage);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private void showChatOptions(String chatId) {
        SendMessage chatOptions = new SendMessage(chatId, """
                Choose an option:
                /fake. Fake name
                /math. Math exercises
                /exit Exit chat and return to menu""");
        try {
            execute(chatOptions);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        this.chatIds.add(chatId);
    }

    private void handleChatOptions(Update update) {
        String messageText = update.getMessage().getText();
        String chatId = String.valueOf(update.getMessage().getChatId());

        switch (messageText) {
            case "/option":
                showChatOptions(chatId);
                break;
            case "/fake":
                generateFakeNames(chatId);
                break;
            case "/math":
                startMathExercise(chatId);
                break;
            case "/exit":
                exitChat(chatId);
                break;
            default:
                handleMathAnswer(chatId, messageText);
                break;
        }
    }

    private void startMathExercise(String chatId) {
        userLevels.put(chatId, 1); // Start at level 1
        userPoints.put(chatId, 0); // Reset points
        sendMathExercise(chatId, 1);
    }

    private void sendMathExercise(String chatId, int level) {
        switch (level) {
            case 1:
                sendMathExerciseEz(chatId);
                break;
            case 2:
                sendMathExerciseHard(chatId);
                break;
            case 3:
                sendMathExerciseHardest(chatId);
                break;
        }
    }

    private void sendMathExerciseEz(String chatId) {
        Random random = new Random();
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int result = a + b;
        mathQuestions.put(chatId, result);
        SendMessage mathMessage = new SendMessage(chatId, "Solve: " + a + " + " + b);
        try {
            execute(mathMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMathExerciseHard(String chatId) {
        Random random = new Random();
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int c = random.nextInt(10) + 1;
        int result = a * b - c;
        mathQuestionsHard.put(chatId, result);
        SendMessage mathMessage = new SendMessage(chatId, "Solve: " + a + " * " + b + " - " + c);
        try {
            execute(mathMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMathExerciseHardest(String chatId) {
        Random random = new Random();
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int c = random.nextInt(10) + 1;
        int d = random.nextInt(10) + 1;
    int result = (a * b) + (c - d);
        mathQuestionsHardest.put(chatId, result);
    SendMessage mathMessage = new SendMessage(chatId, "Solve: (" + a + " * " + b + ") + (" + c + " - " + d + ")");
        try {
        execute(mathMessage);
    } catch (TelegramApiException e) {
        throw new RuntimeException(e);
    }
}

private void handleMathAnswer(String chatId, String answerText) {
    try {
        int userAnswer = Integer.parseInt(answerText);
        int level = userLevels.getOrDefault(chatId, 1);
        boolean correct = false;
        switch (level) {
            case 1:
                correct = userAnswer == mathQuestions.get(chatId);
                break;
            case 2:
                correct = userAnswer == mathQuestionsHard.get(chatId);
                break;
            case 3:
                correct = userAnswer == mathQuestionsHardest.get(chatId);
                break;
        }

        if (correct) {
            int newPoints = userPoints.getOrDefault(chatId, 0) + 1;
            userPoints.put(chatId, newPoints);
            SendMessage correctMessage = new SendMessage(chatId, "Correct! Your points --> " + newPoints);
            try {
                execute(correctMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            if (newPoints >= 10 && level < 3) {
                userLevels.put(chatId, level + 1);
                userPoints.put(chatId, 0);
                SendMessage levelUpMessage = new SendMessage(chatId, "Congratulations! You've advanced to level " + (level + 1));
                try {
                    execute(levelUpMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            sendMathExercise(chatId, userLevels.get(chatId));
        } else {
            int newPoints = userPoints.getOrDefault(chatId,0) - 1;
            userPoints.put(chatId,newPoints);
            SendMessage wrongMessage = new SendMessage(chatId, "Wrong answer, Try again. Your points --> " + newPoints);
            try {
                execute(wrongMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            sendMathExercise(chatId, level);
        }
    } catch (NumberFormatException e) {
        SendMessage errorMessage = new SendMessage(chatId, "Please enter a valid number.");
        try {
            execute(errorMessage);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }
}

    private void generateFakeNames(String chatId) {
        Faker faker = new Faker();
        StringBuilder names = new StringBuilder("Fake names:\n");
        for (int i = 0; i < 5; i++) {
            names.append(faker.name().fullName()).append("\n");
        }
        SendMessage nameMessage = new SendMessage(chatId, names.toString());
        try {
            execute(nameMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void exitChat(String chatId) {
        this.chatIds.remove(chatId);
        this.mathQuestions.remove(chatId);
        SendMessage exitMessage = new SendMessage(chatId, "Exited chat. Returning to menu.");
        try {
            execute(exitMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        sendStartMessage(chatId);
    }

    private void cancelAction(String chatId) {
        this.userStates.remove(chatId);
        SendMessage cancelMessage = new SendMessage(chatId, "Cancelled. Returning to menu.");
        try {
            execute(cancelMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        sendStartMessage(chatId);
    }

    @Override
    public String getBotUsername() {
        return USER_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

}
