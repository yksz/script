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

class LifeGame extends Game {

    static def SPEED = 5 // [/sec]
    static def TIME = 1000 / SPEED // [ms]
    static def TITLE = "LifeGame"
    static def CELL_SIZE = 10
    static def WIDTH = 30
    static def HEIGHT = 30
    static def SCREEN_WIDTH = CELL_SIZE * WIDTH
    static def SCREEN_HEIGHT = CELL_SIZE * HEIGHT
    static def OFFSET = 1
    static def COLORS = [0: Color.BLACK,
                         1: Color.CYAN]

    Integer[][] cells = new Integer[HEIGHT][WIDTH]
    boolean isStarted = false
    long waitingTime, pastTime

    LifeGame() {
        super(TITLE, SCREEN_WIDTH, SCREEN_HEIGHT)
        reset()
        birth()
    }

    def reset() {
        for (y in 0..<HEIGHT)
            for (x in 0..<WIDTH)
                cells[y][x] = 0
    }

    def birth() {
        for (y in 0..<HEIGHT)
            for (x in 0..<WIDTH)
                if ((int)(Math.random() * 1.1))
                    cells[y][x] = 1
    }

    def grow() {
        for (y in 0..<HEIGHT)
            for (x in 0..<WIDTH)
                rule(x, y)
    }

    def rule(x, y) {
        def neighbor = 0
        for (j in y-1..y+1) {
            for (i in x-1..x+1) {
                if (i < 0 || i >= WIDTH || j < 0 || j >= HEIGHT)
                    continue
                if (!(i == x && j == y))
                    neighbor += cells[j][i]
            }
        }
        if (neighbor == 3)
            cells[y][x] = 1
        else if (neighbor <= 1 || neighbor >= 4)
            cells[y][x] = 0
    }

    def _processInput() {
        if (keys.contains(KeyEvent.VK_Q))
            System.exit(0)
        else if (keys.contains(KeyEvent.VK_S))
            isStarted = !isStarted
        else if (keys.contains(KeyEvent.VK_R))
            reset()
        else if (keys.contains(KeyEvent.VK_B))
            birth()
        else if (keys.contains(KeyEvent.VK_G))
            grow()
        keys.clear()
    }

    def _update() {
        if (_wait(TIME))
            return
        if (isStarted)
            grow()
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
        _drawCells(g)
    }

    def _drawCells(g) {
        for (y in 0..<HEIGHT) {
            for (x in 0..<WIDTH) {
                def val = cells[y][x]
                g.setColor(COLORS[val])
                _drawCell(g, x, y)
            }
        }
    }

    def _drawCell(g, x, y) {
        g.fillRect(x * CELL_SIZE, y * CELL_SIZE,
                CELL_SIZE - OFFSET, CELL_SIZE - OFFSET)
    }

}


def lifegame = new LifeGame()
lifegame.show(true)
lifegame.start()
