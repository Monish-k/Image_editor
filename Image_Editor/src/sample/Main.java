package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javafx.scene.layout.TilePane;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    static Accordion accordion;
    static ResultSet resultSet1;
    static String query_select = "select ",query_name = "*",query_from = " from user_edited",query_where="";
    static String query;
    LocalDateTime now ;
    static PreparedStatement preparedStatement1;
    static String get_names_db = "select distinct * from user_edited";
    Scene scene_login,scene_choose,scene_editor,scene_previous;
    int x1=180,y1=270;
    static ImageView imageView=new ImageView();
    static String username,to_date,to_date1,to_path="/Users/monish/Desktop/snapshot",to_path1="/Users/monish/Desktop/savedImage";
    static File file1;
    static File file ;
    static ImageView imageViewcheck[];
    static TitledPane titledPane[];
    static int i=0,j;
    static ListView<ImageView> list =null;
    static ListView<String> listView_names=new ListView<String>();

    //ImageView imageView=new ImageView();
    // global mysql
    static Connection connection;
    static PreparedStatement preparedStatement;
    static String mysqlurl="jdbc:mysql://localhost/image_editor";
    private static void register() throws SQLException {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        connection=DriverManager.getConnection(mysqlurl,"root","monimkd072000");
    }
    private static void prepared(InputStream incoming) throws SQLException {
        preparedStatement =connection.prepareStatement("insert into user_edited values(?,?)");
        preparedStatement.setString(1,username);
        preparedStatement.setBlob(2, incoming);
        preparedStatement.execute();
    }

    private static final int ADJUST_TYPE_HUE = 1;
    private static final int ADJUST_TYPE_CONTRAST = 2;
    private static final int ADJUST_TYPE_SATURATION = 3;
    private static final int ADJUST_TYPE_BRIGHTNESS = 4;

    static ColorAdjust colorAdjust=new ColorAdjust();

    private Slider createSlider(final int adjustType) {
        Slider slider = new Slider();
        slider.setMin(-1);
        slider.setMax(1);
        slider.setBlockIncrement(0.1);
        slider.setValue(0);

        slider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public  void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                switch (adjustType) {
                    case ADJUST_TYPE_HUE:
                        colorAdjust.setHue(newValue.doubleValue());
                        break;
                    case ADJUST_TYPE_CONTRAST:
                        colorAdjust.setContrast(newValue.doubleValue());
                        break;
                    case ADJUST_TYPE_SATURATION:
                        colorAdjust.setSaturation(newValue.doubleValue());
                        break;
                    case ADJUST_TYPE_BRIGHTNESS:
                        colorAdjust.setBrightness(newValue.doubleValue());
                        break;
                }
            }
        });
        return slider;
    }

    private static void get_names() throws SQLException {
        PreparedStatement preparedStatement2 = connection.prepareStatement(get_names_db);
        ResultSet resultSet_2 = preparedStatement2.executeQuery();
        while(resultSet_2.next())
        {
            String sss=resultSet_2.getString(1);
            listView_names.getItems().add(sss);

        }
        resultSet_2.close();

    }

    private static void get_images()
    {
        query = query_select.concat(query_name.concat(query_from));
    }
    private static void get_num_item() throws SQLException{

        get_images();
        preparedStatement1=connection.prepareStatement(query);
        preparedStatement1.execute();
        resultSet1=preparedStatement1.executeQuery();
        while(resultSet1.next()){i++;}
        imageViewcheck= new ImageView[i];
        titledPane = new TitledPane[i];
        j = i;
        i=0;
        resultSet1.close();

    }

    private static void put_items() throws SQLException {
        Image image=null;
        InputStream io=null;
        ResultSet resultSet=preparedStatement1.executeQuery();
        while(i<j){
            while (resultSet.next()){
                io=resultSet.getBinaryStream(2);
                image = new Image(io);
                imageViewcheck[i]=new ImageView(image);
                titledPane[i]=new TitledPane(resultSet.getString(1),imageViewcheck[i]);
                titledPane[i].setMinWidth(600);
                i++;
            }
        }
        accordion.getPanes().addAll(titledPane);
        resultSet.close();
    }


    private File takeSnapShot(Node node){

        now=LocalDateTime.now();
        to_date=to_path.concat(String.valueOf(now));
        to_date=to_date.concat(".jpg");
        file = new File(to_date);
        WritableImage writableImage = node.snapshot(new SnapshotParameters(), null);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            System.out.println("snapshot saved: " + file.getAbsolutePath());
            InputStream inputStream=new FileInputStream(file.getAbsoluteFile());
            register();
            prepared(inputStream);
            return file;
        } catch (IOException | SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    //Save Image of ImageView
    private File saveImage(ImageView iv) throws IOException {
        now=LocalDateTime.now();
        to_date1=to_path1.concat(String.valueOf(now));
        to_date1=to_date1.concat(".jpg");
        file1 = new File(to_date1);
        Image img = iv.getImage();
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(img, null);
        try {
            ImageIO.write(renderedImage, "jpg", file1);
            System.out.println("Image saved: " + file1.getAbsolutePath());
            return file1;
        } catch (IOException ex ) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //Retrieve saved image
    private void retrieveImage(File file, ImageView imageView, Label label){
        if(file1 != null){
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);

            label.setText(file.getName() + "\n"
                    + image.getWidth() + " x " + image.getHeight());
        }else{
            label.setText("");
            imageView.setImage(null);

        }
    }


    @Override
    public void start(Stage primaryStage) throws Exception{

        BackgroundFill b=new BackgroundFill(Color.BROWN,new CornerRadii(1),null);
        primaryStage.show();


        //Blend effect
        Blend blend = new Blend();
        blend.setMode(BlendMode.COLOR_BURN);
        ColorInput blendColorInput = new ColorInput();
        blendColorInput.setPaint(Color.STEELBLUE);
        blendColorInput.setX(0);
        blendColorInput.setY(0);
        blendColorInput.setWidth(x1);
        blendColorInput.setHeight(y1);
        blend.setTopInput(blendColorInput);

        //Bloom effect
        Bloom bloom = new Bloom(0.1);

        //BoxBlur effect
        BoxBlur boxBlur = new BoxBlur();
        boxBlur.setWidth(3);
        boxBlur.setHeight(3);
        boxBlur.setIterations(3);

        //ColorInput effect
        ColorInput colorInput;
        colorInput = new ColorInput(0, 0,
                y1, x1, Color.STEELBLUE);

        //DisplacementMap effect
        FloatMap floatMap = new FloatMap();
        floatMap.setWidth(x1);
        floatMap.setHeight(y1);

        for (int i = 0; i < x1; i++) {
            double v = (Math.sin(i / 20.0 * Math.PI) - 0.5) / 40.0;
            for (int j = 0; j < y1; j++) {
                floatMap.setSamples(i, j, 0.0f, (float) v);
            }
        }
        DisplacementMap displacementMap = new DisplacementMap();
        displacementMap.setMapData(floatMap);

        //DropShadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(10.0);
        dropShadow.setOffsetY(5.0);
        dropShadow.setColor(Color.GREY);

        //GaussianBlur effect
        GaussianBlur gaussianBlur = new GaussianBlur();

        //Glow effect
        Glow glow = new Glow(1.0);


        //InnerShadow effect
        InnerShadow innerShadow = new InnerShadow(5.0, 5.0, 5.0, Color.AZURE);

        //Lighting effect
        Light.Distant light = new Light.Distant();
        light.setAzimuth(50.0);
        light.setElevation(30.0);
        light.setColor(Color.YELLOW);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(50.0);

        //MotionBlur effect
        MotionBlur motionBlur = new MotionBlur();
        motionBlur.setRadius(30);
        motionBlur.setAngle(-15.0);

        //PerspectiveTransform effect
        PerspectiveTransform perspectiveTrasform = new PerspectiveTransform();
        perspectiveTrasform.setUlx(0.0);
        perspectiveTrasform.setUly(0.0);
        perspectiveTrasform.setUrx(x1*1.5);
        perspectiveTrasform.setUry(0.0);
        perspectiveTrasform.setLrx(x1*3);
        perspectiveTrasform.setLry(y1 *2);
        perspectiveTrasform.setLlx(0);
        perspectiveTrasform.setLly(y1);

        //Reflection effect
        Reflection reflection = new Reflection();
        reflection.setFraction(0.7);

        //SepiaTone effect
        SepiaTone sepiaTone = new SepiaTone();

        //Shadow effect
        Shadow shadow = new Shadow(null, null, 100000.0);


        Label contrastLabel = new Label("Contrast:");
        Label hueLabel = new Label("Hue:");
        Label saturationLabel = new Label("Saturation:");
        Label brightnessLabel = new Label("Brightness:");

        Slider contrastSlider = this.createSlider(ADJUST_TYPE_CONTRAST);
        Slider hueSlider = this.createSlider(ADJUST_TYPE_HUE);
        Slider saturationSlider = this.createSlider(ADJUST_TYPE_SATURATION);
        Slider brightnessSlider = this.createSlider(ADJUST_TYPE_BRIGHTNESS);


        VBox root1 = new VBox();
        root1.setPadding(new Insets(10));

        root1.getChildren().addAll(contrastLabel, contrastSlider,
                hueLabel, hueSlider,
                saturationLabel, saturationSlider,
                brightnessLabel, brightnessSlider, imageView);

        imageView.setEffect(colorAdjust);


        Effect effects[] = {
                blend,
                bloom,
                boxBlur,
                colorInput,
                displacementMap,
                dropShadow,
                gaussianBlur,
                glow,
                innerShadow,
                lighting,
                motionBlur,
                perspectiveTrasform,
                reflection,
                sepiaTone,
                shadow
        };

        ChoiceBox choiceBox = new ChoiceBox(
                FXCollections.observableArrayList(
                        "Blend", "Bloom", "BoxBlur",
                        "ColorInput", "DisplacementMap", "DropShadow",
                        "GaussianBlur", "Glow", "InnerShadow",
                        "Lighting", "MotionBlur", "PerspectiveTransform",
                        "Reflection", "SepiaTone", "Shadow"
                ));
        choiceBox.getSelectionModel().selectFirst();

        choiceBox.getSelectionModel().selectedIndexProperty()
                .addListener((ObservableValue<? extends Number> observable,
                              Number oldValue, Number newValue) -> {
                    imageView.setEffect(effects[newValue.intValue()]);
                });

        ImageView retrievedImage = new ImageView();
        Label labelPath = new Label();

        Button btnSnapShot = new Button("Take SnapShot");
        btnSnapShot.setOnAction((ActionEvent event) -> {
            File savedFile = takeSnapShot(imageView);
            retrieveImage(savedFile, retrievedImage, labelPath);
        });

        Button btnSaveImage = new Button("Save");
        btnSaveImage.setOnAction((ActionEvent event) -> {
            File savedFile = null;
            try {
                savedFile = saveImage(imageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
            retrieveImage(savedFile, retrievedImage, labelPath);
        });

        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        HBox hBox = new HBox(5);
        Button back3=new Button("<<");
        hBox.getChildren().addAll(back3,choiceBox,btnSnapShot,btnSaveImage);
        hBox.setAlignment(Pos.TOP_CENTER);
        vBox.getChildren().addAll(hBox, imageView, retrievedImage, labelPath);

        VBox root = new VBox();
        root.getChildren().addAll(vBox,root1);

        scene_editor = new Scene(root, 500, 600);



        // scene_choose
        Button choose_edit=new Button("   EDIT\nIMAGE");
        Button choose_next=new Button(" >>");
        Button choose_previous=new Button("PREVIOUSLY\n      EDITED");
        Button choose_go_back=new Button("<<");
        choose_edit.setBackground(new Background(new BackgroundFill(Color.DARKGOLDENROD,null,null)));
        choose_previous.setBackground(new Background(new BackgroundFill(Color.DARKGOLDENROD,null,null)));
        choose_go_back.setBackground(new Background(new BackgroundFill(Color.LEMONCHIFFON,null,null)));
        choose_next.setBackground(new Background(new BackgroundFill(Color.LEMONCHIFFON,null,null)));
        choose_edit.setPrefSize(100,100);
        choose_go_back.setPrefSize(40,40);
        choose_next.setPrefSize(40,40);
        choose_previous.setPrefSize(100,100);
        GridPane gridPane_choose=new GridPane();
        GridPane.setConstraints(choose_edit,35,35);
        GridPane.setConstraints(choose_previous,26,35);
        GridPane.setConstraints(choose_go_back,1,1);
        GridPane.setConstraints(choose_next,100,1);
        gridPane_choose.setHgap(3);
        gridPane_choose.setVgap(5);
        gridPane_choose.setBackground(new Background(b));
        gridPane_choose.getChildren().addAll(choose_edit,choose_go_back,choose_next);
        scene_choose=new Scene(gridPane_choose,500,600);


        // scene_login
        Button button_login=new Button("LOGIN");
        Button button_root=new Button("/");
        button_root.setMaxSize(5,5);
        Label login_label=new Label();
        login_label.setText("AMR EDITOR");
        login_label.setFont(new Font("",30));
        Label login_user_label=new Label();
        login_user_label.setText("USER NAME");
        TextField textField_login=new TextField() ;
        textField_login.setEditable(true);
        textField_login.setPrefSize(40,20);
        GridPane gridPane_login=new GridPane();
        gridPane_login.setVgap(5);
        gridPane_login.setHgap(3);
        GridPane.setConstraints(button_root,80,82);
        GridPane.setConstraints(login_user_label,40,34);
        GridPane.setConstraints(button_login,40,37);
        GridPane.setConstraints(login_label,40,5);
        GridPane.setConstraints(textField_login,40,35);
        primaryStage.setTitle("AMR Editor");
        gridPane_login.getChildren().addAll(button_root,login_label,textField_login,button_login,login_user_label);
        gridPane_login.setBackground(new Background(b));
        scene_login=new Scene(gridPane_login,500,600);
        primaryStage.setScene(scene_login);
        button_login.setOnAction(e -> {
            username=textField_login.getText();
            if(username.isEmpty())
            {
                primaryStage.setScene(scene_login);
            }
            else{
                primaryStage.setScene(scene_choose);
            }
        });
        choose_edit.setOnAction(btnLoadEventListener);
        choose_next.setOnAction(e -> {
            primaryStage.setScene(scene_editor);
        });

        PasswordField passwordField_root_login=new PasswordField();
        Button back1 = new Button("<<");
        back1.setAlignment(Pos.BOTTOM_LEFT);
        passwordField_root_login.setMaxSize(140,20);
        passwordField_root_login.setEditable(true);
        Label label_password=new Label();
        label_password.setText("ENTER PASSWORD");
        Button enter_root =new Button("ENTER");
        VBox vBox_root_check = new VBox(label_password,passwordField_root_login,enter_root,back1);
        vBox_root_check.setAlignment(Pos.CENTER);
        vBox_root_check.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET,null,null)));
        Scene scene_root_check = new Scene(vBox_root_check,400,600);


        accordion = new Accordion();
        VBox root_display = new VBox();
        Button back2 = new Button("<<");
        back2.setAlignment(Pos.BOTTOM_LEFT);
        root_display.getChildren().addAll(back2,accordion);
        Scene scene_see_edited =new Scene(root_display,400,600);


        button_root.setOnAction(e -> primaryStage.setScene(scene_root_check));




        enter_root.setOnAction(e -> {
            if(passwordField_root_login.getText().equals("monimkd07"))
            {
                try {
                    get_num_item();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                try {
                    put_items();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                primaryStage.setScene(scene_see_edited);
            }
            else primaryStage.setScene(scene_root_check);
        });
        back1.setOnAction( e -> primaryStage.setScene(scene_login));
        back2.setOnAction( e -> primaryStage.setScene(scene_login));
        back3.setOnAction(e -> primaryStage.setScene(scene_choose));
        choose_go_back.setOnAction(e -> primaryStage.setScene(scene_login));

    }

    EventHandler<ActionEvent> btnLoadEventListener
            = new EventHandler<ActionEvent>(){

        @Override
        public void handle(ActionEvent t) {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

            //Show open file dialog
            File file = fileChooser.showOpenDialog(null);

            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    };

    public static void main(String[] args) throws SQLException, FileNotFoundException {
        register();
        launch(args);
    }
}