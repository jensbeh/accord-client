package de.uniks.stp.controller.snake.model;


import java.util.Random;

import static de.uniks.stp.controller.snake.Constants.*;

public class Food {
    private int foodSize;
    private int posX;
    private int posY;

    public Food() {
        this.foodSize = FIELD_SIZE;
        Random rand = new Random();
        this.posX = rand.nextInt(COLUMN) * FIELD_SIZE;
        this.posY = rand.nextInt(ROW) * FIELD_SIZE;
    }

    public int getFoodSize() {
        return this.foodSize;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }
}
