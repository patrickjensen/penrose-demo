package TilingDemo;

/**
 * Created by Patrick on 4/22/2016.
 */
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.geometry.Point2D;
import static java.lang.Math.*;

public class PTile {
    public final Polygon myPolygon;
    private PTile tile;
    private static final String[] TILENAME = {"Kite", "Dart"};


    public static final int KITE = 0;
    public static final int DART = 1;

    private final Point2D[] vertex = new Point2D[4];
    private int tileType = KITE;

    private static final double magnify = 6.0;
    private static final double scale = pow(2, magnify);
    private static final double fifthPI = PI / 5.0;
    private static final double invPHI = 2.0 / (1.0 + sqrt(5.0));

    private static final double[][] Headings = {
            {0.0, 3 * fifthPI, 4 * fifthPI, 7 * fifthPI},
            {0.0, 4 * fifthPI, 3 * fifthPI, 7 * fifthPI},
    };

    private static final double[] Lengths =
            {1.0, invPHI, invPHI, 1.0};

    public PTile(int pTileType, Point2D anchor) {
        this(pTileType, anchor, 0, 3*fifthPI/2.0);
    }

    public PTile(int pTileType, Point2D anchor, double inclination) {
        this(pTileType, anchor, 0, inclination);
    }

    public PTile(int pTileType, Point2D anchor, int sideStart) {
        this(pTileType, anchor, sideStart, 0.0);
    }

    private PTile(int pTileType, Point2D anchor, int buildSide, double inclination) {
/*
Default Constructor for a PTILE object

parameters for this constructor are:
int  pTileType:     Holds the values KITE = 0 and DART = 1 . This is a mandatory parameter
Point2D anchor:     This object hold the x and y coordinates of the first vertex of the tile to be constructed.
                    This is a mandatory parameter.
                    By convention,
                        the first vertex of a KITE is the tail of the KITE.
                        the first vertex of a DART is the head of the DART.
int buildSide:      This is the side (0, 1, 2, or 3) of the tile to construct, starting from anchor point.
                    This in an optional parameter; default value is provided as 0
                    Points are defined in a counterclockwide direction starting from the anchor point.
double inclination: This is the angle that side 0 of the KITE or DART makes with the x-axis.
                    This is an optional parameter; default value is provided as 0.0
*/
        double[] flattileData = new double[8];
        tileType = pTileType;

        this.vertex[buildSide] = anchor;

        for (int indx = 1; indx < 4; indx++) {
            vertex[(buildSide + indx) % 4] = getNextPoint(
                    vertex[(buildSide - 1 + indx) % 4],
                    inclination + Headings[pTileType][(buildSide - 1 + indx) % 4],
                    scale * Lengths[(buildSide - 1 + indx) % 4]
            );
        }

        // create the array of (x,y) coordinates required by Polygon
        for (int sideindx = 0; sideindx < 4; sideindx++) {
            flattileData[2 * sideindx] = vertex[sideindx].getX();
            flattileData[2 * sideindx + 1] = vertex[sideindx].getY();
        }

        Polygon tile = new Polygon(flattileData);

        tile.setFill(Color.TRANSPARENT);
        tile.setStrokeWidth(2.0);
        if (tileType == KITE)
            tile.setStroke(Color.RED);
        else tile.setStroke(Color.DARKGREEN);

        this.myPolygon = tile;

        SetTileEventHandlers();
    }

