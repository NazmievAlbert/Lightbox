package com.example.sun.bluetoothlightbox;

public class Interfaces {

    public interface Observer {
        public void update(String status);
    }

    public interface Observable {
        void registerObserver(Observer o);

        void removeObserver(Observer o);

        void notifyObservers();
    }

}
