package dev.sbs.minecraftapi.client.mojang.request;

import dev.sbs.api.client.route.DynamicRoute;
import dev.sbs.minecraftapi.client.mojang.MojangClient;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@DynamicRoute
public @interface MojangDomain {

    @NotNull MojangClient.Domain value();

}