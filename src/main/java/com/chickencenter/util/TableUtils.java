package com.chickencenter.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableUtils {

    public static <T> TableColumn<T, Integer> createSerialNumberColumn(String columnName, int fixedWidth) {
        TableColumn<T, Integer> col = new TableColumn<>(columnName);
        col.setPrefWidth(fixedWidth);
        col.setMinWidth(fixedWidth);
        col.setMaxWidth(fixedWidth);
        col.setStyle("-fx-alignment: CENTER; -fx-font-size: 12;");
        col.setCellFactory(colParam -> new TableCell<T, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        col.setSortable(false);
        col.setReorderable(false);
        return col;
    }

    public static <T> void addSerialNumberColumn(TableView<T> table, int position) {
        table.getColumns().add(position, createSerialNumberColumn("Sl No", 40));
    }
}
