import groovy.transform.ToString
import groovy.swing.SwingBuilder
import java.awt.*
import java.awt.event.*
import javax.swing.JPanel
import javax.swing.WindowConstants

class Game {

    static long DELAY = 0, PERIOD = 50

    def timer = new Timer()
    def frame, screen = new GameScreen()
    def keys = new HashSet()

    Game(title, width, height) {
        def swing = new SwingBuilder()
        screen.setPreferredSize(new Dimension(width, height))
        frame = swing.frame(title: title,
                            defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE,
                            keyPressed: { e ->
                                keys.add(e.getKeyCode())
                            },
                            keyReleased: { e ->
                                keys.remove(e.getKeyCode())
                            }) {
            panel(screen)
        }
        frame.pack()
    }

    def start() {
        def gameloop = [
            run: {
                _processInput()
                _update()
                screen.repaint()
             }
        ] as TimerTask
        timer.schedule(gameloop, DELAY, PERIOD)
    }

    def stop() {
        timer.cancel()
    }

    def show(boolean b) {
        frame.setVisible(b)
    }

    def _processInput() {}
    def _update() {}
    def _render(g, o) {}

    class GameScreen extends JPanel {
        void paintComponent(Graphics g) {
            _render(g, this)
        }
    }

}

class Tetris extends Game {

    static def SPEED = 2 // [/sec]
    static def TIME = 1000 / SPEED // [ms]
    static def TITLE = "Tetris"
    static def CELL_SIZE = 20
    static def FIELD_WIDTH = 10
    static def FIELD_HEIGHT = 20
    static def SCREEN_WIDTH = CELL_SIZE * FIELD_WIDTH
    static def SCREEN_HEIGHT = CELL_SIZE * FIELD_HEIGHT
    static def OFFSET = 1
    static def COLORS = [0: Color.BLACK,
                         1: Color.CYAN,
                         2: Color.YELLOW,
                         3: Color.GREEN,
                         4: Color.RED,
                         5: Color.BLUE,
                         6: Color.ORANGE,
                         7: Color.MAGENTA]

    def field = new Field(FIELD_WIDTH, FIELD_HEIGHT)
    long waitingTime, pastTime

    Tetris() {
        super(TITLE, SCREEN_WIDTH, SCREEN_HEIGHT)
    }

    def _processInput() {
        if (keys.contains(KeyEvent.VK_Q))
            System.exit(0)
        else if (keys.contains(KeyEvent.VK_UP))
            field.rotateTetrimino()
        else if (keys.contains(KeyEvent.VK_DOWN))
            field.dropTetrimino()
        else if (keys.contains(KeyEvent.VK_LEFT))
            field.moveTetriminoToLeft()
        else if (keys.contains(KeyEvent.VK_RIGHT))
            field.moveTetriminoToRight()
        keys.clear()
    }

    def _update() {
        if (_wait(TIME))
            return
        field.dropTetrimino()
    }

    def _wait(time) {
        long currentTime = System.currentTimeMillis()
        waitingTime += currentTime - pastTime
        pastTime = currentTime
        if (waitingTime < time) {
            return true
        } else {
            waitingTime = 0
            return false
        }
    }

    def _render(g, o) {
        _drawField(g)
        _drawTetrimino(g)
    }

    def _drawField(g) {
        for (y in 0..<FIELD_HEIGHT) {
            for (x in 0..<FIELD_WIDTH) {
                def val = field.cells[y][x]
                g.setColor(COLORS[val])
                _drawCell(g, x, y)
            }
        }
    }

    def _drawTetrimino(g) {
        def tetrimino = field.tetrimino
        for (j in 0..<tetrimino.getHeight()) {
            for (i in 0..<tetrimino.getWidth()) {
                def val = tetrimino.shape[j][i]
                if (val) {
                    g.setColor(COLORS[val])
                    _drawCell(g, i + tetrimino.x, j + tetrimino.y)
                }
            }
        }
    }

    def _drawCell(g, x, y) {
        g.fillRect(x * CELL_SIZE, y * CELL_SIZE,
                CELL_SIZE - OFFSET, CELL_SIZE - OFFSET)
    }

}

class Field {

