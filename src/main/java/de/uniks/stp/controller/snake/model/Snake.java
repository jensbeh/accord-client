package de.uniks.stp.controller.snake.model;

import java.util.Random;

import static de.uniks.stp.controller.snake.Constants.*;

public class Snake {

    private int snakeSize;
    private int posX;
    private int posY;

    public Snake() {
        this.snakeSize = 1;
        Random rand = new Random();
        this.posX = rand.nextInt(COLUMN);
        this.posY = rand.nextInt(ROW);
    }

    public int getSnakeSize() {
        return this.snakeSize;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }
}
