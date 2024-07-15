package program;

import api.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static utils.Constants.ErrorText.*;
import static utils.Constants.Error.*;
import static utils.Constants.Text.*;
import static utils.Constants.Details.*;
import static utils.Constants.Path.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MyTelegramBot extends TelegramLongPollingBot {

    private static final Scanner s = new Scanner(System.in);
    private static final CloseableHttpClient client = HttpClients.createDefault();

    private final Map<String, String> userStates;

    public MyTelegramBot() {
        this.userStates = new HashMap<>();

    }

    public void sendMessage(String userId, String text) throws IOException, URISyntaxException, TelegramApiException {
        URI uriBuilder = getUriBuilder(PATH_M)
                .setParameter("id", ID)
                .setParameter("text", text)
                .build();

        String request = getAndResponse(uriBuilder);
        if (!request.isEmpty()) {
            Response responseObj = getResponseObj(request);
            if (responseObj.isSuccess()) {
                execute(new SendMessage(userId, responseObj.getExtra()));
                System.out.println(responseObj.getExtra());

            } else {
                errorCode(responseObj);
            }
        } else {
            execute(new SendMessage(userId, "The message was not sent."));
        }
    }


    public void clearHistory(String userId) throws IOException, URISyntaxException, TelegramApiException {
        URI uriBuilder = getUriBuilder(PATH_H)
                .setParameter("id", ID).build();

        String myResponse = getAndResponse(uriBuilder);
        if (!myResponse.isEmpty()){
            Response responseObj = getResponseObj(myResponse);
            if (responseObj.isSuccess()) {
                execute(new SendMessage(userId,TEXT_5));
                System.out.println(TEXT_5);
            } else {
                errorCode(responseObj);
            }
        }

    }

    public void checkBalance(String userId) throws IOException, URISyntaxException, TelegramApiException {
        URI uriBuilder = getUriBuilder(PATH_B)
                .setParameter("id", ID)
                .build();

        String myResponse = getAndResponse(uriBuilder);
        if (!myResponse.isEmpty()) {
            Response responseObj = getResponseObj(myResponse);
            if (responseObj.isSuccess()) {
                execute(new SendMessage(userId, TEXT_4 + responseObj.getExtra()));
                System.out.println(TEXT_4 + responseObj.getExtra());

            }else {
                errorCode(responseObj);
            }
        }
    }

    private void cancelAction(String chatId) {
        userStates.remove(chatId);
        SendMessage cancelMessage = new SendMessage(chatId, "Action cancelled.");
        try {
            execute(cancelMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        sendStartMessage(chatId);
    }

    public static URIBuilder getUriBuilder(String httpsName) throws URISyntaxException {
        return new URIBuilder(DOM + httpsName);
    }

    public static String getAndResponse(URI uriBuilder) throws IOException {
        HttpGet get = new HttpGet(uriBuilder);
        CloseableHttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    public static Response getResponseObj(String myResponse) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(myResponse, Response.class);
    }

    public static void errorCode(Response responseObj) {
        Integer errorCode = responseObj.getErrorCode();
        if (errorCode != null) {
            switch (errorCode) {
                case ERROR_0:
                    System.out.println(E_0);
                    break;
                case ERROR_1:
                    System.out.println(E_1);
                    break;
                case ERROR_2:
                    System.out.println(E_2);
                    break;
                case ERROR_3:
                    System.out.println(E_3);
                    break;
                case ERROR_4:
                    System.out.println(E_4);
                    break;
                default:
                    System.out.println(E_5 + errorCode);
            }
        }
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
                case C_1:
                    startChat2(chatId);
                    break;
                case C_2:
                    userStates.put(chatId, "chat");
                    startChat(chatId);
                    break;
                case C_3:
                    clearHistoryTry(chatId);
                    break;
                case C_4:
                    checkBalanceTry(chatId);
                    break;
                case C_5:
                    cancelAction(chatId);
                    break;
                default:
                    handleUserInput(chatId, messageText);
                    break;
            }
        }
    }

    private void clearHistoryTry(String chatId){
        try {
            clearHistory(chatId);
        } catch (IOException | URISyntaxException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkBalanceTry(String userId){
        try {
            checkBalance(userId);
        } catch (IOException | URISyntaxException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendStartMessage(String chatId) {
        SendMessage startMessage = new SendMessage(chatId,TEXT_1);
        try {
            execute(startMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void startChat(String chatId) {
        SendMessage chatMessage = new SendMessage(chatId, TEXT_2);
        try {
            execute(chatMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void startChat2(String chatId){
        sendStartMessage(chatId);
        userStates.put(chatId,"chat");
    }

    private void handleUserInput(String chatId, String messageText) {
        String currentState = userStates.get(chatId);
        System.out.println(currentState);
        if ("chat".equals(currentState)) {
            try {
                sendMessage(chatId, messageText);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void mainMenu() {
        System.out.println(TEXT_3);
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