    Integer width, height
    Integer[][] cells
    def tetrimino = new Tetrimino([[]])

    Field(width, height) {
        this.width = width
        this.height = height
        cells = new Integer[height][width]
        reset()
        createTetrimino()
    }

    def reset() {
        for (y in 0..<height)
            for (x in 0..<width)
                cells[y][x] = 0
    }

    def createTetrimino() {
        tetrimino = new Tetrimino()
        tetrimino.x = (width - tetrimino.getWidth()) * 0.5 + 0.5
        tetrimino.y = 0 - tetrimino.getHeight()
        return tetrimino
    }

    def rotateTetrimino() {
        def newShape = tetrimino.rotate()
        if (!_checkCollision(newShape, tetrimino.x, tetrimino.y))
            tetrimino.shape = newShape
    }

    def moveTetriminoToLeft() {
        if (!_checkCollision(tetrimino.shape, tetrimino.x - 1, tetrimino.y))
            tetrimino.x--
    }

    def moveTetriminoToRight() {
        if (!_checkCollision(tetrimino.shape, tetrimino.x + 1, tetrimino.y))
            tetrimino.x++
    }

    def dropTetrimino() {
        if (!_checkCollision(tetrimino.shape, tetrimino.x, tetrimino.y + 1)) {
            tetrimino.y++
        } else {
            if (_checkGameOver()) {
                reset()
            } else {
                _merge()
                def lines = _checkLines()
                if (!lines.isEmpty()) {
                    _deleteLines(lines)
                    _dropLines(lines)
                }
            }
            tetrimino = createTetrimino()
        }
    }

    def _merge() {
        for (j in 0..<tetrimino.getHeight()) {
            for (i in 0..<tetrimino.getWidth()) {
                def val = tetrimino.shape[j][i]
                if (val)
                    cells[j + tetrimino.y][i + tetrimino.x] = val
            }
        }
    }

    def _checkCollision(shape, tx, ty) {
        def tWidth = shape[0].size(), tHeight = shape.size()
        for (j in 0..<tHeight) {
            for (i in 0..<tWidth) {
                if (shape[j][i]) {
                    def x = i + tx, y = j + ty
                    if (y < 0)
                        continue
                    if (x < 0 || x >= width || y >= height)
                        return true
                    if (cells[y][x])
                        return true
                }
            }
        }
        return false
    }

    def _checkGameOver() {
        return tetrimino.y < 0
    }

    def _checkLines() {
        def lines = []
        for (y in 0..<height) {
            def isLine = true
            for (x in 0..<width)
                isLine &= (cells[y][x] != 0)
            if (isLine)
                lines.push(y)
        }
        return lines
    }

    def _deleteLines(lines) {
        for (line in lines)
            for (x in 0..<width)
                cells[line][x] = 0
    }

    def _dropLines(lines) {
        for (line in lines)
            for (y in line..1)
                for (x in 0..<width)
                    cells[y][x] = cells[y - 1][x]
    }

}

@ToString(includeNames=true, includeFields=true)
class Tetrimino {

    static def shapes = [
        [
            [1, 1, 1, 1],
        ],
        [
            [2, 2],
            [2, 2],
        ],
        [
            [0, 3, 3],
            [3, 3, 0],
        ],
        [
            [4, 4, 0],
            [0, 4, 4],
        ],
        [
            [5, 0, 0],
            [5, 5, 5],
        ],
        [
            [0, 0, 6],
            [6, 6, 6],
        ],
        [
            [0, 7, 0],
            [7, 7, 7],
        ]
    ];

    Integer[][] shape
    Integer x = 0, y = 0 // field's coordinate

    Tetrimino(shape) {
        this.shape = shape
    }

    Tetrimino() {
        Integer rand = Math.random() * shapes.size()
        shape = shapes[rand]
    }

    def getWidth() {
        return shape[0].size()
    }

    def getHeight() {
        return shape.size()
    }

    def rotate() {
        def width = getWidth(), height = getHeight()
        def newShape = new Integer[width][height]
        for (y in 0..<height)
            for (x in 0..<width)
                newShape[x][height-1-y] = shape[y][x]
        return newShape
    }

}


def tetris = new Tetris()
tetris.show(true)
tetris.start()
