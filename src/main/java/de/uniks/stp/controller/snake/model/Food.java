package de.uniks.stp.controller.snake.model;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import util.ResourceManager;

import java.util.ArrayList;
import java.util.Random;

import static de.uniks.stp.controller.snake.Constants.*;

public class Food {
    private int foodSize;
    private int posX;
    private int posY;
    private Image foodPic;
    private String[] foodList = {"apple", "berry", "cherry", "orange"};

    public Food() {
        this.foodSize = FIELD_SIZE;
        Random rand = new Random();
        this.posX = rand.nextInt(COLUMN) * FIELD_SIZE;
        this.posY = rand.nextInt(ROW) * FIELD_SIZE;
        foodPic = ResourceManager.loadSnakeGameIcon(foodList[rand.nextInt(4)]);
    }

    public Image getFoodPic() {
        return foodPic;
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