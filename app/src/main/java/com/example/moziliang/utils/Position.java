package com.example.moziliang.utils;

/**
 * Created by limkuan on 15/6/10.
 */
public class Position {
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position () {}

    private double x;

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    private double y;


}
