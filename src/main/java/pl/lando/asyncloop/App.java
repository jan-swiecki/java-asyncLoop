package pl.lando.asyncloop;

import org.aeonbits.owner.ConfigFactory;

public class App {

    public static void main(String[] args) {
        initializeConfig();
    }

    public static void initializeConfig() {
        Cfg cfg = ConfigFactory.create(Cfg.class);
        System.out.println("Interval = " + cfg.interval());
    }

}
