package oop.duong.rpggame.tiled;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.GdxRuntimeException;
import oop.duong.rpggame.RPGGame;

/**
 * Chuyển các MapObject (rectangle / ellipse / polygon / polyline) từ Tiled
 * thành FixtureDef của Box2D.
 *
 * - scaling  : scale của object (nếu tile bị scale trong map).
 * - relativeTo: dịch chuyển tương đối so với điểm gốc (0,0) của body.
 *
 * Code này bám rất sát với project mẫu mystictutorial.
 */
public final class TiledPhysics {

    private TiledPhysics() {
        // class util, không cho new
    }

    /**
     * Tạo ra FixtureDef phù hợp với kiểu MapObject.
     *
     * @param mapObject  object trong Tiled
     * @param scaling    scale của object (thường là (1,1))
     * @param relativeTo tọa độ gốc tương đối của body (thường là (0,0)
     *                   hoặc vị trí cell / object)
     */
    public static FixtureDef fixtureDefOf(MapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        if (mapObject instanceof RectangleMapObject rectMapObj) {
            return rectangleFixtureDef(rectMapObj, scaling, relativeTo);
        } else if (mapObject instanceof EllipseMapObject ellipseMapObj) {
            return ellipseFixtureDef(ellipseMapObj, scaling, relativeTo);
        } else if (mapObject instanceof PolygonMapObject polygonMapObj) {
            Polygon polygon = polygonMapObj.getPolygon();
            float offsetX = polygon.getX() * RPGGame.UNIT_SCALE;
            float offsetY = polygon.getY() * RPGGame.UNIT_SCALE;
            return polygonFixtureDef(polygonMapObj, polygon.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else if (mapObject instanceof PolylineMapObject polylineMapObj) {
            Polyline polyline = polylineMapObj.getPolyline();
            float offsetX = polyline.getX() * RPGGame.UNIT_SCALE;
            float offsetY = polyline.getY() * RPGGame.UNIT_SCALE;
            return polygonFixtureDef(polylineMapObj, polyline.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else {
            throw new GdxRuntimeException("Unsupported MapObject: " + mapObject);
        }
    }

    // ---------- RECTANGLE ----------

    /**
     * Box2D setAsBox() luôn coi position của body là TÂM của box,
     * nhưng trong game ta muốn body nằm ở góc dưới trái của box.
     * → Phải cộng thêm offset (boxW, boxH) để dịch tâm.
     */
    private static FixtureDef rectangleFixtureDef(RectangleMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Rectangle rectangle = mapObject.getRectangle();
        float rectX = rectangle.x;
        float rectY = rectangle.y;
        float rectW = rectangle.width;
        float rectH = rectangle.height;

        float boxX = rectX * RPGGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float boxY = rectY * RPGGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float boxW = rectW * RPGGame.UNIT_SCALE * scaling.x * 0.5f;
        float boxH = rectH * RPGGame.UNIT_SCALE * scaling.y * 0.5f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxW, boxH, new Vector2(boxX + boxW, boxY + boxH), 0f);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    // ---------- ELLIPSE / CIRCLE ----------

    private static FixtureDef ellipseFixtureDef(EllipseMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Ellipse ellipse = mapObject.getEllipse();
        float x = ellipse.x;
        float y = ellipse.y;
        float w = ellipse.width;
        float h = ellipse.height;

        float ellipseX = x * RPGGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float ellipseY = y * RPGGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float ellipseW = w * RPGGame.UNIT_SCALE * scaling.x * 0.5f;
        float ellipseH = h * RPGGame.UNIT_SCALE * scaling.y * 0.5f;

        if (MathUtils.isEqual(ellipseW, ellipseH, 0.1f)) {
            // gần như hình tròn → dùng CircleShape
            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(ellipseX + ellipseW, ellipseY + ellipseH));
            shape.setRadius(ellipseW);
            return fixtureDefOfMapObjectAndShape(mapObject, shape);
        }

        // Hình elip: convert thành đa giác với numVertices đỉnh.
        final int numVertices = 8; // PolygonShape hỗ trợ tối đa 8 đỉnh
        float angleStep = MathUtils.PI2 / numVertices;
        Vector2[] vertices = new Vector2[numVertices];

        for (int vertexIdx = 0; vertexIdx < numVertices; vertexIdx++) {
            float angle = vertexIdx * angleStep;
            float offsetX = ellipseW * MathUtils.cos(angle);
            float offsetY = ellipseH * MathUtils.sin(angle);
            vertices[vertexIdx] = new Vector2(
                ellipseX + ellipseW + offsetX,
                ellipseY + ellipseH + offsetY
            );
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    // ---------- POLYGON / POLYLINE ----------

    private static FixtureDef polygonFixtureDef(
        MapObject mapObject, // PolygonMapObject hoặc PolylineMapObject
        float[] polyVertices,
        float offsetX,
        float offsetY,
        Vector2 scaling,
        Vector2 relativeTo
    ) {
        // offsetX/Y là offset gốc của polygon trong Tiled
        offsetX = (offsetX * scaling.x) - relativeTo.x;
        offsetY = (offsetY * scaling.y) - relativeTo.y;

        float[] vertices = new float[polyVertices.length];
        for (int i = 0; i < polyVertices.length; i += 2) {
            // x
            vertices[i] = offsetX + polyVertices[i] * RPGGame.UNIT_SCALE * scaling.x;
            // y
            vertices[i + 1] = offsetY + polyVertices[i + 1] * RPGGame.UNIT_SCALE * scaling.y;
        }

        ChainShape shape = new ChainShape();
        if (mapObject instanceof PolygonMapObject) {
            shape.createLoop(vertices);   // polygon khép kín
        } else { // PolylineMapObject
            shape.createChain(vertices);  // đường gấp khúc
        }
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    // ---------- COMMON ----------

    /**
     * Tạo FixtureDef từ shape + copy các property từ MapObject
     * (friction, restitution, density, sensor).
     */
    private static FixtureDef fixtureDefOfMapObjectAndShape(MapObject mapObject, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = mapObject.getProperties().get("friction", 0f, Float.class);
        fixtureDef.restitution = mapObject.getProperties().get("restitution", 0f, Float.class);
        fixtureDef.density = mapObject.getProperties().get("density", 0f, Float.class);
        fixtureDef.isSensor = mapObject.getProperties().get("sensor", false, Boolean.class);
        return fixtureDef;
    }
}
