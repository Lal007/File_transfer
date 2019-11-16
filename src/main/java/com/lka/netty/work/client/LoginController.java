package com.lka.netty.work.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    public VBox login;
    @FXML
    public TextField loginTf;
    @FXML
    public PasswordField passTf;

    public MainController backController;

    public LoginController() {
    }

    public void pressOnLoginBtn(ActionEvent actionEvent) {
        backController.tryToAuth(loginTf.getText(), passTf.getText());

        loginTf.clear();
        passTf.clear();
    }
}
