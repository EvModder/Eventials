package EvLib;

import org.bukkit.Material;

public class TypeUtils{
	public static boolean isSpawnEgg(Material mat){
		switch(mat){
			case BAT_SPAWN_EGG:
			case BLAZE_SPAWN_EGG:
			case CAVE_SPIDER_SPAWN_EGG:
			case CHICKEN_SPAWN_EGG:
			case COD_SPAWN_EGG:
			case COW_SPAWN_EGG:
			case CREEPER_SPAWN_EGG:
			case DOLPHIN_SPAWN_EGG:
			case DONKEY_SPAWN_EGG:
			case DROWNED_SPAWN_EGG:
			case ELDER_GUARDIAN_SPAWN_EGG:
			case ENDERMAN_SPAWN_EGG:
			case ENDERMITE_SPAWN_EGG:
			case EVOKER_SPAWN_EGG:
			case GHAST_SPAWN_EGG:
			case GUARDIAN_SPAWN_EGG:
			case HORSE_SPAWN_EGG:
			case HUSK_SPAWN_EGG:
			case LLAMA_SPAWN_EGG:
			case MAGMA_CUBE_SPAWN_EGG:
			case MOOSHROOM_SPAWN_EGG:
			case MULE_SPAWN_EGG:
			case OCELOT_SPAWN_EGG:
			case PARROT_SPAWN_EGG:
			case PHANTOM_SPAWN_EGG:
			case PIG_SPAWN_EGG:
			case POLAR_BEAR_SPAWN_EGG:
			case PUFFERFISH_SPAWN_EGG:
			case RABBIT_SPAWN_EGG:
			case SALMON_SPAWN_EGG:
			case SHEEP_SPAWN_EGG:
			case SHULKER_SPAWN_EGG:
			case SILVERFISH_SPAWN_EGG:
			case SKELETON_HORSE_SPAWN_EGG:
			case SKELETON_SPAWN_EGG:
			case SLIME_SPAWN_EGG:
			case SPIDER_SPAWN_EGG:
			case SQUID_SPAWN_EGG:
			case STRAY_SPAWN_EGG:
			case TROPICAL_FISH_SPAWN_EGG:
			case TURTLE_SPAWN_EGG:
			case VEX_SPAWN_EGG:
			case VILLAGER_SPAWN_EGG:
			case VINDICATOR_SPAWN_EGG:
			case WITCH_SPAWN_EGG:
			case WITHER_SKELETON_SPAWN_EGG:
			case WOLF_SPAWN_EGG:
			case ZOMBIE_HORSE_SPAWN_EGG:
			case ZOMBIE_PIGMAN_SPAWN_EGG:
			case ZOMBIE_SPAWN_EGG:
			case ZOMBIE_VILLAGER_SPAWN_EGG:
				return true;
			default:
				return false;
		}
	}

	public static boolean isOre(Material mat){
		switch(mat){
			case NETHER_QUARTZ_ORE:
			case COAL_ORE:
			case IRON_ORE:
			case GOLD_ORE:
			case REDSTONE_ORE:
			case LAPIS_ORE:
			case EMERALD_ORE:
			case DIAMOND_ORE:
				return true;
			default:
				return false;
		}
	}

	public static boolean isInfested(Material mat){
		switch(mat){
			case INFESTED_CHISELED_STONE_BRICKS:
			case INFESTED_COBBLESTONE:
			case INFESTED_CRACKED_STONE_BRICKS:
			case INFESTED_MOSSY_STONE_BRICKS:
			case INFESTED_STONE:
			case INFESTED_STONE_BRICKS:
				return true;
			default:
				return false;
		}
	}

