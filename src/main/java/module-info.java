import com.velocitypowered.api.plugin.LaunchablePlugin;
import com.velocitypowered.api.proxy.player.AuthenticationProvider;
import gg.solarmc.authplugin.AuthPlugin;
import gg.solarmc.authplugin.AuthProviderFactory;

module gg.solarmc.authplugin {
    requires com.velocitypowered.api;
    requires gg.solarmc.command;
    requires gg.solarmc.loader.authentication;
    requires jakarta.inject;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;
    requires space.arim.api.util.dazzleconf;
    requires space.arim.api.util.web;
    requires space.arim.dazzleconf;
    requires space.arim.dazzleconf.ext.snakeyaml;
    requires space.arim.injector;
    requires space.arim.omnibus;

    exports gg.solarmc.authplugin to space.arim.injector, gg.solarmc.authplugin.test;
    exports gg.solarmc.authplugin.auth to space.arim.injector, gg.solarmc.authplugin.test;
    exports gg.solarmc.authplugin.auth.handler to space.arim.injector, gg.solarmc.authplugin.test;
    exports gg.solarmc.authplugin.command to space.arim.injector, gg.solarmc.authplugin.test;
    exports gg.solarmc.authplugin.config to space.arim.injector, gg.solarmc.authplugin.test;
    exports gg.solarmc.authplugin.listener to space.arim.injector, gg.solarmc.authplugin.test;
    opens gg.solarmc.authplugin to com.velocitypowered.api;
    opens gg.solarmc.authplugin.config to space.arim.dazzleconf;
    opens gg.solarmc.authplugin.listener to com.velocitypowered.api;

    provides LaunchablePlugin with AuthPlugin;
    provides AuthenticationProvider.ProviderFactory with AuthProviderFactory;
}