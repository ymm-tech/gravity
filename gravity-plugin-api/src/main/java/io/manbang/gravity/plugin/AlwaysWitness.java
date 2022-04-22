package io.manbang.gravity.plugin;

enum AlwaysWitness implements Witness {
    INSTANCE;

    @Override
    public boolean saw(ClassLoader classLoader) {
        return true;
    }
}
