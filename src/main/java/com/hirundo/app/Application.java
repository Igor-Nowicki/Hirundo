package com.hirundo.app;

import com.hirundo.app.models.MainModel;
import com.hirundo.app.services.DialogFileChooser;
import com.hirundo.app.view_models.MainViewModel;
import com.hirundo.app.views.MainView;
import com.hirundo.libs.services.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {

    final double width = 600.0;
    final double height = 400.0;

    public Application() {
    }

    public static void main(final String[] args) {
        javafx.application.Application.launch();
    }

    @Override
    public void start(final Stage stage) throws IOException {

//        final var oldDbBirdRecordDataLoader = new AccessOldDbBirdRecordDataLoader();
//        final var newDbBirdRecordDataLoader = new AccessNewDbBirdRecordDataLoader();

//        final var oldAdapter = new BirdDataLoaderAdapter(oldDbBirdRecordDataLoader);
//        final var newAdapter = new BirdDataLoaderAdapter(newDbBirdRecordDataLoader);

//        final var dataLoader = new FileDataLoader(oldAdapter, newAdapter);
        final var fileChooser = new DialogFileChooser();

        var builder = new BirdRecordDataLoaderBuilder();

        final var model = new MainModel(builder, fileChooser);
        final var viewModel = new MainViewModel(model);
        final var view = new MainView();
        view.setViewModel(viewModel);

        stage.setMinHeight(width);
        stage.setMinWidth(height);
        stage.setResizable(false);

        final Scene scene = new Scene(view.getParent());

        stage.setTitle("Hirundo - powroty ptaków");
        stage.setScene(scene);
        stage.show();
    }
}