	public static boolean isShulkerBox(Material mat){
		switch(mat){
			case BLACK_SHULKER_BOX:
			case BLUE_SHULKER_BOX:
			case BROWN_SHULKER_BOX:
			case CYAN_SHULKER_BOX:
			case GRAY_SHULKER_BOX:
			case GREEN_SHULKER_BOX:
			case LIGHT_BLUE_SHULKER_BOX:
			case LIME_SHULKER_BOX:
			case MAGENTA_SHULKER_BOX:
			case ORANGE_SHULKER_BOX:
			case PINK_SHULKER_BOX:
			case PURPLE_SHULKER_BOX:
			case RED_SHULKER_BOX:
			case LIGHT_GRAY_SHULKER_BOX:
			case WHITE_SHULKER_BOX:
			case YELLOW_SHULKER_BOX:
				return true;
			default:
				return false;
		}
	}

	public static boolean isConcrete(Material mat){
		switch(mat){
			case BLACK_CONCRETE:
			case BLUE_CONCRETE:
			case BROWN_CONCRETE:
			case CYAN_CONCRETE:
			case GRAY_CONCRETE:
			case GREEN_CONCRETE:
			case LIGHT_BLUE_CONCRETE:
			case LIGHT_GRAY_CONCRETE:
			case LIME_CONCRETE:
			case MAGENTA_CONCRETE:
			case ORANGE_CONCRETE:
			case PINK_CONCRETE:
			case PURPLE_CONCRETE:
			case RED_CONCRETE:
			case WHITE_CONCRETE:
			case YELLOW_CONCRETE:
				return true;
			default:
				return false;
		}
	}

	public static boolean isConcretePowder(Material mat){
		switch(mat){
			case BLACK_CONCRETE_POWDER:
			case BLUE_CONCRETE_POWDER:
			case BROWN_CONCRETE_POWDER:
			case CYAN_CONCRETE_POWDER:
			case GRAY_CONCRETE_POWDER:
			case GREEN_CONCRETE_POWDER:
			case LIGHT_BLUE_CONCRETE_POWDER:
			case LIGHT_GRAY_CONCRETE_POWDER:
			case LIME_CONCRETE_POWDER:
			case MAGENTA_CONCRETE_POWDER:
			case ORANGE_CONCRETE_POWDER:
			case PINK_CONCRETE_POWDER:
			case PURPLE_CONCRETE_POWDER:
			case RED_CONCRETE_POWDER:
			case WHITE_CONCRETE_POWDER:
			case YELLOW_CONCRETE_POWDER:
				return true;
			default:
				return false;
		}
	}

	public static boolean isStainedGlass(Material mat){
		switch(mat){
			case BLACK_STAINED_GLASS:
			case BLUE_STAINED_GLASS:
			case BROWN_STAINED_GLASS:
			case CYAN_STAINED_GLASS:
			case GRAY_STAINED_GLASS:
			case GREEN_STAINED_GLASS:
			case LIGHT_BLUE_STAINED_GLASS:
			case LIGHT_GRAY_STAINED_GLASS:
			case LIME_STAINED_GLASS:
			case MAGENTA_STAINED_GLASS:
			case ORANGE_STAINED_GLASS:
			case PINK_STAINED_GLASS:
			case PURPLE_STAINED_GLASS:
			case RED_STAINED_GLASS:
			case WHITE_STAINED_GLASS:
			case YELLOW_STAINED_GLASS:
				return true;
			default:
				return false;
		}
	}

	public static boolean isTerracotta(Material mat){
		switch(mat){
			case TERRACOTTA:
			case BLACK_TERRACOTTA:
			case BLUE_TERRACOTTA:
			case BROWN_TERRACOTTA:
			case CYAN_TERRACOTTA:
			case GRAY_TERRACOTTA:
			case GREEN_TERRACOTTA:
			case LIGHT_BLUE_TERRACOTTA:
			case LIGHT_GRAY_TERRACOTTA:
			case LIME_TERRACOTTA:
			case MAGENTA_TERRACOTTA:
			case ORANGE_TERRACOTTA:
			case PINK_TERRACOTTA:
			case PURPLE_TERRACOTTA:
			case RED_TERRACOTTA:
			case WHITE_TERRACOTTA:
			case YELLOW_TERRACOTTA:
				return true;
			default:
				return false;
		}
	}

