package net.potatocloud.network.security;

public interface SecurityProvider<T> {

    T createServerContext();

    T createClientContext();

    void generate(String name);

}
