import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    TableView<ViewFiles> filesList;

    @FXML
    TableView<ViewFiles> serverFilesList;

    boolean isAuth;

    LoginController loginController = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: 24.07.2019 Перекинуть инициализацию на первый вызов 
        connectToServer(new ActionEvent());
        openAuthorization();
        initializeTable();
    }

    private void initializeTable() {
        TableColumn<ViewFiles, String> tcFileName = new TableColumn<>("Name");
        tcFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<ViewFiles, String> tcSize = new TableColumn<>("Size");
        tcSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));

        filesList.getColumns().addAll(tcFileName, tcSize);

        TableColumn<ViewFiles, String> tcServerFileName = new TableColumn<>("Name");
        tcServerFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<ViewFiles, String> tcServerSize = new TableColumn<>("Size");
        tcServerSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        serverFilesList.getColumns().addAll(tcServerFileName, tcServerSize);
    }

    public void openAuthorization(){

        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            loginController = (LoginController) loader.getController();
            loginController.backController = this;

            stage.setTitle("JavaFX Authorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Network.sendMsg(new CommandRequest(CommandType.Disconnect));
                    System.exit(0);
                }
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isAuth(){
        return isAuth;
    }

        public void connectToServer(ActionEvent actionEvent){

        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof CommandRequest){
                        CommandRequest cr = (CommandRequest) am;
                        if (cr.getCt() == CommandType.Authorization){
                            isAuth = true;
                            loginController.hideScene();
                            pressGetServerCatalog(new ActionEvent());
                        }
                        if (cr.getCt() == CommandType.GetFileLIST){
                            refreshServerFilesList(cr.getArguments());
                        }

                    }
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                    }

                   refreshLocalFilesList();

                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();

    }

    public void Authorization(String login, String password){
        Network.sendMsg(new CommandRequest(CommandType.Authorization, login, password));
    }

    public void pressPushFileToServer(ActionEvent actionEvent){

        String text = filesList.getSelectionModel().getSelectedItem().getFileName();
        if (Objects.isNull(text)) return;

        try {
            Path path = Paths.get("client_storage", "/", text);
            FileMessage fileMessage = new FileMessage(path);
            Network.sendMsg(fileMessage);
            pressGetServerCatalog(new ActionEvent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String text = serverFilesList.getSelectionModel().getSelectedItem().getFileName();
        if (Objects.isNull(text)) return;
        Network.sendMsg(new FileRequest(text));
    }

    public void pressGetServerCatalog(ActionEvent actionEvent) {
        Network.sendMsg(new CommandRequest(CommandType.GetFileLIST));
    }

    public void pressDelete(ActionEvent actionEvent) {
        ViewFiles viewFiles = serverFilesList.getSelectionModel().getSelectedItem();
        if (Objects.isNull(viewFiles)) return;
        Network.sendMsg(new CommandRequest(CommandType.DeleteFile, viewFiles.getFileName()));
    }

    public void getDirectory(ActionEvent actionEvent) {
        DirectoryChooser dir = new DirectoryChooser();
        File file = dir.showDialog(null);
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage")).forEach(o -> {
                    filesList.getItems().addAll(new ViewFiles(o.getFileName().toString(), getFileSizeKiloBytes(o.toFile().length())));
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getFileSizeKiloBytes(long length) {
        return length/1024 + " Kb";
    }

    private void refreshServerFilesList(String[] read) {

        updateUI(() -> {
            serverFilesList.getItems().clear();
            for (String str : read) {

                String[] strFile = str.split("/", 2);


                serverFilesList.getItems().addAll(new ViewFiles(strFile[0], "1"));
            }
        });
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
