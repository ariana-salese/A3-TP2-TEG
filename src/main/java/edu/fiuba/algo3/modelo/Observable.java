package edu.fiuba.algo3.modelo;

import java.util.Observer;

public class Observable extends java.util.Observable {
    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);
        observer.update(this, null);
    }
}
