package de.uniks.stp.controller.snake.model;

public class Game {
    private Direction direction;
    private int score;
    private int highScore;

    public Game(int score, int highScore) {
        this.score = score;
        this.highScore = highScore;
        this.direction = Direction.RIGHT; // TODO
    }

    public Direction getCurrentDirection() {
        return direction;
    }

    public void setCurrentDirection(Direction direction) {
        this.direction = direction;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public enum Direction {
        RIGHT,
        LEFT,
        UP,
        DOWN
    }
}
