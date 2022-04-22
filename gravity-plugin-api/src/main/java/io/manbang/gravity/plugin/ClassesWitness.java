package io.manbang.gravity.plugin;

import lombok.extern.java.Log;

import java.net.URL;
import java.util.stream.Stream;

@Log
class ClassesWitness implements Witness {
    private final String[] classNames;

    ClassesWitness(String... classNames) {
        this.classNames = Stream.of(classNames)
                .map(n -> n.replace(".", "/"))
                .map(n -> n.concat(".class"))
                .toArray(String[]::new);
    }

    @Override
    public boolean saw(ClassLoader classLoader) {
        for (String name : classNames) {
            URL resource = classLoader.getResource(name);
            if (resource == null) {
                return false;
            }
        }

        return true;
    }
}
