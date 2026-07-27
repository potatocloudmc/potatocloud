package net.potatocloud.plugins.platform.neoforge.v1_21_11.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "getStatus", at = @At("HEAD"))
    private void potatocloud$getStatus(CallbackInfoReturnable<ServerStatus> cir) {
        ((MinecraftServer) (Object) this).invalidateStatus();
    }
}
