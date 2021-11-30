import java.io.*;

final class ChatMessage {

    private int messageType; // 0 for a normal String message and 1 for file
    private String userMessage;

    //End Variables-------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public ChatMessage(String messageInput)
    {
        this.messageType = 0;
        this.userMessage = messageInput;
    }

    public ChatMessage(int typeInput, String messageInput)
    {
        this.messageType = typeInput;
        this.userMessage = messageInput;
    }


    //End Constructor-----------------------------------------------------------------------------------------------------------------------------------------------------------------

    public int getType()
    {
        return this.messageType;
    }

    public String getMessage()
    {
        return this.userMessage;
    }

    //End Getters---------------------------------------------------------------------------------------------------------------------------------------------------------------------
}