	public static boolean isGlazedTerracotta(Material mat){
		switch(mat){
			case BLACK_GLAZED_TERRACOTTA:
			case BLUE_GLAZED_TERRACOTTA:
			case BROWN_GLAZED_TERRACOTTA:
			case CYAN_GLAZED_TERRACOTTA:
			case GRAY_GLAZED_TERRACOTTA:
			case GREEN_GLAZED_TERRACOTTA:
			case LIGHT_BLUE_GLAZED_TERRACOTTA:
			case LIGHT_GRAY_GLAZED_TERRACOTTA:
			case LIME_GLAZED_TERRACOTTA:
			case MAGENTA_GLAZED_TERRACOTTA:
			case ORANGE_GLAZED_TERRACOTTA:
			case PINK_GLAZED_TERRACOTTA:
			case PURPLE_GLAZED_TERRACOTTA:
			case RED_GLAZED_TERRACOTTA:
			case WHITE_GLAZED_TERRACOTTA:
			case YELLOW_GLAZED_TERRACOTTA:
				return true;
			default:
				return false;
		}
	}

	public static boolean isFlowerPot(Material mat){
		switch(mat){
			case FLOWER_POT:
			case POTTED_ACACIA_SAPLING:
			case POTTED_ALLIUM:
			case POTTED_AZURE_BLUET:
			case POTTED_BIRCH_SAPLING:
			case POTTED_BLUE_ORCHID:
			case POTTED_BROWN_MUSHROOM:
			case POTTED_CACTUS:
			case POTTED_DANDELION:
			case POTTED_DARK_OAK_SAPLING:
			case POTTED_DEAD_BUSH:
			case POTTED_FERN:
			case POTTED_JUNGLE_SAPLING:
			case POTTED_OAK_SAPLING:
			case POTTED_ORANGE_TULIP:
			case POTTED_OXEYE_DAISY:
			case POTTED_PINK_TULIP:
			case POTTED_POPPY:
			case POTTED_RED_MUSHROOM:
			case POTTED_RED_TULIP:
			case POTTED_SPRUCE_SAPLING:
			case POTTED_WHITE_TULIP:
				return true;
			default:
				return false;
		}
	}

	public static boolean isCarpet(Material mat){
		switch(mat){
			case BLACK_CARPET:
			case BLUE_CARPET:
			case BROWN_CARPET:
			case CYAN_CARPET:
			case GRAY_CARPET:
			case GREEN_CARPET:
			case LIGHT_BLUE_CARPET:
			case LIGHT_GRAY_CARPET:
			case LIME_CARPET:
			case MAGENTA_CARPET:
			case ORANGE_CARPET:
			case PINK_CARPET:
			case PURPLE_CARPET:
			case RED_CARPET:
			case WHITE_CARPET:
			case YELLOW_CARPET:
				return true;
			default:
				return false;
		}
	}

	public static boolean isBanner(Material mat){
		switch(mat){
			case BLACK_BANNER:
			case BLUE_BANNER:
			case BROWN_BANNER:
			case CYAN_BANNER:
			case GRAY_BANNER:
			case GREEN_BANNER:
			case LIGHT_BLUE_BANNER:
			case LIGHT_GRAY_BANNER:
			case LIME_BANNER:
			case MAGENTA_BANNER:
			case ORANGE_BANNER:
			case PINK_BANNER:
			case PURPLE_BANNER:
			case RED_BANNER:
			case WHITE_BANNER:
			case YELLOW_BANNER:
				return true;
			default:
				return false;
		}
	}

