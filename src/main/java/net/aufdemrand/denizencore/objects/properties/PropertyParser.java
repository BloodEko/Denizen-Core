package net.aufdemrand.denizencore.objects.properties;

import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.bukkit.BukkitScriptProperties;
import net.aufdemrand.denizencore.objects.properties.entity.*;
import net.aufdemrand.denizencore.objects.properties.inventory.*;
import net.aufdemrand.denizencore.objects.properties.item.*;
import net.aufdemrand.denizencore.utilities.debugging.dB;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class PropertyParser {

    // Keep track of which Property belongs to which dObject
    static Map<Class<? extends dObject>, List<Class>> properties
            = new HashMap<Class<? extends dObject>, List<Class>>();

    // Keep track of the static 'describes' and 'getFrom' methods for each Property
    static Map<Class, Method> describes = new WeakHashMap<Class, Method>();
    static Map<Class, Method> getFrom = new WeakHashMap<Class, Method>();


    public PropertyParser() {
        properties.clear();
        describes.clear();
        getFrom.clear();

        // register properties that add Bukkit code to core objects
        registerProperty(BukkitScriptProperties.class, dScript.class);
        registerProperty(BukkitQueueProperties.class, dScript.class);

        // register core dEntity properties
        registerProperty(EntityAge.class, dEntity.class);
        registerProperty(EntityAngry.class, dEntity.class);
        registerProperty(EntityColor.class, dEntity.class);
        registerProperty(EntityCritical.class, dEntity.class);
        registerProperty(EntityFirework.class, dEntity.class);
        registerProperty(EntityFramed.class, dEntity.class);
        registerProperty(EntityInfected.class, dEntity.class);
        registerProperty(EntityItem.class, dEntity.class);
        registerProperty(EntityJumpStrength.class, dEntity.class);
        registerProperty(EntityKnockback.class, dEntity.class);
        registerProperty(EntityPainting.class, dEntity.class);
        registerProperty(EntityPotion.class, dEntity.class);
        registerProperty(EntityPowered.class, dEntity.class);
        registerProperty(EntityProfession.class, dEntity.class);
        registerProperty(EntityRotation.class, dEntity.class);
        registerProperty(EntitySitting.class, dEntity.class);
        registerProperty(EntitySize.class, dEntity.class);
        registerProperty(EntitySkeleton.class, dEntity.class);
        registerProperty(EntityTame.class, dEntity.class);

        // register core dInventory properties
        registerProperty(InventoryHolder.class, dInventory.class); // Holder must be loaded first to initiate correctly
        registerProperty(InventorySize.class, dInventory.class); // Same with size...(Too small for contents)
        registerProperty(InventoryContents.class, dInventory.class);
        registerProperty(InventoryTitle.class, dInventory.class);

        // register core dItem properties
        registerProperty(ItemApple.class, dItem.class);
        registerProperty(ItemBook.class, dItem.class);
        registerProperty(ItemDisplayname.class, dItem.class);
        registerProperty(ItemDurability.class, dItem.class);
        registerProperty(ItemDye.class, dItem.class);
        registerProperty(ItemEnchantments.class, dItem.class);
        registerProperty(ItemFirework.class, dItem.class);
        registerProperty(ItemLore.class, dItem.class);
        registerProperty(ItemMap.class, dItem.class);
        registerProperty(ItemPlantgrowth.class, dItem.class);
        registerProperty(ItemPotion.class, dItem.class);
        registerProperty(ItemQuantity.class, dItem.class);
        registerProperty(ItemSkullskin.class, dItem.class);
        registerProperty(ItemSpawnEgg.class, dItem.class);

    }

    public void registerProperty(Class property, Class<? extends dObject> object) {
        // Add property to the dObject's Properties list
        List<Class> prop_list;

        // Get current properties list, or make a new one
        if (properties.containsKey(object))
            prop_list = properties.get(object);
        else prop_list = new ArrayList<Class>();

        // Add this property to the list
        prop_list.add(property);

        // Put the list back into the Map
        properties.put(object, prop_list);

        // Cache methods used for fetching new properties
        try {
            describes.put(property, property.getMethod("describes", dObject.class));
            getFrom.put(property, property.getMethod("getFrom", dObject.class));

        } catch (NoSuchMethodException e) {
            dB.echoError("Unable to register property '" + property.getSimpleName() + "'!");
        }

    }

    public static String getPropertiesString(dObject object) {
        StringBuilder prop_string = new StringBuilder();

        // Iterate through each property associated with the dObject type, invoke 'describes'
        // and if 'true', add property string from the property to the prop_string.
        for (Property property: getProperties(object)) {
            String description = property.getPropertyString();
            if (description != null) {
                prop_string.append(property.getPropertyId()).append('=')
                        .append(description.replace(';', (char)0x2011)).append(';');
            }
        }

        // Return the list of properties
        if (prop_string.length() > 0) // Remove final semicolon
            return "[" + prop_string.substring(0, prop_string.length() - 1) + "]";
        else
            return "";
    }

    public static List<Property> getProperties(dObject object) {
        List<Property> props = new ArrayList<Property>();

        try {
            if (properties.containsKey(object.getClass())) {
                for (Class property : properties.get(object.getClass())) {
                    if ((Boolean) describes.get(property).invoke(null, object))
                        props.add((Property) getFrom.get(property).invoke(null, object));
                }
            }

        } catch (IllegalAccessException e) {
            dB.echoError(e);
        } catch (InvocationTargetException e) {
            dB.echoError(e);
        }

        return props;

    }
}