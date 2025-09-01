package finalexam;

//Yasmim Isabela Batista Silva

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

public class SerialKillerFinal extends Application {

    private TextArea skList;
    private TextField yearField;
    private TextField nameField;
    private ImageView imageView;
    private List<String> foundKillers = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        //labels
        Label welcomeLabel = new Label("Welcome to the Serial Killers Searching!");
        Label welcomeLabel2 = new Label("You tell me what year you want to search to get a list and then enter a serial killer");
        Label welcomeLabel3 = new Label("name to search for their picture!");
        Label funFact = new Label ("Curiosity: Did you know that the United States has the most number of documented serial killers");
        Label funFact2 = new Label ("in the world? Over 70% of known cases come from the U.S. WATCH OUT!");

        //set font styles
        welcomeLabel.setFont(Font.font("American Typewriter", FontWeight.BOLD, 18));
        welcomeLabel.setWrapText(true);
        welcomeLabel2.setFont(Font.font("American Typewriter", FontWeight.LIGHT, 15));
        welcomeLabel2.setWrapText(true);
        welcomeLabel3.setFont(Font.font("American Typewriter", FontWeight.LIGHT, 15));
        welcomeLabel3.setWrapText(true);
        funFact.setFont(Font.font("American Typewriter", FontWeight.LIGHT, 12));
        funFact.setWrapText(true);
        funFact2.setFont(Font.font("American Typewriter", FontWeight.LIGHT, 12));
        funFact2.setWrapText(true);

        //set background color behind labels for better reading
        welcomeLabel.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), null, null)));
        welcomeLabel2.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), null, null)));
        welcomeLabel3.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), null, null)));
        funFact.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), null, null)));
        funFact2.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), null, null)));

        //set text color
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel2.setTextFill(Color.WHITE);
        welcomeLabel3.setTextFill(Color.WHITE);
        funFact.setTextFill(Color.WHITE);
        funFact2.setTextFill(Color.WHITE);

        //year input
        yearField = new TextField();
        yearField.setPromptText("Enter year or range (e.g., 1980 or 1980-1990)");
        
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("^[0-9-]*$")){//make sure the user input is only numbers and -
                return change;
            }
            return null;
        });
        yearField.setTextFormatter(textFormatter);
        
        Button searchButton = new Button("Search Killers");
        searchButton.setOnAction(e -> {
            try{
                searchKillersByYear(yearField.getText().trim());
            } catch (IOException ex){
                skList.setText("Error searching. Please try again");
            }
        });
        //accepts pressing enter to search
        yearField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                searchKillersByYear(yearField.getText().trim());
                } catch (IOException ex){
                skList.setText("Error searching. Please try again");
                }
            }
        });
        //textArea where the serial killers and warnings will show
        skList = new TextArea();
        skList.setEditable(false);
        skList.setPrefHeight(350);

        //serial killer name input
        nameField = new TextField();
        nameField.setPromptText("Enter serial killer name");

        Button imageButton = new Button("Show Image");
        imageButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                showWikipediaImage(name);
            }
        });
        //accepts pressing enter to search
        nameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                showWikipediaImage(name);
            }
            }
        });
        
        //image settings
        imageView = new ImageView();
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        //center the image
        StackPane imagePane = new StackPane();
        imagePane.getChildren().add(imageView);
        StackPane.setAlignment(imageView, Pos.CENTER);

        //background image
        String imagePath = "background.jpg";
        Image backgroundImage = new Image("file:" + imagePath);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT
        );
        HBox centeredLabelBox = new HBox(welcomeLabel);
        centeredLabelBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, centeredLabelBox, welcomeLabel2, welcomeLabel3, yearField, searchButton, skList, nameField, imageButton, imagePane, funFact, funFact2);
        layout.setPadding(new Insets(15));
        layout.setBackground(new Background(background));

        //style the text labels
        welcomeLabel.setTextFill(Color.web("#f0f0f0"));
        welcomeLabel2.setTextFill(Color.web("#f0f0f0"));
        welcomeLabel3.setTextFill(Color.web("#f0f0f0"));
        
        //scene settings
        Scene scene = new Scene(layout, 600, 800);
        primaryStage.setTitle("Serial Killer Searching");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void searchKillersByYear(String yearInput) throws IOException {
        skList.clear();
        foundKillers.clear();
        try {
            Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_serial_killers_in_the_United_States").get();
            Elements tables = doc.select("table.wikitable");

            boolean found = false;

            for (Element table : tables) {
                Elements rows = table.select("tr");

                for (Element row : rows) {
                    Elements cells = row.select("td");

                    if (cells.size() >= 6) {
                        String name = cells.get(0).text();
                        String yearsActive = cells.get(1).text();

                        if (isYearInRange(yearInput, yearsActive)) {
                            String status = cells.get(4).text();
                            String notes = cells.get(5).text();

                            if (status.length() == 2) {
                                status = "Unknown";
                            }

                            skList.appendText("Name: " + name + "\nYears Active: " + yearsActive + "\nStatus: " + status + "\nNotes: " + notes + "\n\n");
                            foundKillers.add(name);
                            found = true;
                        }
                    }
                }
            }

            if (!found) {
                skList.setText("No serial killers found for the year or range " + yearInput + ".");
            }
        } catch (IOException e) {
            showErrorDialog("Error searching. Please try again.");
        }
    }

    private void showWikipediaImage(String name) {
    //convert the name to title case for better URL formatting/searching
    String formattedName = toTitleCase(name);
    String imageUrl = "https://en.wikipedia.org/wiki/" + formattedName.replace(" ", "_");
    try {
        Document doc = Jsoup.connect(imageUrl).get();
        Element infobox = doc.selectFirst(".infobox img");
        if (infobox != null) {
            String src = infobox.absUrl("src");
            imageView.setImage(new Image(src));
            playSound();  //plays the sound when the image shows
        } else {
            skList.setText("Image not found for " + formattedName);
            imageView.setImage(null);
        }
    } catch (IOException e) {
        showErrorDialog("Failed to load image for " + formattedName + ". Please try again.");
    }
}

    //method to convert a string to title case
    private String toTitleCase(String input) {
        String[] words = input.split(" ");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                titleCase.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        return titleCase.toString().trim();
    }

    private void playSound() {
        Media sound = new Media(Paths.get("The Murder.mp3").toUri().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        if (mediaPlayer != null) { //stop overlaying the sound
            mediaPlayer.stop();
        }
        mediaPlayer.setStartTime(Duration.seconds(0));//start at 0 seconds
        mediaPlayer.setStopTime(Duration.seconds(10));//stops at 10 seconds
        mediaPlayer.play();
    }

    private boolean isYearInRange(String input, String yearsActive) {
        if (yearsActive.contains(input)){
            return true;
        }
        
        if (yearsActive.contains("-")) {
            String[] range = yearsActive.split("[-–—]");

            try {
                int startYear = Integer.parseInt(range[0].trim().replaceAll("[^0-9]", ""));
                int endYear = Integer.parseInt(range[1].trim().replaceAll("[^0-9]", ""));
                int inputYear = Integer.parseInt(input);

                return inputYear >= startYear && inputYear <= endYear;
            } catch (Exception ignored) {}
        }
        return false;
    }
    
    private void showErrorDialog(String message){ //shows pop up errors
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}