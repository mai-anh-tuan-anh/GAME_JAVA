package oop.duong.rpggame.asset; // 1. Đã sửa về đúng package của bạn

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.graphics.Texture; // Thêm cái này để map không bị mờ

public enum MapAsset implements Asset<TiledMap> {
    // 2. Sửa tên file cho đúng với file trong thư mục assets/maps của bạn
    MAIN("MAP.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName) {
        BaseTiledMapLoader.Parameters parameters = new TmxMapLoader.Parameters();

        // 3. Chỗ này video trỏ vào file .tiled-project.
        // Vì bạn có thể chưa có file đó, ta trỏ tạm vào thư mục "maps" để tránh lỗi.
        // Nó giúp game hiểu được các Class/Type bạn đặt trong Tiled.
       // parameters.projectFilePath = "maps/" + mapName;

        // 4. Thêm bộ lọc này (Video chuẩn thường sẽ có đoạn này sau đó).
        // Nếu không có 3 dòng này, khi nhân vật di chuyển, map sẽ bị nhấp nháy hoặc vỡ hạt.
        parameters.generateMipMaps = true;
        parameters.textureMinFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameters.textureMagFilter = Texture.TextureFilter.Linear;

        this.descriptor = new AssetDescriptor<>("maps/" + mapName, TiledMap.class, parameters);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return descriptor;
    }
}
