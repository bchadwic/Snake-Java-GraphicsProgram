/**
 * @author Ben Chadwick
 * @version 1.0
 * created 8/18/20
 * This program represents a game of snake
 */

package game;

import java.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;

import acm.graphics.*;

import acm.program.*;

import static game.Direction.*;

public class SnakeGame extends GraphicsProgram {

    // game-layout fields
    private final GRect[] gridLayout = new GRect[625];
    private int green = 255, red = 0;
    private final int
            WIDTH = 720, HEIGHT = 800,
            BOX = 18, OFFSET = 2,
            MAP_OFFSET = 120, GRID_SIZE = 24;


    // snake fields
    private int length = 6;
    public Direction direction = LEFT;
    public final Color SNAKE_COLOR = Color.GREEN;
    private ArrayList<GRect> snakeBody = new ArrayList<GRect>();

    // session variables
    private boolean running = true;
    private int score = 0, highScore = 0, delay = 150;
    private final Random rand = new Random();
    private final GImage apple = new GImage("src/images/apple.png");

    // game labels
    private final GLabel
            scoreLabel = new GLabel("Score: " + score, 340, 90),
            GOLabel = new GLabel("Game Over", 327, 70),
            startLabel = new GLabel("Click to Begin", 312, 70),
            highScoreLabel = new GLabel("High Score: " + highScore, 318, 40),
            directionsLabel = new GLabel("Directions: Use the Arrow Keys to Collect the Apples", 115, 620),
            restartLabel = new GLabel("Click to Reset", 312, 620);


    @Override
    public void init() {
        setSize(WIDTH, HEIGHT);
        setVisible(true);
        addKeyListeners();
        add(apple);
        add(startLabel);
        add(scoreLabel);
        add(directionsLabel);
        add(GOLabel);
        add(restartLabel);
        add(highScoreLabel);
        GOLabel.setVisible(false);
        restartLabel.setVisible(false);
        restartLabel.setFont(new Font("SanSerif Bold", Font.BOLD, 20));
        scoreLabel.setFont(new Font("SanSerif Bold", Font.BOLD, 20));
        startLabel.setFont(new Font("SanSerif Bold", Font.BOLD, 20));
        directionsLabel.setFont(new Font("SanSerif Bold", Font.BOLD, 20));
        GOLabel.setFont(new Font("SanSerif Bold", Font.BOLD, 20));
        highScoreLabel.setFont(new Font("SanSerif Bold", Font.BOLD, 20));
        setBackground(new Color(10, 255, 0));
    }

