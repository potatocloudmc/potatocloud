package net.potatocloud.plugin.platform.spigot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CaptainHook {

	private List<ICloudHook> hooks = new ArrayList<ICloudHook>();

	public void registerHook(ICloudHook hook) {
		if (!this.hooks.contains(hook)) {
			this.hooks.add(hook);
		}
	}
	
	public void unregisterHook(ICloudHook hook) {
		if (this.hooks.contains(hook)) {
			this.hooks.remove(hook);
		}
	}

	public List<ICloudHook> getHooks() {
		return Collections.unmodifiableList(hooks);
	}
}