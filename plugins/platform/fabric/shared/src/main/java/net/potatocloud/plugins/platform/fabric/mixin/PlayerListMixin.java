package net.potatocloud.plugins.platform.fabric.mixin;

import net.minecraft.server.players.PlayerList;
import net.potatocloud.plugins.platform.fabric.FabricPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "getMaxPlayers", at = @At("HEAD"), cancellable = true)
    private void potatocloud$getMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        final FabricPlugin plugin = FabricPlugin.instance();
        if (plugin == null) {
            return;
        }

        plugin.connector().currentService().ifPresent(service -> cir.setReturnValue(service.maxPlayers()));
    }
}
