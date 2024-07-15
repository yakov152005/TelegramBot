package utils;

public class Constants {

    public static class Text{
        public static final String TEXT_1 =  "Welcome! Choose an option:\n" +
                                             "/chat - Chat with me\n" +
                                             "/clear - Clear chat history\n" +
                                             "/balance - Check message balance\n" +
                                             "/exit.";
        public static final String TEXT_2 = "You are now chatting with GPT-4. Type 'exit' to end the chat.";
        public static final String TEXT_3 = """
                What do you want to do?
                1 - Send message to Chat GPT
                2 - Clear history
                3 - Check balance
                0 - Exit.
                """;
        public static final String TEXT_4 = "The amount of message do u have is: ";
        public static final String TEXT_5 = "Clear chat...";
        public static final String C_1 = "/start";
        public static final String C_2 = "/chat";
        public static final String C_3 = "/clear";
        public static final String C_4 ="/balance";
        public static final String C_5 = "/exit";


    }

    public static class Details{
        public static final String TOKEN = "6537799423:AAHgEf9HyP-flUoxJ3678gJcZI3OBXf0yuM";
        public static final String USER_NAME = "MyBotYakovbot";
        public static final String ID = "039575329";
        public static final String ID_2 = "206263667";
    }

    public static class Error{
        public static final int ERROR_0 = 3000;
        public static final int ERROR_1 = 3001;
        public static final int ERROR_2 = 3002;
        public static final int ERROR_3 = 3003;
        public static final int ERROR_4 = 3005;
    }

    public static class ErrorText{
        public static final String E_0 = "ID card was not sent";
        public static final String E_1 = "ID card does not exist in the database";
        public static final String E_2 = "The quota of applications for this identity card has expired";
        public static final String E_3 = "No message text was sent";
        public static final String E_4 = "general error";
        public static final String E_5 = "Unknown error code: ";
    }

    public static class Path{
        public static final String DOM = "https://app.seker.live/fm1/";
        public static final String PATH_M = "send-message";
        public static final String PATH_H = "clear-history";
        public static final String PATH_B = "check-balance";
    }


}
