package TilingDemo;

/**
 * Created by Patrick on 4/22/2016.
 */
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.geometry.Point2D;
import javafx.stage.*;

public class PenroseDemo extends Application {
    public  static Pane  rootNode = new Pane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
// Set a title for the stage
        stage.setTitle("Penrose Tiling Demonstration");

// Create a root node
        Scene scene = new Scene(rootNode, 1000, 1000);
        stage.setScene(scene);

        PTile tile1, tile2;


        Point2D p1 = new Point2D(500.0,500.0);
        Point2D p2 = new Point2D(600.0,600.0);

        tile1 = new PTile(PTile.KITE, new Point2D(300,300));
        tile2 = new PTile(PTile.DART, new Point2D(700,700));

        boolean addtile;
        addtile = rootNode.getChildren().add(tile1.myPolygon);
        addtile = rootNode.getChildren().add(tile2.myPolygon);
        stage.show();
    }
}
