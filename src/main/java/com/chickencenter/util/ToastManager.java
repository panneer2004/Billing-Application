package com.chickencenter.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Queue;

public class ToastManager {

    private static ToastManager instance;
    private VBox toastContainer;
    private final Queue<ToastMessage> queue = new LinkedList<>();
    private boolean isShowing = false;

    private static final String STYLE_SUCCESS = "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 25; -fx-effect: dropshadow(gaussian, #00000033, 8, 0, 0, 2);";
    private static final String STYLE_ERROR = "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 25; -fx-effect: dropshadow(gaussian, #00000033, 8, 0, 0, 2);";
    private static final String STYLE_WARNING = "-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 25; -fx-effect: dropshadow(gaussian, #00000033, 8, 0, 0, 2);";
    private static final String STYLE_INFO = "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 25; -fx-effect: dropshadow(gaussian, #00000033, 8, 0, 0, 2);";

    public enum Type { SUCCESS, ERROR, WARNING, INFO }

    private ToastManager() {}

    public static ToastManager getInstance() {
        if (instance == null) {
            instance = new ToastManager();
        }
        return instance;
    }

    public void init(StackPane globalRoot) {
        toastContainer = new VBox(8);
        toastContainer.setAlignment(Pos.TOP_CENTER);
        toastContainer.setPadding(new Insets(20, 0, 0, 0));
        toastContainer.setMouseTransparent(true);
        globalRoot.getChildren().add(toastContainer);
        StackPane.setAlignment(toastContainer, Pos.TOP_CENTER);
    }

    private static class ToastMessage {
        String message;
        Type type;
        ToastMessage(String message, Type type) { this.message = message; this.type = type; }
    }

    public static void showSuccess(String message) { getInstance().enqueue(message, Type.SUCCESS); }
    public static void showError(String message) { getInstance().enqueue(message, Type.ERROR); }
    public static void showWarning(String message) { getInstance().enqueue(message, Type.WARNING); }
    public static void showInfo(String message) { getInstance().enqueue(message, Type.INFO); }

    private void enqueue(String message, Type type) {
        queue.add(new ToastMessage(message, type));
        if (!isShowing) {
            processQueue();
        }
    }

    private void processQueue() {
        if (queue.isEmpty()) {
            isShowing = false;
            return;
        }
        isShowing = true;
        ToastMessage msg = queue.poll();
        displayToast(msg.message, msg.type);
    }

    private void displayToast(String message, Type type) {
        Label toastLabel = new Label(message);
        switch (type) {
            case SUCCESS: toastLabel.setStyle(STYLE_SUCCESS); break;
            case ERROR: toastLabel.setStyle(STYLE_ERROR); break;
            case WARNING: toastLabel.setStyle(STYLE_WARNING); break;
            case INFO: toastLabel.setStyle(STYLE_INFO); break;
        }
        toastLabel.setAlignment(Pos.CENTER);
        toastLabel.setOpacity(0);
        toastLabel.setTranslateY(-20);

        toastContainer.getChildren().add(toastLabel);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toastLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toastLabel);
        slideIn.setFromY(-20);
        slideIn.setToY(0);

        PauseTransition display = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toastLabel);
        slideOut.setFromY(0);
        slideOut.setToY(-20);
        slideOut.setOnFinished(e -> {
            toastContainer.getChildren().remove(toastLabel);
            processQueue();
        });

        SequentialTransition animation = new SequentialTransition(fadeIn, slideIn, display, fadeOut, slideOut);
        animation.play();
    }
}
