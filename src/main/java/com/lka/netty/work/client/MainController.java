package com.lka.netty.work.client;

import com.lka.netty.work.common.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    public MenuBar menuBar;

    @FXML
    public VBox rootNode;

    @FXML
    TextField tfOutputFileName;

    @FXML
    TextField tfInputFileName;

    @FXML
    ListView<String> clientFilesList;

    @FXML
    ListView<String> serverFilesList;

    private boolean isAuthorized = false;

    private LoginController lc;

    private byte[] buffer = new byte[FileMessage.MSG_BYTE_BUFFER];

    private Alert alert = new Alert(Alert.AlertType.ERROR);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        rootNode.setVisible(false);
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        System.out.println("Recieve " + fm.getLen());
                        if (fm.getLen() == FileMessage.MSG_BYTE_BUFFER){
                            Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }else {
                            byte[] tmpBuf = new byte[fm.getLen()];
                            System.arraycopy(fm.getData(), 0, tmpBuf, 0, fm.getLen());
                            Files.write(Paths.get("client_storage/" + fm.getFilename()), tmpBuf, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                        refreshLocalFilesList();
                    } else if (am instanceof ListOfFiles) {
                        refreshServerFilesList(am);
                    } else if (am instanceof AuthAnswer) {
                        if (((AuthAnswer) am).isAuthOK()) {
                            System.out.println("Auth ok");
                            updateUI(() -> {
                                lc.login.getScene().getWindow().hide();
                            });
                            rootNode.setVisible(true);
                            requestToServerFiles();
                        } else System.out.println("Auth failed");
                    }
                }
            }catch (SocketException es){
                System.out.println("Socket closed");
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        initContextMenu();
        showLoginScene();
    }

    private void showLoginScene() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            lc = loader.getController();
            lc.backController = this;

            stage.setTitle("JavaFX Authorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(String user, String pass) {
        Network.sendMsg(new AuthorizationReq(user, pass));
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        downloadFromServer(tfInputFileName.getText());
    }

    private void downloadFromServer(String fileName){
        if (fileName.length() > 0 && !existLocal(fileName)) {
            Network.sendMsg(new FileRequest(fileName));
            tfInputFileName.clear();
        }
    }

    private boolean existLocal(String fileName) {
        if (clientFilesList.getItems().contains(fileName)){
            alert.setTitle("Ошибка");
            alert.setContentText("Файл с таким именем уже скачен!");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        uploadToServer(tfOutputFileName.getText());
    }

    private void uploadToServer(String filename){
        if (filename.length() > 0) {
            if (Files.exists(Paths.get("client_storage/" + filename)) && !existOnServer(filename)) {
                try(BufferedInputStream in = new BufferedInputStream(new FileInputStream("client_storage/" + filename))) {
                    int len = 0;
                    while ((len = in.read(buffer)) != -1){
                        Network.sendMsg(new FileMessage(filename, buffer, len));
                    }
                    System.out.println("File have send");
                    tfOutputFileName.clear();
                    requestToServerFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            alert.setTitle("Ошибка");
            alert.setContentText("Файл не найден!");
            alert.showAndWait();
        }
    }

    private boolean existOnServer(String fileName) {
        requestToServerFiles();
        if (serverFilesList.getItems().contains(fileName)) {
            alert.setTitle("Ошибка");
            alert.setContentText("Файл с таким именем уже передан!");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFilesList(AbstractMessage am) {
        updateUI(() -> {
            serverFilesList.getItems().clear();
            serverFilesList.getItems().addAll(((ListOfFiles) am).getFiles());
        });
    }

    public void requestToServerFiles() {
        Network.sendMsg(new ListOfFilesRequest());
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void updateListsOfFiles(ActionEvent actionEvent) {
        refreshLocalFilesList();
        requestToServerFiles();
    }

    public void exitApp(ActionEvent actionEvent) {
        Network.stop();
        Platform.exit();
        System.exit(0);
    }

    public void initContextMenu() {
        clientFilesList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                if (Files.exists(Paths.get("client_storage/" + cell.getItem()))) {
                    try {
                        Files.delete(Paths.get("client_storage/" + cell.getItem()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                refreshLocalFilesList();
            });

            MenuItem copyToTF = new MenuItem();
            copyToTF.textProperty().bind(Bindings.format("Copy file \"%s\" to cloud", cell.itemProperty()));
            copyToTF.setOnAction(event -> uploadToServer(cell.getItem()));
            contextMenu.getItems().addAll(copyToTF, deleteItem);

            cell.textProperty().bind(cell.itemProperty());

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });

        serverFilesList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                Network.sendMsg(new DeleteFileReq("server_storage/" + cell.getItem()));
                requestToServerFiles();
            } );

            MenuItem copyToTF = new MenuItem();
            copyToTF.textProperty().bind(Bindings.format("Copy file \"%s\" to client", cell.itemProperty()));
            copyToTF.setOnAction(event -> downloadFromServer(cell.getItem()));
            contextMenu.getItems().addAll(copyToTF, deleteItem);

            cell.textProperty().bind(cell.itemProperty());

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
    }
}
