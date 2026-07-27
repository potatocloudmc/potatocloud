package net.potatocloud.plugins.platform.neoforge.v26_1.mixin;

import net.minecraft.server.players.PlayerList;
import net.potatocloud.plugins.platform.neoforge.v26_1.NeoForge26_1Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "getMaxPlayers", at = @At("HEAD"), cancellable = true)
    private void potatocloud$getMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        final NeoForge26_1Plugin plugin = NeoForge26_1Plugin.instance();
        if (plugin == null) {
            return;
        }

        final Integer maxPlayers = plugin.maxPlayers();
        if (maxPlayers != null) {
            cir.setReturnValue(maxPlayers);
        }
    }
}
