package com.andrei1058.bedwars.support.version.v1_21_R7;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.ShopHolo;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.VersionSupport;
import com.andrei1058.bedwars.support.version.common.VersionCommon;
import com.andrei1058.bedwars.support.version.v1_21_R7.despawnable.DespawnableAttributes;
import com.andrei1058.bedwars.support.version.v1_21_R7.despawnable.DespawnableFactory;
import com.andrei1058.bedwars.support.version.v1_21_R7.despawnable.DespawnableType;
import com.mojang.datafixers.util.Pair;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class v1_21_R7 extends VersionSupport {

    private final DespawnableFactory despawnableFactory;

    public v1_21_R7(Plugin plugin, String name) {
        super(plugin, name);
        loadDefaultEffects();
        this.despawnableFactory = new DespawnableFactory(this);
    }

    @Override
    public void registerVersionListeners() {
        new VersionCommon(this);
    }

    @Override
    public void registerCommand(String name, Command cmd) {
        ((CraftServer) getPlugin().getServer()).getCommandMap().register(name, cmd);
    }

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return pdc.has(createKey(key)) ? pdc.get(createKey(key), PersistentDataType.STRING) : null;
    }

    @Override
    public void sendTitle(@NotNull Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        p.sendTitle(title == null ? " " : title, subtitle == null ? " " : subtitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void spawnSilverfish(Location loc, ITeam bedWarsTeam, double speed, double health, int despawn, double damage) {
        var attr = new DespawnableAttributes(DespawnableType.SILVERFISH, speed, health, damage, despawn);
        var entity = despawnableFactory.spawn(attr, loc, bedWarsTeam);

        new com.andrei1058.bedwars.api.entity.Despawnable(
                entity,
                bedWarsTeam, despawn,
                Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME,
                PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL,
                PlayerKillEvent.PlayerKillCause.SILVERFISH
        );
    }

    @Override
    public void spawnIronGolem(Location loc, ITeam bedWarsTeam, double speed, double health, int despawn) {
        var attr = new DespawnableAttributes(DespawnableType.IRON_GOLEM, speed, health, 4, despawn);
        var entity = despawnableFactory.spawn(attr, loc, bedWarsTeam);
        new com.andrei1058.bedwars.api.entity.Despawnable(
                entity,
                bedWarsTeam, despawn,
                Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME,
                PlayerKillEvent.PlayerKillCause.IRON_GOLEM_FINAL_KILL,
                PlayerKillEvent.PlayerKillCause.IRON_GOLEM
        );
    }

    @Override
    public void playAction(@NotNull Player p, String text) {
        p.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.translateAlternateColorCodes('&', text))
        );
    }

    @Override
    public boolean isBukkitCommandRegistered(String name) {
        return ((CraftServer) getPlugin().getServer()).getCommandMap().getCommand(name) != null;
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(@NotNull Player p) {
        return p.getInventory().getItemInMainHand();
    }

    @Override
    public void hideEntity(@NotNull Entity e, Player p) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(e.getEntityId());
        this.sendPacket(p, packet);
    }

    @Override
    public void minusAmount(Player p, org.bukkit.inventory.@NotNull ItemStack i, int amount) {
        if (i.getAmount() - amount <= 0) {
            if (p.getInventory().getItemInOffHand().equals(i)) {
                p.getInventory().setItemInOffHand(null);
            } else {
                p.getInventory().removeItem(i);
            }
            return;
        }
        i.setAmount(i.getAmount() - amount);
        p.updateInventory();
    }

    @Override
    public void setSource(TNTPrimed tnt, Player owner) {
        LivingEntity nmsEntityLiving = ((CraftLivingEntity) owner).getHandle();
        PrimedTnt nmsTNT = ((CraftTNTPrimed) tnt).getHandle();
        try {
            Field sourceField = PrimedTnt.class.getDeclaredField("owner");
            sourceField.setAccessible(true);
            sourceField.set(nmsTNT, EntityReference.of(nmsEntityLiving));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isArmor(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        String name = itemStack.getType().name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                || name.equals("ELYTRA");
    }

    @Override
    public boolean isTool(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        String name = itemStack.getType().name();
        return name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }

    @Override
    public boolean isSword(org.bukkit.inventory.ItemStack itemStack) {
        return itemStack != null && itemStack.getType().name().endsWith("_SWORD");
    }

    @Override
    public boolean isAxe(org.bukkit.inventory.ItemStack itemStack) {
        return itemStack != null && itemStack.getType().name().endsWith("_AXE");
    }

    @Override
    public boolean isBow(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        Material m = itemStack.getType();
        return m == Material.BOW || m == Material.CROSSBOW;
    }

    @Override
    public boolean isProjectile(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) return false;
        switch (itemStack.getType()) {
            case SNOWBALL:
            case EGG:
            case FIRE_CHARGE:
            case ARROW:
            case TIPPED_ARROW:
            case SPECTRAL_ARROW:
            case TRIDENT:
            case ENDER_PEARL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isInvisibilityPotion(org.bukkit.inventory.@NotNull ItemStack itemStack) {
        if (!itemStack.getType().equals(org.bukkit.Material.POTION)) return false;
        org.bukkit.inventory.meta.PotionMeta pm = (org.bukkit.inventory.meta.PotionMeta) itemStack.getItemMeta();
        return pm != null && pm.hasCustomEffects() && pm.hasCustomEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
    }

    @Override
    public void registerEntities() {
    }

    @Override
    public void spawnShop(@NotNull Location loc, String name1, List<Player> players, IArena arena) {
        Location l = loc.clone();
        if (l.getWorld() == null) return;
        Villager vlg = (Villager) l.getWorld().spawnEntity(l, EntityType.VILLAGER);
        vlg.setAI(false);
        vlg.setRemoveWhenFarAway(false);
        vlg.setCollidable(false);
        vlg.setInvulnerable(true);
        vlg.setSilent(true);

        for (Player p : players) {
            String[] name = Language.getMsg(p, name1).split(",");
            if (name.length == 1) {
                ArmorStand a = createArmorStand(name[0], l.clone().add(0, 1.85, 0));
                new ShopHolo(Language.getPlayerLanguage(p).getIso(), a, null, l, arena);
            } else {
                ArmorStand a = createArmorStand(name[0], l.clone().add(0, 2.1, 0));
                ArmorStand b = createArmorStand(name[1], l.clone().add(0, 1.85, 0));
                new ShopHolo(Language.getPlayerLanguage(p).getIso(), a, b, l, arena);
            }
        }
        for (ShopHolo sh : ShopHolo.getShopHolo()) {
            if (sh.getA() == arena) {
                sh.update();
            }
        }
    }

    @Override
    public double getDamage(org.bukkit.inventory.ItemStack i) {
        var meta = i.getItemMeta();
        if (meta == null) return 0;
        var mods = meta.getAttributeModifiers(org.bukkit.inventory.EquipmentSlot.HAND);
        if (mods == null) return 0;
        return mods.stream()
                .filter(m -> m.getAttribute() == Attribute.GENERIC_ATTACK_DAMAGE)
                .mapToDouble(AttributeModifier::getAmount)
                .findFirst().orElse(0);
    }

    private static ArmorStand createArmorStand(String name, Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        ArmorStand a = loc.getWorld().spawn(loc, ArmorStand.class);
        a.setGravity(false);
        a.setVisible(false);
        a.setCustomNameVisible(true);
        a.setCustomName(name);
        return a;
    }

    @Override
    public void voidKill(Player p) {
        ServerPlayer player = getPlayer(p);
        player.hurt(player.damageSources().fellOutOfWorld(), 1000);
    }

    @Override
    public void hideArmor(@NotNull Player victim, Player receiver) {
        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();
        items.add(new Pair<>(EquipmentSlot.HEAD, ItemStack.EMPTY));
        items.add(new Pair<>(EquipmentSlot.CHEST, ItemStack.EMPTY));
        items.add(new Pair<>(EquipmentSlot.LEGS, ItemStack.EMPTY));
        items.add(new Pair<>(EquipmentSlot.FEET, ItemStack.EMPTY));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(victim.getEntityId(), items);
        sendPacket(receiver, packet);
    }

    @Override
    public void showArmor(@NotNull Player victim, Player receiver) {
        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();
        items.add(new Pair<>(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(victim.getInventory().getHelmet())));
        items.add(new Pair<>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(victim.getInventory().getChestplate())));
        items.add(new Pair<>(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(victim.getInventory().getLeggings())));
        items.add(new Pair<>(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(victim.getInventory().getBoots())));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(victim.getEntityId(), items);
        sendPacket(receiver, packet);
    }

    @Override
    public void spawnDragon(Location l, ITeam bwt) {
        if (l == null || l.getWorld() == null) {
            getPlugin().getLogger().log(Level.WARNING, "Could not spawn Dragon. Location is null");
            return;
        }
        org.bukkit.entity.EnderDragon ed = (org.bukkit.entity.EnderDragon) l.getWorld().spawnEntity(l, org.bukkit.entity.EntityType.ENDER_DRAGON);
        ed.setPhase(org.bukkit.entity.EnderDragon.Phase.CIRCLING);
    }

    @Override
    public void colorBed(ITeam bwt) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockState bed = bwt.getBed().clone().add(x, 0, z).getBlock().getState();
                if (bed instanceof Bed) {
                    bed.setType(bwt.getColor().bedMaterial());
                    bed.update();
                }
            }
        }
    }

    @Override
    public void registerTntWhitelist(float endStoneBlast, float glassBlast) {
        try {
            Field field = BlockBehaviour.Properties.class.getDeclaredField("explosionResistance");
            field.setAccessible(true);
            field.setFloat(Blocks.END_STONE.properties(), endStoneBlast);
            field.setFloat(Blocks.GLASS.properties(), glassBlast);

            Block[] coloredGlass = new Block[]{
                    Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS,
                    Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS,
                    Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS,
                    Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS,
                    Blocks.TINTED_GLASS,
            };

            for (Block glass : coloredGlass) {
                field.setFloat(glass.properties(), glassBlast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBlockTeamColor(@NotNull org.bukkit.block.Block block, TeamColor teamColor) {
        if (block.getType().toString().contains("STAINED_GLASS") || block.getType().toString().equals("GLASS")) {
            block.setType(teamColor.glassMaterial());
        } else if (block.getType().toString().contains("_TERRACOTTA")) {
            block.setType(teamColor.glazedTerracottaMaterial());
        } else if (block.getType().toString().contains("_WOOL")) {
            block.setType(teamColor.woolMaterial());
        }
    }

    @Override
    public void setCollide(@NotNull Player p, IArena a, boolean value) {
        p.setCollidable(value);
        if (a == null) return;
        a.updateSpectatorCollideRule(p, value);
    }

    @Override
    public org.bukkit.inventory.ItemStack addCustomData(org.bukkit.inventory.ItemStack i, String data) {
        var meta = i.getItemMeta();
        if (meta == null) return i;
        meta.getPersistentDataContainer().set(createKey(VersionSupport.PLUGIN_TAG_GENERIC_KEY), PersistentDataType.STRING, data);
        i.setItemMeta(meta);
        return i;
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        var meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;
        meta.getPersistentDataContainer().set(createKey(key), PersistentDataType.STRING, value);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public boolean isCustomBedWarsItem(org.bukkit.inventory.ItemStack i) {
        if (i == null || !i.hasItemMeta()) return false;
        return i.getItemMeta().getPersistentDataContainer().has(createKey(VersionSupport.PLUGIN_TAG_GENERIC_KEY));
    }

    @Override
    public String getCustomData(org.bukkit.inventory.ItemStack i) {
        if (i == null || !i.hasItemMeta()) return null;
        var pdc = i.getItemMeta().getPersistentDataContainer();
        return pdc.get(createKey(VersionSupport.PLUGIN_TAG_GENERIC_KEY), PersistentDataType.STRING);
    }

    @Override
    public org.bukkit.inventory.ItemStack colourItem(org.bukkit.inventory.ItemStack itemStack, ITeam bedWarsTeam) {
        if (itemStack == null) return null;
        String type = itemStack.getType().toString();
        if (isBed(itemStack.getType())) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().bedMaterial(), itemStack.getAmount());
        } else if (type.contains("_STAINED_GLASS_PANE")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glassPaneMaterial(), itemStack.getAmount());
        } else if (type.contains("STAINED_GLASS") || type.equals("GLASS")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glassMaterial(), itemStack.getAmount());
        } else if (type.contains("_TERRACOTTA")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().glazedTerracottaMaterial(), itemStack.getAmount());
        } else if (type.contains("_WOOL")) {
            return new org.bukkit.inventory.ItemStack(bedWarsTeam.getColor().woolMaterial(), itemStack.getAmount());
        }
        return itemStack;
    }

    @Override
    public org.bukkit.inventory.ItemStack createItemStack(String material, int amount, short data) {
        org.bukkit.inventory.ItemStack i;
        try {
            i = new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(material), amount);
        } catch (Exception ex) {
            getPlugin().getLogger().log(Level.WARNING, material + " is not a valid " + getName() + " material!");
            i = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BEDROCK);
        }
        return i;
    }

    @Override
    public org.bukkit.Material materialFireball() {
        return org.bukkit.Material.FIRE_CHARGE;
    }

    @Override
    public org.bukkit.Material materialPlayerHead() {
        return org.bukkit.Material.PLAYER_HEAD;
    }

    @Override
    public org.bukkit.Material materialSnowball() {
        return org.bukkit.Material.SNOWBALL;
    }

    @Override
    public org.bukkit.Material materialGoldenHelmet() {
        return org.bukkit.Material.GOLDEN_HELMET;
    }

    @Override
    public org.bukkit.Material materialGoldenChestPlate() {
        return org.bukkit.Material.GOLDEN_CHESTPLATE;
    }

    @Override
    public org.bukkit.Material materialGoldenLeggings() {
        return org.bukkit.Material.GOLDEN_LEGGINGS;
    }

    @Override
    public org.bukkit.Material materialNetheriteHelmet() {
        return Material.NETHERITE_HELMET;
    }

    @Override
    public org.bukkit.Material materialNetheriteChestPlate() {
        return Material.NETHERITE_CHESTPLATE;
    }

    @Override
    public org.bukkit.Material materialNetheriteLeggings() {
        return Material.NETHERITE_LEGGINGS;
    }

    @Override
    public org.bukkit.Material materialElytra() {
        return Material.ELYTRA;
    }

    @Override
    public org.bukkit.Material materialCake() {
        return org.bukkit.Material.CAKE;
    }

    @Override
    public org.bukkit.Material materialCraftingTable() {
        return org.bukkit.Material.CRAFTING_TABLE;
    }

    @Override
    public org.bukkit.Material materialEnchantingTable() {
        return org.bukkit.Material.ENCHANTING_TABLE;
    }

    @Override
    public org.bukkit.Material woolMaterial() {
        return org.bukkit.Material.WHITE_WOOL;
    }

    @Override
    public String getShopUpgradeIdentifier(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return "null";
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return pdc.has(createKey(VersionSupport.PLUGIN_TAG_TIER_KEY))
                ? pdc.get(createKey(VersionSupport.PLUGIN_TAG_TIER_KEY), PersistentDataType.STRING) : "null";
    }

    @Override
    public org.bukkit.inventory.ItemStack setShopUpgradeIdentifier(org.bukkit.inventory.ItemStack itemStack, String identifier) {
        return setTag(itemStack, VersionSupport.PLUGIN_TAG_TIER_KEY, identifier);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerHead(Player player, org.bukkit.inventory.ItemStack copyTagFrom) {
        org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(materialPlayerHead());

        var meta = head.getItemMeta();
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwnerProfile(player.getPlayerProfile());
        }
        head.setItemMeta(meta);
        return head;
    }

    @Override
    public void sendPlayerSpawnPackets(Player respawned, IArena arena) {
        if (respawned == null || arena == null) return;
        if (!arena.isPlayer(respawned)) return;
        if (arena.getRespawnSessions().containsKey(respawned)) return;

        ServerPlayer entityPlayer = getPlayer(respawned);
        ClientboundAddEntityPacket show = addEntityPacket(entityPlayer);
        ClientboundSetEntityMotionPacket playerVelocity = new ClientboundSetEntityMotionPacket(entityPlayer);
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(entityPlayer, getCompressedAngle(entityPlayer.getBukkitYaw()));

        List<Pair<EquipmentSlot, ItemStack>> list = getPlayerEquipment(entityPlayer);

        for (Player p : arena.getPlayers()) {
            if (p == null || p.equals(respawned)) continue;
            if (arena.getRespawnSessions().containsKey(p)) continue;

            ServerPlayer boundTo = getPlayer(p);
            if (p.getWorld().equals(respawned.getWorld())
                    && respawned.getLocation().distance(p.getLocation()) <= arena.getRenderDistance()) {

                this.sendPackets(
                        p, show, head, playerVelocity,
                        new ClientboundSetEquipmentPacket(respawned.getEntityId(), list)
                );

                if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    hideArmor(p, respawned);
                } else {
                    ClientboundAddEntityPacket show2 = addEntityPacket(boundTo);
                    ClientboundSetEntityMotionPacket playerVelocity2 = new ClientboundSetEntityMotionPacket(boundTo);
                    ClientboundRotateHeadPacket head2 = new ClientboundRotateHeadPacket(boundTo, getCompressedAngle(boundTo.getBukkitYaw()));
                    this.sendPackets(respawned, show2, playerVelocity2, head2);
                    showArmor(p, respawned);
                }
            }
        }

        for (Player spectator : arena.getSpectators()) {
            if (spectator == null || spectator.equals(respawned)) continue;
            respawned.hidePlayer(getPlugin(), spectator);
            if (spectator.getWorld().equals(respawned.getWorld())
                    && respawned.getLocation().distance(spectator.getLocation()) <= arena.getRenderDistance()) {
                this.sendPackets(
                        spectator, show, playerVelocity,
                        new ClientboundSetEquipmentPacket(respawned.getEntityId(), list),
                        new ClientboundRotateHeadPacket(entityPlayer, getCompressedAngle(entityPlayer.getBukkitYaw()))
                );
            }
        }
    }

    @Override
    public String getInventoryName(@NotNull InventoryEvent e) {
        return e.getView().getTitle();
    }

    @Override
    public void setUnbreakable(@NotNull ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
    }

    @Override
    public String getMainLevel() {
        return MinecraftServer.getServer().getWorldData().getLevelName();
    }

    @Override
    public int getVersion() {
        return 10;
    }

    @Override
    public void setJoinSignBackground(@NotNull BlockState b, org.bukkit.Material material) {
        if (b.getBlockData() instanceof WallSign) {
            b.getBlock().getRelative(((WallSign) b.getBlockData()).getFacing().getOppositeFace()).setType(material);
        }
    }

    @Override
    public void spigotShowPlayer(Player victim, @NotNull Player receiver) {
        receiver.showPlayer(getPlugin(), victim);
    }

    @Override
    public void spigotHidePlayer(Player victim, @NotNull Player receiver) {
        receiver.hidePlayer(getPlugin(), victim);
    }

    @Override
    public org.bukkit.entity.Fireball setFireballDirection(org.bukkit.entity.Fireball fireball, @NotNull Vector vector) {
        fireball.setDirection(vector);
        return fireball;
    }

    @Override
    public void playRedStoneDot(@NotNull Player player) {
        var particle = new DustParticleOptions(org.bukkit.Color.RED.asRGB(), 1.0f);
        ClientboundLevelParticlesPacket particlePacket = new ClientboundLevelParticlesPacket(
                particle, true, false,
                player.getLocation().getX(),
                player.getLocation().getY() + 2.6,
                player.getLocation().getZ(),
                0, 0, 0, 0, 1
        );
        for (Player inWorld : player.getWorld().getPlayers()) {
            if (inWorld.equals(player)) continue;
            this.sendPacket(inWorld, particlePacket);
        }
    }

    @Override
    public void clearArrowsFromPlayerBody(Player player) {
        // minecraft clears them on death on newer versions
    }

    @Override
    public void placeTowerBlocks(@NotNull org.bukkit.block.Block b, @NotNull IArena a, @NotNull TeamColor color, int x, int y, int z) {
        b.getRelative(x, y, z).setType(color.woolMaterial());
        a.addPlacedBlock(b.getRelative(x, y, z));
    }

    @Override
    public void placeLadder(@NotNull org.bukkit.block.Block b, int x, int y, int z, @NotNull IArena a, int ladderData) {
        org.bukkit.block.Block block = b.getRelative(x, y, z);
        block.setType(Material.LADDER);
        Ladder ladder = (Ladder) block.getBlockData();
        a.addPlacedBlock(block);
        switch (ladderData) {
            case 2 -> {
                ladder.setFacing(BlockFace.NORTH);
                block.setBlockData(ladder);
            }
            case 3 -> {
                ladder.setFacing(BlockFace.SOUTH);
                block.setBlockData(ladder);
            }
            case 4 -> {
                ladder.setFacing(BlockFace.WEST);
                block.setBlockData(ladder);
            }
            case 5 -> {
                ladder.setFacing(BlockFace.EAST);
                block.setBlockData(ladder);
            }
        }
    }

    @Override
    public void playVillagerEffect(@NotNull Player player, Location location) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, location, 1);
    }

    public ServerPlayer getPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public List<Pair<EquipmentSlot, ItemStack>> getPlayerEquipment(@NotNull ServerPlayer entityPlayer) {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
        list.add(new Pair<>(EquipmentSlot.MAINHAND, entityPlayer.getItemBySlot(EquipmentSlot.MAINHAND)));
        list.add(new Pair<>(EquipmentSlot.OFFHAND, entityPlayer.getItemBySlot(EquipmentSlot.OFFHAND)));
        list.add(new Pair<>(EquipmentSlot.HEAD, entityPlayer.getItemBySlot(EquipmentSlot.HEAD)));
        list.add(new Pair<>(EquipmentSlot.CHEST, entityPlayer.getItemBySlot(EquipmentSlot.CHEST)));
        list.add(new Pair<>(EquipmentSlot.LEGS, entityPlayer.getItemBySlot(EquipmentSlot.LEGS)));
        list.add(new Pair<>(EquipmentSlot.FEET, entityPlayer.getItemBySlot(EquipmentSlot.FEET)));
        return list;
    }

    private ClientboundAddEntityPacket addEntityPacket(ServerPlayer p) {
        return new ClientboundAddEntityPacket(
                p.getId(), p.getUUID(), p.getX(), p.getY(), p.getZ(),
                p.getXRot(), p.getYRot(), EntityType.PLAYER, 0, Vec3.ZERO, 0.0D);
    }

    private NamespacedKey createKey(String key) {
        return new NamespacedKey(getPlugin(), key);
    }

    private void sendPacket(Player player, Packet<?> packet) {
        getConnection(player).send(packet);
    }

    private void sendPackets(Player player, Packet<?> @NotNull ... packets) {
        ServerGamePacketListenerImpl connection = getConnection(player);
        for (Packet<?> p : packets) {
            connection.send(p);
        }
    }

    private ServerGamePacketListenerImpl getConnection(Player player) {
        return ((CraftPlayer) player).getHandle().connection;
    }
}