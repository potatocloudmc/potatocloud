package net.potatocloud.plugins.platform.fabric.v26_1.mixin;

import net.minecraft.server.MinecraftServer;
import net.potatocloud.plugins.platform.fabric.v26_1.Fabric26_1Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;",
                    ordinal = 0
            )
    )
    private void onServerStarted(CallbackInfo info) {
        Fabric26_1Plugin.instance().serverStarted((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void onServerStopped(CallbackInfo info) {
        Fabric26_1Plugin.instance().serverStopped();
    }
}
