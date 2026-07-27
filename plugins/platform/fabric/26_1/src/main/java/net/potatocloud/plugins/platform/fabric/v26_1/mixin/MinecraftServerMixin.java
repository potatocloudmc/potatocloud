package net.potatocloud.plugins.platform.fabric.v26_1.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.protocol.status.ServerStatus;
import net.potatocloud.plugins.platform.fabric.v26_1.Fabric26_1Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void potatocloud$runServer(CallbackInfo ci) {
        Fabric26_1Plugin.instance().serverStarted((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void potatocloud$stopServer(CallbackInfo ci) {
        Fabric26_1Plugin.instance().serverStopped();
    }

    @Inject(method = "getStatus", at = @At("HEAD"))
    private void potatocloud$getStatus(CallbackInfoReturnable<ServerStatus> cir) {
        ((MinecraftServer) (Object) this).invalidateStatus();
    }
}
