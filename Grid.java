import java.awt.*;
import java.util.*;
import java.util.List;

public class Grid {
    private int size;
    private int actualSize;
    private Tile[][] tiles;
    private boolean[][] justMerged;
    private ArrayList<Point>[] startPoints = new ArrayList[6];  // Starting points for collection of tiles

    private int goal;
    private boolean finished;
    private int totalCount;

    private Random myRandom;

    public Grid(int n, int target) {
        size = n;  // 5
        actualSize = n*2-1;  // 9
        tiles = new Tile[actualSize][actualSize];
        justMerged = new boolean[actualSize][actualSize];  // Prevent the case of 8,4,2,2 where a tile that's just merged is merged with another tile
        computeStartPoints();
        goal = target;  // Stored in power of 2 (e.g. 11 for 2048)
        myRandom = new Random();
    }
    public int getSize() {
        return size;
    }
    public int getGoal() {
        return goal;
    }
    public int getTotal() {
        return totalCount;
    }
    public int getMaxGrids() {
        // Pure math. Thanks Prof Astrachan for plustorial
        return (size*3-1)*size/2 + (size*3-2)*(size-1)/2;
    }
    
    // Coordinate operations
    private int[][] dirVec = {
            {0, 1}, {0, -1},
            {1, 1}, {-1, -1},
            {1, 0}, {-1, 0}
    };
    private int[] dirRev = {1, 0, 3, 2, 5, 4};  // Reverse direction
    protected boolean inBound(Point pos) {  // Should probably make these static
        int x = pos.x;
        int y = pos.y;

        if (x<0 || x>=size*2-1)
            return false;
        int miny = (x<size? 0: x-size+1);
        int maxy = (x<size? x+size-1: size*2-1);
        if (y<miny || y>maxy)
            return false;
        return true;
    }
    public Tile getTile(Point pos) {
        if (!inBound(pos))
            return null;
        return tiles[pos.x][pos.y];
    }
    public Point tile2point(Tile tile) {
        return tile.getPos();
    }
    protected Point moveInDir(Point pos, int dir) {  // This might be out of bounds or already full.
        return new Point(pos.x + dirVec[dir][0], pos.y + dirVec[dir][1]);
    }
    protected Point moveInDirToEnd(Point pos, int dir) {  // Move pointer all the way, stops at another block or end of grid
        Point pt = moveInDir(pos, dir);
        Point pt_prev = pos;
        while (isEmpty(pt)) {
            pt_prev = pt;
            pt = moveInDir(pt, dir);
        }
        return pt_prev;
    }
    public boolean isEmpty(Point pos) {
        return inBound(pos) && (tiles[pos.x][pos.y] == null);
    }
    public boolean isAdjacent(Point pos1, Point pos2) {
        if (!inBound(pos1) || !inBound(pos2))
            return false;
        for (int i=0; i<6; i++)
            if (moveInDir(pos1, i).equals(pos2))
                return true;
        return false;
    }

    // Movements
    public void createTile(Point pos, int value) {
        if (!isEmpty(pos)) {
            // Exception
            return;
        }
        Tile t = new Tile(pos, value);
        tiles[pos.x][pos.y] = t;
        totalCount++;
    }
    public void createRandomTile() {
        if (isFull())
            return;
        List<Point> vacantCells = new LinkedList<>();
        for (int i=0; i<actualSize; i++)
            for (int j=0; j<actualSize; j++)
                if (isEmpty(new Point(i, j)))
                    vacantCells.add(new Point(i, j));

        int val = 1;  // 2
        if (myRandom.nextDouble() <= 0.2)
            val = 2;  // 4
        createTile(vacantCells.get(myRandom.nextInt(vacantCells.size())), val);
    }
    public void removeTile(Point pos) {
        if (getTile(pos) == null) {
            // Exception
            return;
        }
        tiles[pos.x][pos.y].erase();
        tiles[pos.x][pos.y] = null;
    }
    public void removeTile(Tile tile) {  // Just in case
        removeTile(tile.getPos());
        totalCount--;
    }
    public void moveTileTo(Point start, Point end) {
        if (getTile(start) == null) {
            // IndexOutOfBoundsException
            return;
        }
        if (!isEmpty(end)) {
            // IndexOutOfBoundsException
            return;
        }

        Tile tile = getTile(start);
        tiles[start.x][start.y] = null;
        tiles[end.x][end.y] = tile;
        tile.moveTo(end);
    }
    private boolean mergeable(Point toErase, int dir) { // Check whether a cell can be merged with the next cell in direction dir
        if (getTile(toErase) == null) {
            // Exception
            return false;
        }
        Point toRetain = moveInDir(toErase, dir);
        if (getTile(toRetain) == null)
            return false;
        return (getTile(toRetain).getValue() == getTile(toErase).getValue()) && !justMerged[toRetain.x][toRetain.y];
    }
    public void merge(Point toRetain, Point toErase) {
        if (!isAdjacent(toRetain, toErase) || getTile(toRetain) == null || getTile(toErase) == null) {
            // Exception
            return;
        }
        Tile x = getTile(toRetain);
        Tile y = getTile(toErase);
        // TODO: Probably write Tile.moveAndMerge() for the sake of animations. If not, the tile will move first before disappearing
        x.upgrade();
        if (x.getValue() >= goal)
            finished = true;
        removeTile(toErase);
        justMerged[toRetain.x][toRetain.y] = true;  // Prevent the merged cell from being merged again later
    }
    public void merge(Point toErase, int dir) {  // For convenience
        if (getTile(toErase) == null) {
            // Exception
            return;
        }
        Point toRetain = moveInDir(toErase, dir);
        if (getTile(toRetain) == null)
            return;
        merge(toRetain, toErase);
    }

