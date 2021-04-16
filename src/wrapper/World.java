package wrapper;

import Chunks.Chunk;
import Chunks.Coordinate2;
import Cube.Block;
import Cube.BlockType;
import Entities.Entity;
import Models.RawModel;
import Models.TexturedModel;
import RenderEngine.Loader;
import Textures.ModelTexture;
import Toolbox.PerlinNoiseGenerator;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

import static MienKraft.MainGameLoop.*;

public class World {

    public static final int WORLD_SIZE = 5 * 32;

    public Map<Coordinate2, Chunk> chunks = Collections.synchronizedMap(new HashMap<>());
    private final List<Chunk> chunkList = Collections.synchronizedList(new ArrayList<>());

    public static ModelTexture texture;

    private int index = 0;

    public World(Loader loader, Game game) {

        PerlinNoiseGenerator generator = new PerlinNoiseGenerator();
        Thread generatorThread = new Thread(() -> {

            while(!closeDisplay) {

                for (int x = (int) (game.getPlayer().getPosition().x - WORLD_SIZE) / 32; x < (game.getPlayer().getPosition().x + WORLD_SIZE) / 32; x++) {
                    for (int z = (int) (game.getPlayer().getPosition().z - WORLD_SIZE) / 32; z < (game.getPlayer().getPosition().z + WORLD_SIZE) / 32; z++) {

                        if (!chunks.containsKey(new Coordinate2(x * 32, z * 32))) {

                            Block[][][]  blocks = new Block[32][256][32];

                            for (int i = 0; i < 32; i++) {
                                for (int j = 0; j < 32; j++) {
                                    int height = (int) generator.generateHeight(i + (x * 32), j + (z * 32)) + 64;
                                    for(int k=0;k<height-3;k++) {
                                        blocks[i][k][j] = new Block(i, k, j, BlockType.STONE);
                                    }
                                    blocks[i][height-3][j] = new Block(i, height-3, j, BlockType.DIRT);
                                    blocks[i][height-2][j] = new Block(i, height-2, j, BlockType.DIRT);
                                    blocks[i][height-1][j] = new Block(i, height-1, j, BlockType.DIRT);
                                    blocks[i][height][j] = new Block(i , height, j, BlockType.GRASS);

                                }
                            }

                            Chunk chunk = new Chunk(blocks, new Coordinate2(x * 32, z * 32));

                            chunks.put(new Coordinate2(x * 32, z * 32), chunk);
                            chunkList.add(chunk);
                        }

                    }
                }

            }

        });
        generatorThread.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        texture = new ModelTexture(loader.loadTexture("textures/block/textures"));
        while (index < chunks.size()) {

            RawModel model123 = loader.loadToVAO(chunkList.get(index).getMesh().positions, chunkList.get(index).getMesh().uvs);
            TexturedModel texModel123 = new TexturedModel(model123, texture);
            Entity entity = new Entity(texModel123, new Vector3f(chunkList.get(index).origin.x, 0, chunkList.get(index).origin.z), 0, 0, 0, 1);
            entities.add(entity);

            chunkList.get(index).getMesh().positions = null;
            chunkList.get(index).getMesh().uvs = null;
            chunkList.get(index).getMesh().normals = null;

            index++;

        }
    }

    public void update(Loader loader) {
        while (index < chunks.size()) {

            RawModel model123 = loader.loadToVAO(chunkList.get(index).getMesh().positions, chunkList.get(index).getMesh().uvs);
            TexturedModel texModel123 = new TexturedModel(model123, texture);
            Entity entity = new Entity(texModel123, new Vector3f(chunkList.get(index).origin.x, 0, chunkList.get(index).origin.z), 0, 0, 0, 1);
            entities.add(entity);

            chunkList.get(index).getMesh().positions = null;
            chunkList.get(index).getMesh().uvs = null;
            chunkList.get(index).getMesh().normals = null;

            index++;

        }
    }

    public Block getBlock(int x, int y, int z) {
        int xGrid = x - Math.floorMod(x, 32);
        int zGrid = z - Math.floorMod(z, 32);
        return chunks.get(new Coordinate2(xGrid, zGrid)).blocks[Math.floorMod(x, 32)][y][Math.floorMod(z, 32)];
    }

    public void removeBlock(int x, int y, int z, Rendering renderer) {
        int xGrid = x - Math.floorMod(x, 32);
        int zGrid = z - Math.floorMod(z, 32);
        Chunk c = chunks.get(new Coordinate2(xGrid, zGrid));
        c.removeBlock(Math.floorMod(x, 32), y, Math.floorMod(z, 32));
        RawModel model123 = renderer.getLoader().loadToVAO(c.getMesh().positions, c.getMesh().uvs);
        TexturedModel texModel123 = new TexturedModel(model123, texture);
        Entity entity = new Entity(texModel123, new Vector3f(c.origin.x, 0, c.origin.z), 0, 0, 0, 1);
        entities.set(chunkList.indexOf(c), entity);
        c.getMesh().positions = null;
        c.getMesh().uvs = null;
        c.getMesh().normals = null;
    }
}
