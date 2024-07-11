package program;

public class Todo {
    private String text;
    private int seconds;

    public String getText(){
        return this.text;
    }
    public void setText(String text){
        this.text = text;
    }
    public int getSeconds(){
        return this.seconds;
    }
    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

}
