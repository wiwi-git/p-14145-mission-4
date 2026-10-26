package org.example;

import java.io.*;
import java.util.*;

public class Main {

    static void main(String[] args) {
        EnvLoader.load();
        App app = new App(new Scanner(System.in));
        app.run(true);
    }
}
