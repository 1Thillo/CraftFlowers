package cm.ptks.craftflowers.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder setDisplayName(String displayName) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setTextureId(String textureId) {
        if(textureId.isEmpty())
            return this;

        SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();

        try {
            PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID(), "null");
            PlayerTextures textures = playerProfile.getTextures();
            textures.setSkin(URI.create("https://textures.minecraft.net/texture/" + textureId).toURL());
            playerProfile.setTextures(textures);
            headMeta.setOwnerProfile(playerProfile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        itemStack.setItemMeta(headMeta);

        return this;
    }

    public ItemStack build() {
        return itemStack;
    }

}
