open module gg.solarmc.authplugin.test {
    requires com.velocitypowered.api;
    requires gg.solarmc.authplugin;
    requires gg.solarmc.loader.authentication;
    requires net.bytebuddy; // Required by mockito
    requires net.bytebuddy.agent; // Required by mockito
    requires org.junit.jupiter.api;
    requires org.mockito;
    requires org.mockito.junit.jupiter;
    requires space.arim.api.util.testing;
    uses com.velocitypowered.api.proxy.player.AuthenticationProvider.ProviderFactory;
}