	public static boolean isWallBanner(Material mat){
		switch(mat){
			case BLACK_WALL_BANNER:
			case BLUE_WALL_BANNER:
			case BROWN_WALL_BANNER:
			case CYAN_WALL_BANNER:
			case GRAY_WALL_BANNER:
			case GREEN_WALL_BANNER:
			case LIGHT_BLUE_WALL_BANNER:
			case LIGHT_GRAY_WALL_BANNER:
			case LIME_WALL_BANNER:
			case MAGENTA_WALL_BANNER:
			case ORANGE_WALL_BANNER:
			case PINK_WALL_BANNER:
			case PURPLE_WALL_BANNER:
			case RED_WALL_BANNER:
			case WHITE_WALL_BANNER:
			case YELLOW_WALL_BANNER:
				return true;
			default:
				return false;
		}
	}

	public static boolean isDoublePlant(Material mat){
		switch(mat){
			case SUNFLOWER:
			case LILAC:
			case ROSE_BUSH:
			case PEONY:
			case TALL_GRASS:
			case LARGE_FERN:
				return true;
			default:
				return false;
		}
	}

	public static boolean isRail(Material mat){
		switch(mat){
			case RAIL:
			case ACTIVATOR_RAIL:
			case DETECTOR_RAIL:
			case POWERED_RAIL:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSapling(Material mat){
		switch(mat){
			case ACACIA_SAPLING:
			case BIRCH_SAPLING:
			case DARK_OAK_SAPLING: 
			case JUNGLE_SAPLING:
			case OAK_SAPLING:
			case SPRUCE_SAPLING:
				return true;
			default:
				return false;
		}
	}

	public static boolean isButton(Material mat){
		switch(mat){
			case ACACIA_BUTTON:
			case BIRCH_BUTTON:
			case DARK_OAK_BUTTON:
			case JUNGLE_BUTTON:
			case OAK_BUTTON:
			case SPRUCE_BUTTON:
			case STONE_BUTTON:
				return true;
			default:
				return false;
		}
	}

	public static boolean isPressurePlate(Material mat){
		switch(mat){
			case ACACIA_PRESSURE_PLATE:
			case BIRCH_PRESSURE_PLATE:
			case DARK_OAK_PRESSURE_PLATE:
			case JUNGLE_PRESSURE_PLATE:
			case OAK_PRESSURE_PLATE:
			case SPRUCE_PRESSURE_PLATE:
			case STONE_PRESSURE_PLATE:
			case HEAVY_WEIGHTED_PRESSURE_PLATE:
			case LIGHT_WEIGHTED_PRESSURE_PLATE:
				return true;
			default:
				return false;
		}
	}

	public static boolean isDoor(Material mat){
		switch(mat){
			case ACACIA_DOOR:
			case BIRCH_DOOR:
			case DARK_OAK_DOOR:
			case JUNGLE_DOOR:
			case OAK_DOOR:
			case SPRUCE_DOOR:
				return true;
			default:
				return false;
		}
	}

	// Broken by water
	public static boolean isFragile(Material mat){
		switch(mat){
//			case WATER:
//			case STATIONARY_WATER:
//			case LAVA:
//			case STATIONARY_LAVA:
			case GRASS:
			case DEAD_BUSH:
			case DANDELION:
			case POPPY:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case FIRE:
			case REDSTONE_WIRE:
			case WHEAT:
			case CARROTS:
			case POTATOES:
			case BEETROOTS:
			case MELON_STEM:
			case PUMPKIN_STEM:
			case LADDER:
			case WALL_SIGN:
			case SIGN:
			case LEVER:
			case REDSTONE_TORCH:
			case TORCH:
			case WALL_TORCH:
			case SNOW:
			case CACTUS:
			case SUGAR_CANE:
			case CAKE:
			case REPEATER:
			case COMPARATOR:
			case LILY_PAD:
			case NETHER_WART:
			case CARROT:
			case POTATO:
			case CHORUS_PLANT:
			case CHORUS_FLOWER:
				return true;
			default:
				if(isCarpet(mat)/* || isBanner(mat) || isPressurePlate(mat) || isDoor(mat)*/
				|| isDoublePlant(mat) || isSapling(mat) || isButton(mat) || isFlowerPot(mat)) return true;
				return false;
		}
	}

}