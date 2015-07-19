package transporter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Gui extends Application {

    final static String SCENE_TITLE = "Transporter GUI";
    final static String TAB_TITLE_REDUCE = "Reduce";
    final static String TAB_TITLE_CONSOLIDATE = "Consolidate";
    final static int SCENE_WIDTH = 480;
    final static int SCENE_HEIGHT = 240;

    private Engine engine;

    private Label reduceFileLabel;
    private Label reduceFilePathLabel;
    private Button reduceBrowseButton;
    private FileChooser fileChooser;
    private Label reducePasswordLabel;
    private PasswordField reducePasswordField;
    private Button reduceButton;
    private Label reduceMessage;

    private Label consolidateDirectoryLabel;
    private Label consolidateDirectoryPathLabel;
    private Button consolidateBrowseButton;
    private DirectoryChooser directoryChooser;
    private Label consolidatePasswordLabel;
    private PasswordField consolidatePasswordField;
    private Button consolidateButton;
    private Label consolidateMessage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        engine = new Engine();

        primaryStage.setTitle(SCENE_TITLE);

        reduceFileLabel = new Label("Select File:");

        fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");

        reduceBrowseButton = new Button("Browse");
        reduceBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            reduceFilePathLabel.setText(file.getAbsolutePath());
                            reduceButton.setDisable(false);
                        }
                    });
                }
            }
        });

        reduceFilePathLabel = new Label("No file selected.");
        reduceFilePathLabel.setPrefWidth(360);
        reduceFilePathLabel.setWrapText(true);

        reducePasswordLabel = new Label("Password:");
        reducePasswordField = new PasswordField();

        reduceButton = new Button("Reduce");
        reduceButton.setDisable(true);
        reduceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reduceMessage.setText("");
                primaryStage.getScene().setCursor(Cursor.WAIT);
                reduceButton.setDisable(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String filepath = reduceFilePathLabel.getText();
                        String password = reducePasswordField.getText();

                        try {
                            final Path directoryPath = engine.reduce(filepath, password);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (Desktop.isDesktopSupported()) {
                                        try {
                                            Desktop.getDesktop().open(directoryPath.toFile());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    reduceMessage.setText(e.getMessage());
                                }
                            });
                            e.printStackTrace();
                        } finally {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    reduceButton.setDisable(false);
                                    primaryStage.getScene().setCursor(Cursor.DEFAULT);
                                }
                            });
                        }
                    }
                });
            }
        });

        reduceMessage = new Label("");
        reduceMessage.setTextFill(Color.web("red"));

        /************************************************************/

        consolidateDirectoryLabel = new Label("Select Directory:");

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");

        consolidateBrowseButton = new Button("Browse");
        consolidateBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final File directory = directoryChooser.showDialog(primaryStage);
                if (directory != null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            consolidateDirectoryPathLabel.setText(directory.getAbsolutePath());
                            consolidateButton.setDisable(false);
                        }
                    });
                }
            }
        });

        consolidateDirectoryPathLabel = new Label("No directory selected.");
        consolidateDirectoryPathLabel.setPrefWidth(360);
        consolidateDirectoryPathLabel.setWrapText(true);

        consolidatePasswordLabel = new Label("Password:");
        consolidatePasswordField = new PasswordField();

        consolidateButton = new Button("Consolidate");
        consolidateButton.setDisable(true);
        consolidateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                consolidateMessage.setText("");
                primaryStage.getScene().setCursor(Cursor.WAIT);
                consolidateButton.setDisable(true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String consolidateDirectoryPath = consolidateDirectoryPathLabel.getText();
                        String password = consolidatePasswordField.getText();
                        try {
                            final Path outputPath = engine.consolidate(consolidateDirectoryPath, password);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (Desktop.isDesktopSupported()) {
                                        try {
                                            Desktop.getDesktop().open(outputPath.getParent().toFile());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    consolidateMessage.setText(e.getMessage());
                                }
                            });
                            e.printStackTrace();
                        } finally {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    consolidateButton.setDisable(false);
                                    primaryStage.getScene().setCursor(Cursor.DEFAULT);
                                }
                            });
                        }
                    }
                });
            }
        });

        consolidateMessage = new Label("");
        consolidateMessage.setTextFill(Color.web("red"));

        addToSceneLayout(primaryStage);
    }

    private void addToSceneLayout(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        /* FIXME: extract the two tabs into a common class */
        /* reduce */
        GridPane reduceGrid = createGridPane();

        reduceGrid.add(reduceFileLabel, 0, 0);
        reduceGrid.add(reduceBrowseButton, 1, 0);
        reduceGrid.add(reduceFilePathLabel, 0, 1, 2, 1);
        reduceGrid.add(reducePasswordLabel, 0, 2);
        reduceGrid.add(reducePasswordField, 1, 2);
        reduceGrid.add(reduceButton, 0, 3);
        reduceGrid.add(reduceMessage, 1, 3);

        Tab reduceTab = new Tab();
        reduceTab.setText(TAB_TITLE_REDUCE);
        reduceTab.setContent(reduceGrid);
        tabPane.getTabs().add(reduceTab);

        /* consolidate */
        GridPane consolidateGrid = createGridPane();

        consolidateGrid.add(consolidateDirectoryLabel, 0, 0);
        consolidateGrid.add(consolidateBrowseButton, 1, 0);
        consolidateGrid.add(consolidateDirectoryPathLabel, 0, 1, 2, 1);
        consolidateGrid.add(consolidatePasswordLabel, 0, 2);
        consolidateGrid.add(consolidatePasswordField, 1, 2);
        consolidateGrid.add(consolidateButton, 0, 3);
        consolidateGrid.add(consolidateMessage, 1, 3);

        Tab consolidateTab = new Tab();
        consolidateTab.setText(TAB_TITLE_CONSOLIDATE);
        consolidateTab.setContent(consolidateGrid);
        tabPane.getTabs().add(consolidateTab);

        /* outer */
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tabPane);

        Scene scene = new Scene(borderPane, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane reduceGrid = new GridPane();
        reduceGrid.setGridLinesVisible(false);
        reduceGrid.setAlignment(Pos.CENTER);
        reduceGrid.setHgap(10);
        reduceGrid.setVgap(10);
        reduceGrid.setPadding(new Insets(5, 25, 5, 25));
        return reduceGrid;
    }

}