    @Override
    public void run() {
        initializeSnake();
        waitForClick();
        startLabel.setVisible(false);
        directionsLabel.setVisible(false);
        while (running) {
            wallCollision();
            appleCollision();
            tailCollision();
            pause(delay);

            /* while running, the snake moves in the direction by rotating the ArrayList indexes,
            and by moving the individual snakebody link GRects
             */
            switch (direction) {
                case RIGHT:
                    snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX() + BOX + OFFSET, snakeBody.get(0).getY());
                    indexRotate();
                    break;
                case DOWN:
                    snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX(), snakeBody.get(0).getY() + BOX + OFFSET);
                    indexRotate();
                    break;
                case LEFT:
                    snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX() - BOX - OFFSET, snakeBody.get(0).getY());
                    indexRotate();
                    break;
                case UP:
                    snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX(), snakeBody.get(0).getY() - BOX - OFFSET);
                    indexRotate();
                    break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent pressed) {
        // for each valid keyPress event, the snake changes direction and sets the location of the snakes head
        if ((pressed.getKeyCode() == KeyEvent.VK_RIGHT) && (direction != RIGHT) && (direction != LEFT) && running) {
            direction = RIGHT;
            snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX() + BOX + OFFSET, snakeBody.get(0).getY());
            move();
        } else if ((pressed.getKeyCode() == KeyEvent.VK_DOWN) && (direction != DOWN) && (direction != UP) && running) {
            direction = DOWN;
            snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX(), snakeBody.get(0).getY() + BOX + OFFSET);
            move();
        } else if ((pressed.getKeyCode() == KeyEvent.VK_LEFT) && (direction != LEFT) && (direction != RIGHT) && running) {
            direction = LEFT;
            snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX() - BOX - OFFSET, snakeBody.get(0).getY());
            move();
        } else if ((pressed.getKeyCode() == KeyEvent.VK_UP) && (direction != DOWN) && (direction != UP) && running) {
            direction = UP;
            snakeBody.get(length - 1).setLocation(snakeBody.get(0).getX(), snakeBody.get(0).getY() - BOX - OFFSET);
            move();
        }
    }

    /**
     * indexRotate rotates the indexes, so that the head is always index 0,
     * even though the GRects continuously rotate while the snake moves around the board
     */
    public void indexRotate() {
        IntStream.range(0, length - 1).forEach(i -> Collections.swap(snakeBody, i, length - 1));
    }

    /**
     * wallCollision checks to see if the bounds of the snakes head is out of the map boundaries
     */
    public void wallCollision() {
        if (snakeBody.get(0).getX() < MAP_OFFSET || snakeBody.get(0).getX() >= WIDTH - MAP_OFFSET) {
            gameOver();
        } else if (snakeBody.get(0).getY() < MAP_OFFSET || snakeBody.get(0).getY() >= WIDTH - MAP_OFFSET) {
            gameOver();
        }
    }

    /**
     * tailCollision checks to see if the snakes head hit any part of the tail
     * (impossible to hit unless snake is longer than 6)
     */
    public void tailCollision() {
        for (int i = 6; i < length; i++) {
            if (snakeBody.get(0).getBounds().intersects(snakeBody.get(i).getBounds())) {
                gameOver();
                snakeBody.get(0).setVisible(false);
            }
        }
    }

    /**
     * apple creates an apple in a random location, speeds up the game to make it more challenging and updates the score
     */
    public void apple() {
        apple.setLocation(rand.nextInt(GRID_SIZE) * 20 + MAP_OFFSET, rand.nextInt(GRID_SIZE) * 20 + MAP_OFFSET);
        apple.setSize(BOX, BOX);
        apple.setVisible(true);
        this.add(apple);
        speedUp();
        scoreLabel.setLabel("Score: " + score);
    }

    /**
     * appleCollision tests whether the snake intersected with the apple,
     * if so the snake gets bigger and the score increases
     */
    public void appleCollision() {
        if ((snakeBody.get(0).getBounds().intersects(apple.getBounds())) && (apple.isVisible())) {
            apple.setVisible(false);
            addLink();
            score++;
            apple();
            background();
        }
    }

    /**
     * addLink adds the next link to the front of the snakebody, and in the direction the snake is going
     */
    public void addLink() {
        switch (direction) {
            case RIGHT:
                snakeBody.add(new GRect((snakeBody.get(0).getX() + BOX + OFFSET), snakeBody.get(0).getY(), BOX, BOX));
                length++;
                drawLink();
                break;
            case DOWN:
                snakeBody.add(new GRect(snakeBody.get(0).getX(), (snakeBody.get(0).getY() + BOX + OFFSET), BOX, BOX));
                length++;
                drawLink();
                break;
            case LEFT:
                snakeBody.add(new GRect((snakeBody.get(0).getX() - BOX - OFFSET), snakeBody.get(0).getY(), BOX, BOX));
                length++;
                drawLink();
                break;
            case UP:
                snakeBody.add(new GRect(snakeBody.get(0).getX(), (snakeBody.get(0).getY() - BOX - OFFSET), BOX, BOX));
                length++;
                drawLink();
                break;
        }
    }

    /**
     * background incrementally changes the background from red to green as the score increases
     */
    public void background() {
        if (red <= 230)
            red = red + 25;
        else if (green >= 25)
            green = green - 25;
        setBackground(new Color(red, green, 0));
    }

    /**
     * gameOver stops the game, sets the highscore, and calls restart
     */
    public void gameOver() {
        snakeBody.get(0).setVisible(false);
        highScore = Math.max(score, highScore);
        highScoreLabel.setLabel("High Score: " + highScore);
        GOLabel.setVisible(true);
        running = false;
        restart();
    }

    /**
     * speedUp decreases the game delay which in turn speeds the game up
     */
    public void speedUp() {
        if (delay > 70)
            delay = delay - 3;
    }

    /**
     * restart waits for the player click, which calls run
     */
    public void restart() {
        restartLabel.setVisible(true);
        waitForClick();
        run();
    }

    /**
     * initializeSnake sets the game map and the snake
     */
    public void initializeSnake() {
        restartLabel.setVisible(false);
        startLabel.setVisible(true);
        directionsLabel.setVisible(true);
        GOLabel.setVisible(false);
        resetGame();
        background();

        // makes sure there are no remaining elements from the prior game
        snakeBody.removeAll(snakeBody);

        // builds map in a grid fashion, assigning and individual index to each box
        int counter = 0;
        for (int i = 0, y = MAP_OFFSET; i < GRID_SIZE; i++, y = y + BOX + OFFSET) {
            for (int n = 0, x = MAP_OFFSET; n < GRID_SIZE; n++, x = x + BOX + OFFSET) {
                gridLayout[counter] = new GRect(x, y, BOX, BOX);
                gridLayout[counter].setFilled(true);
                gridLayout[counter].setColor(Color.BLACK);
                this.add(gridLayout[counter]);
                counter++;
            }
        }

        // creates a new snake in the middle of the map
        for (int i = 0, nextLink = 0; i < length; i++, nextLink += BOX + OFFSET) {
            snakeBody.add(new GRect(((MAP_OFFSET + 240) + nextLink), MAP_OFFSET + 240, BOX, BOX));
            snakeBody.get(i).setFilled(true);
            snakeBody.get(i).setColor(SNAKE_COLOR);
            this.add(snakeBody.get(i));
        }
        apple();
    }

    /**
     * resetGame resets all the fields
     */
    public void resetGame() {
        direction = LEFT;
        running = true;
        length = 6;
        delay = 150;
        green = 255;
        red = 0;
        score = 0;
    }

    /**
     * move is a helper method that calls indexRotate() and appleCollision()
     */
    public void move() {
        indexRotate();
        appleCollision();
    }

    /**
     * drawLink is a helper method to addLink() which draws the link that was last added
     */
    public void drawLink() {
        snakeBody.get(length - 1).setFilled(true);
        snakeBody.get(length - 1).setColor(SNAKE_COLOR);
        this.add(snakeBody.get(length - 1));
    }
}
