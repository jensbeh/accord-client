package de.uniks.stp.controller.snake.model;


import javafx.scene.image.Image;
import util.ResourceManager;

import java.util.Random;

public class Food {
    private int posX;
    private int posY;
    private final Image foodPic;

    public Food() {
        Random rand = new Random();
        String[] foodList = {"apple", "berry", "cherry", "orange"};
        foodPic = ResourceManager.loadSnakeGameIcon(foodList[rand.nextInt(4)]);
    }

    public Image getFoodPic() {
        return foodPic;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public Food setPosX(int posX) {
        this.posX = posX;
        return this;
    }

    public Food setPosY(int posY) {
        this.posY = posY;
        return this;
    }
}
