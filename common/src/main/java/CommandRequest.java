public class CommandRequest extends AbstractMessage{

    private CommandType ct;
    private String[] arguments;

    public CommandType getCt() {
        return ct;
    }


    public CommandRequest(CommandType ct, String ... arguments){
        this.ct = ct;
        this.arguments = arguments.clone();
    }

    public String[] getArguments() {
        return arguments;
    }
}

enum CommandType {

    GetFileLIST, DeleteFile, Authorization, Disconnect;

}