    private void SetTileEventHandlers() {
        this.myPolygon.setOnMouseEntered(e -> {
            this.myPolygon.setFill(Color.LIGHTGREY);
        });

        this.myPolygon.setOnMouseExited(e -> {
            this.myPolygon.setFill(Color.TRANSPARENT);
        });

        myPolygon.setOnMouseClicked((e) -> {
            Point2D click;
            Point2D startPoint;
            int nearSideIndex = 0;
            int[] matchingSides = new int[2];
            int[] buildTypes = new int[2];
            double inclination = 0;
            int desiredBuildType = KITE;
            int[] numberMatched = new int[4];

            int newTileSide;

            if (e.getButton() == MouseButton.PRIMARY)
                desiredBuildType = KITE;
            else
                desiredBuildType = DART;

            // get the coordinates of the cursor
            click = new Point2D(e.getSceneX(), e.getSceneY());

            // find the side closest to the cursor when clicked
            nearSideIndex = findNearSide(vertex, click);

            /*
            build new tile on the near side of base tile if possible
            */
            // find tile types and sides which are possible matches to the selected tile and side
            getMatchingSides
                    (tileType, nearSideIndex, matchingSides, buildTypes, numberMatched);

            if (desiredBuildType == KITE)
                newTileSide = matchingSides[0];
            else {     // desiredBuildType == DART
                if (numberMatched[nearSideIndex] == 2)
                    newTileSide = matchingSides[1];
                else {
                    System.out.println("NO " + TILENAME[desiredBuildType] +
                            " matches side " + nearSideIndex + " of base " + TILENAME[tileType]);
                    return;
                }
            }

            System.out.println(
                    TILENAME[tileType] + " Side " + nearSideIndex +
                            " ------> " + TILENAME[desiredBuildType] + " side " + newTileSide);

/* compute the starting point for the new tile */
            startPoint = vertex[(nearSideIndex) % 4];

/* compute the inclination for the new tile *
/
*/
            inclination = PI - Headings[buildTypes[desiredBuildType]][(newTileSide) % 4] -
                    p2pAngle(vertex[(nearSideIndex) % 4], vertex[(nearSideIndex + 1) % 4]);

            tile = new PTile(
                    desiredBuildType,
                    startPoint,
                    (newTileSide + 1) % 4,
                    inclination
            );

            boolean addtile;
            addtile = PenroseDemo.rootNode.getChildren().add(tile.myPolygon);
        });
    }

    private double pointToLineDistance(Point2D A, Point2D B, Point2D P) {
        double Ax = A.getX();
        double Ay = A.getY();
        double Bx = B.getX();
        double By = B.getY();
        double Px = P.getX();
        double Py = P.getY();

        double normalLength = Math.sqrt((Bx - Ax) * (Bx - Ax) + (By - Ay) * (By - Ay));
        return Math.abs((Px - Ax) * (By - Ay) - (Py - Ay) * (Bx - Ax)) / normalLength;
    }

    private double p2pAngle(Point2D p1, Point2D p2) {
        /* computes the angle in radians of the vector defined by  two points p1 and p2  */
        double dx = (p2.getX() - p1.getX());
        double dy = (p2.getY() - p1.getY());
        return Math.atan2(dy, dx);
    }

    private Point2D getNextPoint(Point2D initialPoint, double angle, double length) {
        return new Point2D(
                initialPoint.getX() + length * cos(angle),
                initialPoint.getY() - length * sin(angle)
        );

    }

    private int findNearSide(Point2D[] tile, Point2D cursorPos) {
        int closestSide = 0;
        double distance;
        double minDistance = Double.POSITIVE_INFINITY;
        for (int side = 0; side < 4; side++) {
            distance = pointToLineDistance(tile[side], tile[(side + 1) % 4], cursorPos);
            if (distance < minDistance) {
                minDistance = distance;
                closestSide = side;
            }
        }
        return closestSide;
    }

    private void getMatchingSides(int baseTileType,
                                  int baseTileSide,
                                  int[] matchedSides,
                                  int[] matchedTypes,
                                  int[] numberOfMatches) {

        switch (baseTileSide) {
            case 0:

                if (baseTileType == KITE) {  // input tile is a KITE
                    numberOfMatches[baseTileSide] = 2;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 3;

                    matchedTypes[1] = DART;
                    matchedSides[1] = 0;
                } else {                    // input tile is a DART
                    numberOfMatches[baseTileSide] = 2;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 0;

                    matchedTypes[1] = DART;
                    matchedSides[1] = 3;
                }
                break;
            case 1:
                if (baseTileType == KITE) {  // input tile is a KITE
                    numberOfMatches[baseTileSide] = 2;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 2;

                    matchedTypes[1] = DART;
                    matchedSides[1] = 1;
                } else {                    // input tile is a DART
                    numberOfMatches[baseTileSide] = 1;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 1;
                }
                break;
            case 2:
                if (baseTileType == KITE) {  // input tile is a KITE
                    numberOfMatches[baseTileSide] = 2;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 1;

                    matchedTypes[1] = DART;
                    matchedSides[1] = 2;
                } else {                    // input tile is a DART
                    numberOfMatches[baseTileSide] = 1;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 2;
                }
                break;
            case 3:
                if (baseTileType == KITE) {  // input tile is a KITE
                    numberOfMatches[baseTileSide] = 2;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 0;

                    matchedTypes[1] = DART;
                    matchedSides[1] = 3;
                } else {                    // input tile is a DART
                    numberOfMatches[baseTileSide] = 2;
                    matchedTypes[0] = KITE;
                    matchedSides[0] = 3;

                    matchedTypes[1] = DART;
                    matchedSides[1] = 0;
                }
                break;
        }
    }
}
