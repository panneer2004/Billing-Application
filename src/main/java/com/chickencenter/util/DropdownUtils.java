package com.chickencenter.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

public class DropdownUtils {

    private static final int DEFAULT_VISIBLE_ROWS = 4;
    private static final int ROW_HEIGHT = 36;

    public static <T> void makeScrollable(ComboBox<T> comboBox) {
        makeScrollable(comboBox, DEFAULT_VISIBLE_ROWS);
    }

    public static <T> void makeScrollable(ComboBox<T> comboBox, int visibleRows) {
        if (comboBox == null) return;

        comboBox.setVisibleRowCount(Math.max(visibleRows, 1));

        comboBox.setCellFactory(listView -> {
            ListCell<T> cell = new ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
            cell.setPrefHeight(ROW_HEIGHT);
            return cell;
        });
    }
}
