import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServersMessage {

    public FileMessage fileRequest(FileRequest fileRequest) {

        FileMessage fileMessage = null;
        if (Files.exists(Paths.get("server_storage/" + fileRequest.getFilename()))) {

            try {
                fileMessage = new FileMessage(Paths.get("server_storage/" + fileRequest.getFilename()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return fileMessage;
    }

    public String fileMessage(FileMessage fileMessage){

        String dir = "server_storage/" + fileMessage.getFilename();
        
        try (FileOutputStream fos = new FileOutputStream(dir)){            
            fos.write(fileMessage.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileMessage.getFilename();
    }
    
    



}