    // Methods for swiping
    protected void computeStartPoints() {
        // Brute force method...
        startPoints[0] = new ArrayList<Point>();
        for (int i=0; i<size; i++)  // (0 to 4, 0)
            startPoints[0].add(new Point(i, 0));
        for (int i=size; i<actualSize; i++)  // (5 to 8, 1 to 4)
            startPoints[0].add(new Point(i, i-size+1));

        startPoints[1] = new ArrayList<Point>();
        for (int i=0; i<size; i++)  // (0 to 4, 4 to 8)
            startPoints[1].add(new Point(i, i+size-1));
        for (int i=size; i<actualSize; i++)  // (5 to 8, 8)
            startPoints[1].add(new Point(i, actualSize-1));

        startPoints[2] = new ArrayList<Point>();
        for (int i=size-1; i<=0; i++)  // (0, 4 to 0)
            startPoints[2].add(new Point(0, i));
        for (int i=1; i<size; i++)  // (1 to 4, 0)
            startPoints[2].add(new Point(i, 0));

        startPoints[3] = new ArrayList<Point>();
        for (int i=size-1; i<actualSize; i++)  // (4 to 8, 8)
            startPoints[3].add(new Point(i, actualSize-1));
        for (int i=actualSize-2; i<=size-1; i++)  // (8, 7 to 4)
            startPoints[3].add(new Point(actualSize-1, i));

        startPoints[4] = new ArrayList<Point>();
        for (int i=0; i<size; i++)  // (0, 0 to 4)
            startPoints[4].add(new Point(0, i));
        for (int i=1; i<size; i++)  // (1 to 4, 5 to 8)
            startPoints[4].add(new Point(i, i+size-1));

        startPoints[5] = new ArrayList<Point>();
        for (int i=size-1; i<actualSize; i++)  // (4 to 8, 0 to 4)
            startPoints[5].add(new Point(i, i-size+1));
        for (int i=size; i<actualSize; i++)  // (8, 5 to 8)
            startPoints[5].add(new Point(actualSize-1, i));
    }
    protected List<Tile> collectAllTilesInDir(int dir, Point start) {  // Get all tiles in a row in the direction of dir
        List<Tile> ret = new LinkedList<>();
        Point pt = start;
        while (inBound(pt)) {
            if (!isEmpty(pt))
                ret.add(tiles[pt.x][pt.y]);
            pt = moveInDir(pt, dir);
        }
        return ret;
    }
    protected List<List<Tile>> organizeByTiles(int dir) {  // Collate all rows in direction dir
        List<List<Tile>> ret = new ArrayList<>();
        for (Point start : startPoints[dir]) {
            ret.add(collectAllTilesInDir(dir, start));
        }
        return ret;
    }
    protected void pushOneTile(Point pos, int dir) {  // Push a particular tile in dir until it stops or merges
        if (getTile(pos) == null)
            return;
        Tile t = getTile(pos);
        Point dest = moveInDirToEnd(pos, dir);
        moveTileTo(pos, dest);
        if (mergeable(dest, dir))
            merge(dest, dir);
    }
    protected void pushOneTile(Tile tile, int dir) {  // Push a particular tile in dir until it stops or merges
        pushOneTile(tile.getPos(), dir);
    }
    protected void pushTilesInOrder(List<Tile> tileList, int dir) {  // Push all cells in the row in reverse order
        Collections.reverse(tileList);
        for (Tile tile : tileList)
            pushOneTile(tile, dir);
    }
    public void swipe(int dir) {
        if (!canSwipeInDir(dir)) {
            if (isLost())
                lose();
            return;
        }
        boolean alreadyWon = finished;
        for (int i=0; i<actualSize; i++)
            Arrays.fill(justMerged[i], false);
        for (List<Tile> list : organizeByTiles(dir))
            pushTilesInOrder(list, dir);

        if (!isFull())
            createRandomTile();

        if (alreadyWon ^ finished)
            win();
    }
    public boolean canSwipeInDir(int dir) {  // Check whether the swipe is allowed (i.e. at least 1 tile can be moved or merged)
        for (int i=0; i<actualSize; i++)
            for (int j=0; j<actualSize; j++)
                if (tiles[i][j] != null) {
                    Point pt = new Point(i, j);
                    if (!moveInDirToEnd(pt, dir).equals(pt) || mergeable(pt, dir))
                        return true;
                }
        return false;
    }

    // Methods related to ending the game
    public void win() {
        // TODO (Graphics)
    }
    public boolean isFinished() {
        return finished;
    }
    public void lose() {
        // TODO (Graphics)
    }
    public boolean isFull() {
        return totalCount >= getMaxGrids();
    }
    public boolean isLost() {
        if (!isFull())
            return false;
        for (int i=0; i<actualSize; i++)
            for (int j=0; j<actualSize; j++)
                if (tiles[i][j] != null)
                    for (int k=0; k<6; k++)
                        if (mergeable(new Point(i,j), k))
                            return false;
        return true;
    }
}
