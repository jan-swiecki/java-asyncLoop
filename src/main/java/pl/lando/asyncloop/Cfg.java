package pl.lando.asyncloop;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:Cfg.properties"})
public interface Cfg extends Config {
    @DefaultValue("10000")
    int interval();
}
