package notepadfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class NotePadFX extends Application {
    private Scene scene;
    private BorderPane pane;
    private MenuBar menu;
    private Menu file;
    private Menu edit;
    private Menu help;
    private Menu compile;
    private TextArea ta;
    private String filetext;
    private MenuItem newFile;
    private MenuItem openFile;
    private MenuItem saveFile;
    private MenuItem exitApp;
    private FileChooser fileChooser;
    private int openedFileFlag, dontExitFlag;
    private File openedFile; //opened file
    private int discardFlag;
    private int successSaveFlag;
    private static Stage pStage;


    @Override
    public void init(){
        dontExitFlag=openedFileFlag=discardFlag=successSaveFlag=0;
        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter javaFilter = new FileChooser.ExtensionFilter("Java files (*.java)", "*.java");
        fileChooser.getExtensionFilters().add(txtFilter);
        fileChooser.getExtensionFilters().add(javaFilter);
        pane = new BorderPane();
        scene = new Scene(pane, 800, 600);
        ta = new TextArea();
        filetext = "";
        menu = new MenuBar();
        file = new Menu("File");
        edit = new Menu("Edit");
        help = new Menu("Help");
        compile = new Menu("Compile");
        newFile = new MenuItem("New");
        newFile.setAccelerator(KeyCombination.keyCombination("ctrl+n"));
        openFile = new MenuItem("Open");
        openFile.setAccelerator(KeyCombination.keyCombination("ctrl+o"));
        saveFile = new MenuItem("Save");
        saveFile.setAccelerator(KeyCombination.keyCombination("ctrl+s"));
        exitApp = new MenuItem("Exit");
        exitApp.setAccelerator(KeyCombination.keyCombination("esc"));
        
        //newFile Handler
        newFile.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(ta.getText() != null){
                    if(!ta.getText().equals(filetext)){
                        checkSaving();
                    }
                }
            //for cancel button action
                if(dontExitFlag==1){
                    dontExitFlag=0;
                    return;
                }
                pStage.setTitle("Untitled - NotePad FX");
                openedFile = null;
                openedFileFlag=0;
                filetext="";
                ta.setText("");
            }
        });
        //openFile Handler
        openFile.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(!ta.getText().equals("")){
                    if(!ta.getText().equals(filetext)){
                        checkSaving();
                    }
                }
                if(dontExitFlag==1){
                    dontExitFlag=0;
                    return;
                }
                openedFile = fileChooser.showOpenDialog(pStage);
                if (openedFile != null) {
                    try {
                        openFile(openedFile);
                        pStage.setTitle(openedFile.getName()+" - NotePad FX");
                        openedFileFlag=1;
                    } catch (IOException ex) {
                        Logger.getLogger(NotePadFX.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        });
        //saveFile Handler
        saveFile.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(openedFileFlag==0){
                    File file = fileChooser.showSaveDialog(pStage);
                    if (file != null) {
                        try {
                            saveTextToFile(ta.getText(), file);
                            openedFile=file;
                            openedFileFlag=1;
                            successSaveFlag=1;
                            filetext=ta.getText();
                            pStage.setTitle(openedFile.getName()+" - NotePad FX");
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(NotePadFX.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                else if (openedFileFlag==1 && !ta.getText().equals(filetext)){
                    try {
                        saveTextToFile(ta.getText(), openedFile);
                        filetext=ta.getText();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(NotePadFX.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        //close Handler
        exitApp.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(ta.getText() != null ){
                    if(!ta.getText().equals(filetext)){
                        checkSaving();
                    }
                }
                if(dontExitFlag==1){
                    dontExitFlag=0;
                    return;
                }
                pStage.close();
            }
        });
        //undo
        MenuItem undo = new MenuItem("Undo");
        undo.setAccelerator(KeyCombination.keyCombination("ctrl+z"));
        undo.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                ta.undo();
            }
        });
        //cut
        MenuItem cut_t = new MenuItem("Cut");
        cut_t.setAccelerator(KeyCombination.keyCombination("ctrl+x"));
        cut_t.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                ta.cut();
            }
        });
        //copy
        MenuItem copy_t = new MenuItem("Copy");
        copy_t.setAccelerator(KeyCombination.keyCombination("ctrl+c"));
        copy_t.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                ta.copy();
            }
        });
        //paste
        MenuItem paste_t = new MenuItem("Paste");
        paste_t.setAccelerator(KeyCombination.keyCombination("ctrl+v"));
        paste_t.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                ta.paste();
            }
        });
        //delete
        MenuItem delete_t = new MenuItem("Delete");
        delete_t.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                ta.deleteText(ta.getSelection());
            }
        });
        //select all
        MenuItem selectAll = new MenuItem("Select All");
        selectAll.setAccelerator(KeyCombination.keyCombination("ctrl+a"));
        selectAll.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                ta.selectAll();
            }
        });
        //about
        MenuItem about = new MenuItem("About NotePad");
        about.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Help?");
                alert.setHeaderText(null);
                alert.setContentText("This App has been created By Mohamed Elshafeay\n"
                        + "For any further information please contact me:\n"
                        + "mohamedelshafeay@gmail.com");
                alert.showAndWait();
            }
        });
        //build
        MenuItem build = new MenuItem("Build");
        build.setAccelerator(KeyCombination.keyCombination("ctrl+f4"));
        build.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                saveFile.fire();
                int results = compiler.run(System.in, System.out, System.err, openedFile.getAbsolutePath());
                System.out.println("Success: " + (results == 0));
            }
        });
        //Run
        MenuItem run = new MenuItem("Run");
        run.setAccelerator(KeyCombination.keyCombination("ctrl+f6"));
        run.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String [] arr = filetext.split("class "); //split by the word class
                String [] className = arr[1].split("[{]"); //split by { to get the class name
                try {
                    Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"java -cp "+openedFile.getParent()+"\\ "+
                            className[0]+"\" ");
                } catch (IOException ex) {
                    Logger.getLogger(NotePadFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //Build & Run
        MenuItem buildRun = new MenuItem("Build & Run");
        buildRun.setAccelerator(KeyCombination.keyCombination("ctrl+f5"));
        buildRun.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                saveFile.fire();
                int results = compiler.run(System.in, System.out, System.err, openedFile.getAbsolutePath());
                System.out.println("Success: " + (results == 0));
                String [] arr = filetext.split("class "); //split by the word class
                String [] className = arr[1].split("[{]"); //split by { to get the class name
                try {
                    Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"java -cp "+openedFile.getParent()+"\\ "+
                        className[0]+"\" ");
                } catch (IOException ex) {
                    Logger.getLogger(NotePadFX.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
        });
        
        file.getItems().addAll(newFile, openFile, saveFile, new SeparatorMenuItem(), exitApp);
        edit.getItems().addAll(undo, new SeparatorMenuItem(), cut_t, copy_t, paste_t, delete_t, new SeparatorMenuItem(), selectAll);
        help.getItems().addAll(about);
        compile.getItems().addAll(build, run, buildRun);
        menu.getMenus().addAll(file, edit, help, compile);
        pane.setTop(menu);
        pane.setCenter(ta);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Untitled - NotePad FX");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e ->{
            if(!ta.getText().equals(filetext)){
                checkSaving();
                if(discardFlag==1 || successSaveFlag==1){
                }
                else{
                    dontExitFlag=discardFlag=successSaveFlag=0;
                    e.consume();
                }
            }
        });
        pStage=primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private void openFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        ta.setText("");
        filetext="";
        String st;
        while ((st = br.readLine()) != null)
            filetext += st;
        ta.setText(filetext);
    }
    private void saveTextToFile(String content, File file) throws FileNotFoundException {
        PrintWriter writer;
        writer = new PrintWriter(file);
        writer.println(content);
        writer.close();
    }
    private void checkSaving(){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("NotePadFX");
        String fileTitle;
        if(openedFileFlag == 1){
            fileTitle = openedFile.getName();
        }
        else{
            fileTitle = "Untitled";
        }
        alert.setHeaderText(null);
        alert.setContentText("Do you want to save your changes to "+ fileTitle + "?");
        ButtonType save_btn = new ButtonType("Save");
        ButtonType discard_btn = new ButtonType("Discard");
        ButtonType cancel_btn = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(save_btn, discard_btn, cancel_btn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == save_btn){
            saveFile.fire();
            openedFile=null;
            openedFileFlag=0;
        } else if (result.get() == discard_btn) {
            discardFlag=1;
        }
        else {
            dontExitFlag=1;
        }
    }
}
