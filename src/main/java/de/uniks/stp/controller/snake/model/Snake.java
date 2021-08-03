package de.uniks.stp.controller.snake.model;

public class Snake {

    private int posX;
    private int posY;

    public Snake() {
    }

    public int getPosX() {
        return this.posX;
    }

    public Snake setPosX(int posX) {
        this.posX = posX;
        return this;
    }

    public int getPosY() {
        return this.posY;
    }

    public Snake setPosY(int posY) {
        this.posY = posY;
        return this;
    }

    public void addPosY(int addY) {
        this.posY += addY;
    }

    public void addPosX(int addX) {
        this.posX += addX;
    }
}
