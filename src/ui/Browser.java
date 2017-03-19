package ui;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Browser extends VBox {
  
  private MenuBar menubar;
  private GridPane content;
  private final TableView<GameData> game_tbl = new TableView<>();
  private final ObservableList<GameData> game_data = 
      FXCollections.observableArrayList (
          new GameData("game1", "poop", "8/10", "2 minutes ago"),
          new GameData("game2", "oop", "8/10", "3 hours ago"),
          new GameData("game3", "asd", "8/10", "4 hours ago"),
          new GameData("game4", "231", "8/10", "5 hours ago"),
          new GameData("game5", "asdfa", "8/10", "6 hours ago"),
          new GameData("game1", "poop", "8/10", "2 hours ago"),
          new GameData("game2", "oop", "8/10", "3 hours ago"),
          new GameData("game3", "asd", "8/10", "4 hours ago"),
          new GameData("game4", "231", "8/10", "5 hours ago"),
          new GameData("game5", "asdfa", "8/10", "6 hours ago"),
          new GameData("game1", "poop", "8/10", "2 hours ago"),
          new GameData("game2", "oop", "8/10", "3 hours ago"),
          new GameData("game3", "asd", "8/10", "4 hours ago"),
          new GameData("game4", "231", "8/10", "5 hours ago"),
          new GameData("game5", "asdfa", "8/10", "6 hours ago"),
          new GameData("game1", "poop", "8/10", "2 hours ago"),
          new GameData("game2", "oop", "8/10", "3 hours ago"),
          new GameData("game3", "asd", "8/10", "4 hours ago"),
          new GameData("game4", "231", "8/10", "5 hours ago"),
          new GameData("game5", "asdfa", "8/10", "6 hours ago"),
          new GameData("game1", "poop", "8/10", "2 hours ago"),
          new GameData("game2", "oop", "8/10", "3 hours ago"),
          new GameData("game3", "asd", "8/10", "4 hours ago"),
          new GameData("game4", "231", "8/10", "5 hours ago"),
          new GameData("game5", "asdfa", "8/10", "6 hours ago"),
          new GameData("game1", "poop", "8/10", "2 hours ago"),
          new GameData("game2", "oop", "8/10", "3 hours ago"),
          new GameData("game3", "asd", "8/10", "4 hours ago"),
          new GameData("game4", "231", "8/10", "5 hours ago"),
          new GameData("game5", "asdfa", "8/10", "6 hours ago"),
          new GameData("game6", "123", "8/10", "7 hours ago")
       );
  
  public Browser() {
    
    menubar = new MenuBar();
    content = new GridPane();
    
    content.setGridLinesVisible(true);
    content.setHgap(30);
    content.setVgap(30);
    content.setPadding(new Insets(0, 10, 0, 10));
    
    // Active game browser        
    game_tbl.setEditable(false);
    game_tbl.setPrefWidth(460);
    game_tbl.setPrefHeight(420);
    game_tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
    TableColumn<GameData, String> col_name = new TableColumn<>("NAME");
    col_name.setPrefWidth(160);
    col_name.setResizable(false);
    col_name.setCellValueFactory(
        new PropertyValueFactory<>("Name"));
    
    TableColumn<GameData, String> col_owner = new TableColumn<>("OWNER");
    col_owner.setPrefWidth(120);
    col_owner.setResizable(false);
    col_owner.setCellValueFactory(
        new PropertyValueFactory<>("Owner"));
    
    TableColumn<GameData, String> col_players = new TableColumn<>("PLAYERS");
    col_players.setPrefWidth(80);
    col_players.setResizable(false);
    col_players.setCellValueFactory(
        new PropertyValueFactory<>("Players"));
    
    TableColumn<GameData, String> col_time = new TableColumn<>("CREATED");
    col_time.setPrefWidth(120);
    col_time.setResizable(false);
    col_time.setCellValueFactory(
        new PropertyValueFactory<>("Time"));
    
    game_tbl.setItems(game_data);
    game_tbl.getColumns().addAll(col_name, col_owner, col_players, col_time);
    
    // Disable user reordering of columns at runtime
    game_tbl.widthProperty().addListener(new ChangeListener<Number>()
    {
        @Override
        public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth)
        {
            TableHeaderRow header = (TableHeaderRow) game_tbl.lookup("TableHeaderRow");
            header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    header.setReordering(false);
                }
            });
        }
    });
    
    game_tbl.getStyleClass().add("tbl-game");
    
    content.add(game_tbl, 0, 0, 1, 2);
    
    // Create game button
    Button newgame_btn = new Button("NEW GAME");
    newgame_btn.getStyleClass().add("btn-newgame");
    content.add(newgame_btn, 0, 2, 1, 1);
    
    // User info
    GridPane userinfo = new GridPane();
    userinfo.setGridLinesVisible(true);
    userinfo.setPrefWidth(230);
    userinfo.setPrefHeight(120);
    userinfo.setHgap(10);
    userinfo.setVgap(10);
    userinfo.setPadding(new Insets(0, 10, 0, 10));
    
    ImageView user_avatar = new ImageView();
    Label user_name = new Label("username");
    HBox user_wins = new HBox();
    
    Label ngold = new Label("4");
    Label nsilver = new Label("2");
    Label nbronze = new Label("0");
    
    user_wins.getChildren().addAll(ngold, nsilver, nbronze);
    userinfo.add(user_avatar, 0, 0, 1, 1);
    userinfo.add(user_name, 1, 0, 1, 1);
    userinfo.add(user_wins, 0, 1, 2, 1);
    userinfo.getStyleClass().add("user");
    content.add(userinfo, 1, 0, 1, 1);
        
    // Online Users / chat
    
    this.getChildren().addAll(menubar, content);
    this.getStyleClass().add("browser");
    this.setPadding(new Insets(0, 40, 0, 40));
  }
  
  public static class GameData {
    private final SimpleStringProperty game_name;
    private final SimpleStringProperty game_owner;
    private final SimpleStringProperty game_players;
    private final SimpleStringProperty game_time;
    
    private GameData(String name, String owner, String players, String time) {
      this.game_name = new SimpleStringProperty(name);
      this.game_owner = new SimpleStringProperty(owner);
      this.game_players = new SimpleStringProperty(players);
      this.game_time = new SimpleStringProperty(time);
    }
    
    public String getName() {
      return game_name.get();
    }
    
    public void setName(String name) {
      game_name.set(name);
    }
    
    public String getOwner() {
      return game_owner.get();
    }
    
    public void setOwner(String owner) {
      game_owner.set(owner);
    }
    
    public String getPlayers() {
      return game_players.get();
    }
    
    public void setPlayers(String players) {
      game_players.set(players);
    }
    
    public String getTime() {
      return game_time.get();
    }
    
    public void setTime(String time) {
      game_time.set(time);
    }
  }
  
}
