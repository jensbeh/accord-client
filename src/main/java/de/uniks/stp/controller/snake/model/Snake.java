package de.uniks.stp.controller.snake.model;

public class Snake {

    private int snakeSize;
    private int posX;
    private int posY;

    public Snake() {
        //this.snakeSize = 1;
        //Random rand = new Random();
        //this.posX = rand.nextInt(COLUMN) * FIELD_SIZE;
        //this.posY = rand.nextInt(ROW) * FIELD_SIZE;
    }

    public void setSnakeSize(int snakeSize) {
        this.snakeSize = snakeSize;
    }

    public Snake setPosX(int posX) {
        this.posX = posX;
        return this;
    }

    public Snake setPosY(int posY) {
        this.posY = posY;
        return this;
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

    public void addPosY(int addY) {
        this.posY += addY;
    }

    public void addPosX(int addX) {
        this.posX += addX;
    }
}
